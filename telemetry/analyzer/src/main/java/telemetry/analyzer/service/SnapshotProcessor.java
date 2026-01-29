package telemetry.analyzer.service;

import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import serialization.avro.SensorsSnapshotDeserializer;
import telemetry.analyzer.config.KafkaConfig;
import telemetry.analyzer.handlers.snapshot.SensorEventHandler;
import telemetry.analyzer.model.*;
import telemetry.analyzer.repository.ScenarioRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SnapshotProcessor {
    private final KafkaConfig kafkaConfig;
    private final KafkaConsumer<String, SensorsSnapshotAvro> consumer;
    private final HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient;
    private final ScenarioRepository scenarioRepository;
    private final Map<Class<?>, SensorEventHandler> handlers;

    @Autowired
    public SnapshotProcessor(KafkaConfig kafkaConfig,
                             @GrpcClient("hub-router") HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient,
                             ScenarioRepository scenarioRepository,
                             Set<SensorEventHandler> handlers) {
        this.kafkaConfig = kafkaConfig;
        this.hubRouterClient = hubRouterClient;
        this.scenarioRepository = scenarioRepository;
        this.handlers = handlers.stream()
                .collect(Collectors.toMap(SensorEventHandler::getType, Function.identity()));

        Properties config = new Properties();
        String serverUrl = kafkaConfig.getServer();

        config.put(ConsumerConfig.CLIENT_ID_CONFIG, "snapshot.processor");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "analyzer.snapshots.group");
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, serverUrl);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SensorsSnapshotDeserializer.class);

        consumer = new KafkaConsumer<>(config);
        log.info("SnapshotProcessor is using Kafka-server at url: {}", serverUrl);

        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
    }

    public void start() {
        try {
            String snapshotTopic = kafkaConfig.getTopics().getSnapshots();
            consumer.subscribe(List.of(snapshotTopic));
            log.info("SnapshotProcessor subscribed to the topic: {}", snapshotTopic);

            while (true) {
                ConsumerRecords<String, SensorsSnapshotAvro> records = consumer.poll(Duration.ofMillis(1000));

                for (ConsumerRecord<String, SensorsSnapshotAvro> record : records) {
                    extracted(record);
                }
            }
        } catch (WakeupException ignored) {
            // обработка в блоке finally
        } catch (Exception ex) {
            log.error("Error processing snapshot topic messages: {}", ex.getMessage());
        } finally {
            try {
                consumer.commitSync();
            } finally {
                log.info("Closing SnapshotProcessor Kafka-consumer...");
                consumer.close();
            }
        }
    }

    private void extracted(ConsumerRecord<String, SensorsSnapshotAvro> record) {
        try {
            SensorsSnapshotAvro snapshot = record.value();
            log.debug("Record value: {}", snapshot);

            List<Scenario> scenarios = scenarioRepository.findByHubId(snapshot.getHubId());
            Map<String, SensorStateAvro> states = snapshot.getSensorsState();

            // проверяем каждый сценарий
            for (Scenario scenario : scenarios) {
                log.debug("Scenario: {}", scenario);

                Set<ScenarioCondition> scenarioConditions = scenario.getScenarioConditions();

                boolean satisfiesCondition = false;

                // промежуточная сущность
                for (ScenarioCondition scenarioCondition : scenarioConditions) {
                    // условие
                    Condition condition = scenarioCondition.getCondition();
                    log.debug("Condition: {}", condition);
                    // датчик
                    Sensor sensor = scenarioCondition.getSensor();
                    log.debug("Sensor: {}", sensor);
                    // состояние в снапшоте
                    SensorStateAvro state = states.get(sensor.getId());
                    log.debug("State: {}", state);

                    // если снапшоте нет состояния датчика, описанного в условии, дальнейшая проверка не имеет смысла
                    if (state == null) {
                        log.warn("Snapshot doesn't contain state for sensor with id = {}", sensor.getId());
                        satisfiesCondition = false;
                        break;
                    }

                    // обработчик для датчика
                    SensorEventHandler handler = handlers.get(state.getData().getClass());

                    if (handler == null) {
                        log.error("Handler for sensor wasn't found: {}", state.getData().getClass());
                        break;
                    }

                    // если хотя бы одно условие не выполняется, дальнейшая проверка не имеет смысла
                    if (!handler.checkCondition(state.getData(), condition)) {
                        log.debug("State doesn't satisfy the condition");
                        satisfiesCondition = false;
                        break;
                    }

                    satisfiesCondition = true;
                    log.debug("State does satisfy the condition");
                }

                // если условия не выполнены, сценарий не запускается
                if (!satisfiesCondition) {
                    log.debug("Conditions for scenario was not met");
                    continue;
                }

                log.debug("All conditions was met");
                Set<ScenarioAction> scenarioActions = scenario.getScenarioActions();

                for (ScenarioAction scenarioAction : scenarioActions) {
                    // действие
                    Action action = scenarioAction.getAction();
                    log.debug("Action: {}", action);
                    // датчик
                    Sensor sensor = scenarioAction.getSensor();
                    log.debug("Sensor: {}", sensor);

                    DeviceActionProto deviceAction = DeviceActionProto.newBuilder()
                            .setSensorId(sensor.getId())
                            .setType(ActionTypeProto.valueOf(action.getType().name()))
                            .setValue(action.getValue())
                            .build();

                    Instant ts = Instant.now();

                    DeviceActionRequest actionRequest = DeviceActionRequest.newBuilder()
                            .setHubId(scenario.getHubId())
                            .setScenarioName(scenario.getName())
                            .setAction(deviceAction)
                            .setTimestamp(Timestamp.newBuilder()
                                    .setSeconds(ts.getEpochSecond())
                                    .setNanos(ts.getNano()))
                            .build();

                    log.info("Sending device action request: {}", actionRequest.getAllFields());
                    hubRouterClient.handleDeviceAction(actionRequest);
                }
            }
        } catch (Exception ex) {
            log.error("Error processing snapshot: {}", ex.getMessage());
        }
    }
}

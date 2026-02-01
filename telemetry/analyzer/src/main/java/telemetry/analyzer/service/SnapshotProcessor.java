package telemetry.analyzer.service;

import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
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
import telemetry.analyzer.exception.HandlerNotFoundException;
import telemetry.analyzer.handlers.snapshot.SensorEventHandler;
import telemetry.analyzer.model.*;
import telemetry.analyzer.repository.ScenarioRepository;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
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
    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

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

        config.put(ConsumerConfig.CLIENT_ID_CONFIG, kafkaConfig.getSnapshotConsumer().getClientId());
        config.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConfig.getSnapshotConsumer().getGroupId());
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getServer());
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SensorsSnapshotDeserializer.class);
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        consumer = new KafkaConsumer<>(config);
        log.info("SnapshotProcessor is using Kafka-server at url: {}", kafkaConfig.getServer());

        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
    }

    public void start() {
        try {
            String snapshotTopic = kafkaConfig.getTopics().getSnapshots();
            consumer.subscribe(List.of(snapshotTopic));
            log.info("SnapshotProcessor subscribed to the topic: {}", snapshotTopic);

            while (true) {
                ConsumerRecords<String, SensorsSnapshotAvro> records =
                        consumer.poll(Duration.ofMillis(kafkaConfig.getSnapshotConsumer().getPollDurationMs()));

                int recordCount = 0;

                for (ConsumerRecord<String, SensorsSnapshotAvro> record : records) {
                    try {
                        SensorsSnapshotAvro snapshot = record.value();
                        log.debug("Received snapshot: {}", snapshot);

                        List<Scenario> scenarios = scenarioRepository.findByHubId(snapshot.getHubId());
                        Map<String, SensorStateAvro> states = snapshot.getSensorsState();

                        // проверяем каждый сценарий
                        for (Scenario scenario : scenarios) {
                            log.debug("Scenario: {}", scenario);

                            Set<ScenarioCondition> scenarioConditions = scenario.getScenarioConditions();

                            // проверка выполнения условий
                            if (!isSatisfiesConditions(scenarioConditions, states)) {
                                log.debug("Conditions for scenario were not met");
                            } else {
                                log.debug("All conditions for scenario execution were met");
                                sendDeviceActions(scenario, scenario.getScenarioActions());
                            }
                        }

                        log.debug("Snapshot has been processed");
                        // промежуточная фиксация для избежания повторной обработки снапшотов
                        manageOffsets(record, recordCount, consumer);
                        recordCount++;
                    } catch (Exception ex) {
                        log.error("Error processing snapshot: {}", ex.getMessage());

                        StringWriter stringWriter = new StringWriter();
                        PrintWriter printWriter = new PrintWriter(stringWriter);

                        ex.printStackTrace(printWriter);
                    }
                }

                consumer.commitAsync();
            }
        } catch (WakeupException ignored) {
            // обработка в блоке finally
        } catch (Exception ex) {
            log.error("Error processing snapshot topic messages: {}", ex.getMessage());

            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);

            ex.printStackTrace(printWriter);
        } finally {
            try {
                consumer.commitSync(currentOffsets);
            } finally {
                log.info("Closing SnapshotProcessor Kafka-consumer...");
                consumer.close();
            }
        }
    }

    private boolean isSatisfiesConditions(Set<ScenarioCondition> scenarioConditions,
                                          Map<String, SensorStateAvro> snapshotStates) {
        log.debug("Checking conditions...");

        for (ScenarioCondition scenarioCondition : scenarioConditions) {
            // условие
            Condition condition = scenarioCondition.getCondition();
            log.debug("Condition: {}", condition);
            // датчик
            Sensor sensor = scenarioCondition.getSensor();
            log.debug("Sensor: {}", sensor);
            // состояние в снапшоте
            SensorStateAvro state = snapshotStates.get(sensor.getId());
            log.debug("State: {}", state);

            // если снапшоте нет состояния датчика, описанного в условии, дальнейшая проверка не имеет смысла
            if (state == null) {
                log.warn("Snapshot doesn't contain state for sensor with id = {}", sensor.getId());
                return false;
            }

            // обработчик для датчика
            SensorEventHandler handler = handlers.get(state.getData().getClass());

            if (handler == null) {
                log.error("Handler for sensor wasn't found: {}", state.getData().getClass());
                throw new HandlerNotFoundException("No handler for sensor was found: " + state.getData().getClass());
            }

            // если хотя бы одно условие не выполняется, дальнейшая проверка не имеет смысла
            if (!handler.checkCondition(state.getData(), condition)) {
                log.debug("State doesn't satisfy the condition");
                return false;
            }

            log.debug("State does satisfy the condition");
        }

        return true;
    }

    private void sendDeviceActions(Scenario scenario, Set<ScenarioAction> scenarioActions) {
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

    private void manageOffsets(ConsumerRecord<String, SensorsSnapshotAvro> record,
                               int count,
                               KafkaConsumer<String, SensorsSnapshotAvro> consumer) {
        currentOffsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );

        if (count % kafkaConfig.getSnapshotConsumer().getManualCommitRecordsCount() == 0) {
            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                if (exception != null) {
                    log.error("Error during offset commit: {}", offsets, exception);
                }
            });
        }
    }
}

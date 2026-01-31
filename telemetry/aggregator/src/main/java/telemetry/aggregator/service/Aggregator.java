package telemetry.aggregator.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import serialization.avro.GeneralAvroSerializer;
import serialization.avro.SensorEventDeserializer;
import telemetry.aggregator.config.KafkaConfig;

import java.time.Duration;
import java.util.*;

@Component
@Slf4j
public class Aggregator {
    private final KafkaConfig kafkaConfig;
    private final KafkaConsumer<String, SensorEventAvro> consumer;
    private final KafkaProducer<String, SpecificRecordBase> producer;
    private final Map<String, SensorsSnapshotAvro> snapshots = new HashMap<>();

    public Aggregator(KafkaConfig kafkaConfig) {
        this.kafkaConfig = kafkaConfig;

        Properties consumerConfig = new Properties();
        Properties producerConfig = new Properties();

        consumerConfig.put(ConsumerConfig.CLIENT_ID_CONFIG, kafkaConfig.getConsumer().getClientId());
        consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConfig.getConsumer().getGroupId());
        consumerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getServer());
        consumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SensorEventDeserializer.class);
        consumerConfig.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getServer());
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GeneralAvroSerializer.class);

        consumer = new KafkaConsumer<>(consumerConfig);
        producer = new KafkaProducer<>(producerConfig);
        log.info("Aggregator is using Kafka-server at url: {}", kafkaConfig.getServer());

        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
    }

    public void start() {
        try {
            String sensorsTopic = kafkaConfig.getTopics().getSensors();
            consumer.subscribe(List.of(sensorsTopic));
            log.info("Aggregator subscribed to the topic: {}", sensorsTopic);

            while (true) {
                ConsumerRecords<String, SensorEventAvro> records =
                        consumer.poll(Duration.ofMillis(kafkaConfig.getConsumer().getPollDurationMs()));

                for (ConsumerRecord<String, SensorEventAvro> consumerRecord : records) {
                    SensorEventAvro sensorEvent = consumerRecord.value();
                    log.debug("Received sensor event: {}", sensorEvent);

                    String hubId = sensorEvent.getHubId();

                    Optional<SensorsSnapshotAvro> maybeSnapshot = updateState(sensorEvent);

                    if (maybeSnapshot.isEmpty()) {
                        log.debug("No need for snapshot update");
                        log.debug("Sensor event has been processed");
                        continue;
                    }

                    log.debug("Snapshot has been updated");
                    SensorsSnapshotAvro snapshot = maybeSnapshot.get();

                    snapshots.put(hubId, snapshot);

                    log.debug("Sending updated snapshot to the topic [{}] with key [{}]",
                            kafkaConfig.getTopics().getSnapshots(), hubId);
                    ProducerRecord<String, SpecificRecordBase> producerRecord =
                            new ProducerRecord<>(kafkaConfig.getTopics().getSnapshots(), hubId, snapshot);

                    producer.send(producerRecord);
                    log.debug("Sensor event has been processed");
                }

                consumer.commitSync();
            }
        } catch (WakeupException ignored) {
            // обработка в блоке finally
        } catch (Exception ex) {
            log.error("Error processing sensor event: {}", ex.getMessage());
        } finally {
            try {
                producer.flush();
                consumer.commitSync();
            } finally {
                log.info("Closing Aggregator Kafka-producer...");
                producer.close();

                log.info("Closing Aggregator Kafka-consumer...");
                consumer.close();
            }
        }
    }

    private Optional<SensorsSnapshotAvro> updateState(SensorEventAvro sensorEvent) {
        String hubId = sensorEvent.getHubId();
        String sensorId = sensorEvent.getId();
        log.debug("Event sensor_id: {}", sensorId);

        SensorsSnapshotAvro snapshot = snapshots.get(sensorEvent.getHubId());

        // если для хаба снапшот отсутствует, создаём новый
        if (snapshot == null) {
            log.debug("No snapshot for hub_id: {}", hubId);

            // состояние
            SensorStateAvro sensorState = SensorStateAvro.newBuilder()
                    .setTimestamp(sensorEvent.getTimestamp())
                    .setData(sensorEvent.getPayload())
                    .build();
            log.debug("New state: {}", sensorState);

            Map<String, SensorStateAvro> sensorStateMap = new HashMap<>();
            sensorStateMap.put(sensorId, sensorState);

            // снапшот
            snapshot = SensorsSnapshotAvro.newBuilder()
                    .setHubId(hubId)
                    .setTimestamp(sensorEvent.getTimestamp())
                    .setSensorsState(sensorStateMap)
                    .build();
            log.debug("New snapshot added for hub_id: {}", hubId);

            return Optional.of(snapshot);
        }

        // если есть снапшот
        log.debug("Found snapshot for hub_id: {}", hubId);

        Map<String, SensorStateAvro> sensorStateMap = snapshot.getSensorsState();
        SensorStateAvro sensorState = sensorStateMap.get(sensorId);

        // если в снапшоте нет состояния для датчика
        if (sensorState == null) {
            log.debug("No state for sensor_id: {}", sensorId);

            // состояние
            sensorState = SensorStateAvro.newBuilder()
                    .setTimestamp(sensorEvent.getTimestamp())
                    .setData(sensorEvent.getPayload())
                    .build();
            log.debug("New state: {}", sensorState);

            sensorStateMap.put(sensorId, sensorState);
            log.debug("New state added for sensor_id: {}", sensorId);

            return Optional.of(snapshot);
        }

        // если есть состояние
        if (sensorState.getTimestamp().isAfter(sensorEvent.getTimestamp())) {// если дата в событии неактуальная
            log.debug("Received state is out of date");

            return Optional.empty();
        }

        log.debug("Old state: {}", sensorState.getData());

        if (sensorState.getData().equals(sensorEvent.getPayload())) { // если состояние не изменилось
            log.debug("Received state contains the same data");

            return Optional.empty();
        }

        // обновляем состояние
        log.debug("New state: {}", sensorEvent.getPayload());

        sensorState.setData(sensorEvent.getPayload());
        sensorState.setTimestamp(sensorEvent.getTimestamp());
        sensorStateMap.put(sensorId, sensorState);
        snapshot.setTimestamp(sensorEvent.getTimestamp());

        return Optional.of(snapshot);
    }
}

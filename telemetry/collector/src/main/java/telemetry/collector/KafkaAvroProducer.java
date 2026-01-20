package telemetry.collector;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Properties;
import java.util.concurrent.Future;

@Component
@Slf4j
public class KafkaAvroProducer {

    private final KafkaProducer<String, SpecificRecordBase> producer;

    public KafkaAvroProducer(@Value("${telemetry.collector.kafka.bootstrap.servers}") String serverUrl) {
        log.info("Using Kafka-server at url: {}", serverUrl);

        Properties config = new Properties();

        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serverUrl);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GeneralAvroSerializer.class);

        producer = new KafkaProducer<>(config);
    }

    public Future<RecordMetadata> send(String topic, String key, SpecificRecordBase value) {
        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(topic, key, value);
        log.debug("Sending message to topic [{}] with key [{}]: {}", topic, key, value);

        return producer.send(record);
    }

    @PreDestroy
    public void preDestroy() {
        if (producer != null) {
            log.info("Closing Kafka-producer...");
            producer.flush();
            producer.close();
        }
    }
}

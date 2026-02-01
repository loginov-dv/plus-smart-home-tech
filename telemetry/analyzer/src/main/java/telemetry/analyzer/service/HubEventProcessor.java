package telemetry.analyzer.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import serialization.avro.HubEventDeserializer;
import telemetry.analyzer.config.KafkaConfig;
import telemetry.analyzer.handlers.hub.HubEventHandler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class HubEventProcessor implements Runnable {
    private final KafkaConfig kafkaConfig;
    private final KafkaConsumer<String, HubEventAvro> consumer;
    private final Map<Class<?>, HubEventHandler> handlers;

    public HubEventProcessor(KafkaConfig kafkaConfig,
                             Set<HubEventHandler> handlers) {
        this.kafkaConfig = kafkaConfig;
        this.handlers = handlers.stream()
                .collect(Collectors.toMap(HubEventHandler::getType, Function.identity()));

        Properties config = new Properties();

        config.put(ConsumerConfig.CLIENT_ID_CONFIG, kafkaConfig.getHubConsumer().getClientId());
        config.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConfig.getHubConsumer().getGroupId());
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getServer());
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, HubEventDeserializer.class);
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        consumer = new KafkaConsumer<>(config);
        log.info("HubEventProcessor is using Kafka-server at url: {}", kafkaConfig.getServer());

        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
    }

    @Override
    public void run() {
        try {
            String hubsTopic = kafkaConfig.getTopics().getHubs();
            consumer.subscribe(List.of(hubsTopic));
            log.info("HubEventProcessor subscribed to the topic: {}", hubsTopic);

            while (true) {
                ConsumerRecords<String, HubEventAvro> records =
                        consumer.poll(Duration.ofMillis(kafkaConfig.getHubConsumer().getPollDurationMs()));

                for (ConsumerRecord<String, HubEventAvro> record : records) {
                    try {
                        HubEventAvro hubEvent = record.value();
                        log.debug("Received hub event: {}", hubEvent);

                        HubEventHandler handler = handlers.get(hubEvent.getPayload().getClass());

                        if (handler == null) {
                            log.error("Couldn't find handler for hub event: {}", hubEvent.getPayload().getClass());
                            continue;
                        }

                        handler.process(hubEvent.getPayload(), hubEvent.getHubId());
                        log.debug("Hub event has been processed");
                    } catch (Exception ex) {
                        log.error("Error processing hub event: {}", ex.getMessage());

                        StringWriter stringWriter = new StringWriter();
                        PrintWriter printWriter = new PrintWriter(stringWriter);

                        ex.printStackTrace(printWriter);
                    }
                }

                consumer.commitSync();
            }
        } catch (WakeupException ignored) {
            // обработка в блоке finally
        } catch (Exception ex) {
            log.error("Error processing hub topic messages: {}", ex.getMessage());

            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);

            ex.printStackTrace(printWriter);
        } finally {
            try {
                consumer.commitSync();
            } finally {
                log.info("Closing HubEventProcessor Kafka-consumer...");
                consumer.close();
            }
        }
    }
}

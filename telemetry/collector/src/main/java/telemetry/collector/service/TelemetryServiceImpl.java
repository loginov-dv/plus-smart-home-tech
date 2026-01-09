package telemetry.collector.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import telemetry.collector.KafkaAvroProducer;
import telemetry.collector.model.hub.HubEvent;
import telemetry.collector.model.sensor.SensorEvent;

@Slf4j
@Service
public class TelemetryServiceImpl implements TelemetryService {
    private final KafkaAvroProducer producer;
    private final String sensorsTopic;
    private final String hubsTopic;

    @Autowired
    public TelemetryServiceImpl(KafkaAvroProducer producer,
                                @Value("${telemetry.collector.kafka.sensors.topic}") String sensorsTopic,
                                @Value("${telemetry.collector.kafka.hubs.topic}") String hubsTopic) {
        log.info("Topic for sensors messages: {}", sensorsTopic);
        log.info("Topic for hubs messages: {}", hubsTopic);

        this.producer = producer;
        this.sensorsTopic = sensorsTopic;
        this.hubsTopic = hubsTopic;
    }

    @Override
    public void sendSensorEvent(SensorEvent sensorEvent) {
        producer.send(sensorsTopic, sensorEvent.toAvro());
    }

    @Override
    public void sendHubEvent(HubEvent hubEvent) {
        producer.send(hubsTopic, hubEvent.toAvro());
    }
}

package telemetry.collector.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import telemetry.collector.KafkaCollectorProducer;
import telemetry.collector.model.rest.hub.HubEvent;
import telemetry.collector.model.rest.sensor.SensorEvent;

@Slf4j
@Deprecated
public class TelemetryRestServiceImpl implements TelemetryRestService {
    private final KafkaCollectorProducer producer;
    private final String sensorsTopic;
    private final String hubsTopic;

    @Autowired
    public TelemetryRestServiceImpl(KafkaCollectorProducer producer,
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
        producer.send(sensorsTopic, sensorEvent.getHubId(), sensorEvent.toAvro());
    }

    @Override
    public void sendHubEvent(HubEvent hubEvent) {
        producer.send(hubsTopic, hubEvent.getHubId(), hubEvent.toAvro());
    }
}

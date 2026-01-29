package telemetry.analyzer.handlers.hub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import telemetry.analyzer.model.Sensor;
import telemetry.analyzer.repository.SensorRepository;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceAddedEventHandler implements HubEventHandler {
    private final SensorRepository sensorRepository;

    @Override
    public Class<?> getType() {
        return DeviceAddedEventAvro.class;
    }

    @Transactional
    @Override
    public void process(Object payload, String hubId) {
        DeviceAddedEventAvro deviceAddedEvent = (DeviceAddedEventAvro) payload;
        String sensorId = deviceAddedEvent.getId();
        Optional<Sensor> maybeSensor = sensorRepository.findByIdAndHubId(sensorId, hubId);

        if (maybeSensor.isPresent()) {
            log.warn("Sensor with id = {} is already connected to the hub with id = {}", sensorId, hubId);
            return;
        }

        Sensor sensor = new Sensor();
        sensor.setId(sensorId);
        sensor.setHubId(hubId);

        sensorRepository.save(sensor);
        log.info("Connected new sensor with id = {} to the hub with id = {}", sensorId, hubId);
    }
}

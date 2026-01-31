package telemetry.analyzer.handlers.hub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import telemetry.analyzer.model.Sensor;
import telemetry.analyzer.repository.ActionRepository;
import telemetry.analyzer.repository.ConditionRepository;
import telemetry.analyzer.repository.SensorRepository;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceRemovedEventHandler implements HubEventHandler {
    private final SensorRepository sensorRepository;
    private final ConditionRepository conditionRepository;
    private final ActionRepository actionRepository;

    @Override
    public Class<?> getType() {
        return DeviceRemovedEventAvro.class;
    }

    @Transactional
    @Override
    public void process(Object payload, String hubId) {
        DeviceRemovedEventAvro deviceRemovedEvent = (DeviceRemovedEventAvro) payload;
        String sensorId = deviceRemovedEvent.getId();

        Optional<Sensor> maybeSensor = sensorRepository.findByIdAndHubId(sensorId, hubId);

        if (maybeSensor.isEmpty()) {
            log.warn("Sensor with id = {} not found in hub with id = {}", sensorId, hubId);
            return;
        }

        Sensor sensor = maybeSensor.get();

        // удаляем все связанные условия и действия
        sensor.getScenarioConditions()
                .forEach(scenarioCondition -> conditionRepository.delete(scenarioCondition.getCondition()));
        sensor.getScenarioActions()
                .forEach(scenarioAction -> actionRepository.delete(scenarioAction.getAction()));

        sensorRepository.delete(sensor);
        log.info("Removed sensor with id = {} from hub with id = {}", sensorId, hubId);
    }
}

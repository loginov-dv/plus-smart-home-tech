package telemetry.analyzer.handlers.hub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import telemetry.analyzer.exception.SensorNotFoundException;
import telemetry.analyzer.exception.UnknownConditionValueException;
import telemetry.analyzer.model.*;
import telemetry.analyzer.repository.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScenarioAddedEventHandler implements HubEventHandler {
    private final ScenarioRepository scenarioRepository;
    private final SensorRepository sensorRepository;
    private final ConditionRepository conditionRepository;
    private final ActionRepository actionRepository;
    private final ScenarioConditionRepository scenarioConditionRepository;
    private final ScenarioActionRepository scenarioActionRepository;

    @Override
    public Class<?> getType() {
        return ScenarioAddedEventAvro.class;
    }

    @Transactional
    @Override
    public void process(Object payload, String hubId) {
        ScenarioAddedEventAvro scenarioAddedEvent = (ScenarioAddedEventAvro) payload;
        String scenarioName = scenarioAddedEvent.getName();
        List<ScenarioConditionAvro> scenarioConditionsAvro = scenarioAddedEvent.getConditions();
        List<DeviceActionAvro> deviceActionsAvro = scenarioAddedEvent.getActions();

        Optional<Scenario> maybeScenario = scenarioRepository.findByHubIdAndName(hubId, scenarioName);
        Scenario scenario;

        if (maybeScenario.isPresent()) {
            log.debug("Hub with id = {} already has scenario with name = {}", hubId, scenarioName);
            log.debug("Rewriting all conditions and actions...");

            scenario = maybeScenario.get();

            Set<ScenarioCondition> scenarioConditionsOld = scenario.getScenarioConditions();
            scenarioConditionsOld.forEach(scenarioCondition -> conditionRepository.delete(scenarioCondition.getCondition()));

            Set<ScenarioAction> scenarioActionsOld = scenario.getScenarioActions();
            scenarioActionsOld.forEach(scenarioAction -> actionRepository.delete(scenarioAction.getAction()));
        } else {
            scenario = new Scenario();
            scenario.setName(scenarioName);
            scenario.setHubId(hubId);
            scenario = scenarioRepository.save(scenario);
        }

        for (ScenarioConditionAvro scenarioConditionAvro : scenarioConditionsAvro) {
            String sensorId = scenarioConditionAvro.getSensorId();

            Optional<Sensor> maybeSensor = sensorRepository.findByIdAndHubId(sensorId, hubId);

            if (maybeSensor.isEmpty()) {
                log.error("Sensor with id = {} not found in hub with id = {}", sensorId, hubId);
                throw new SensorNotFoundException(String.format("Sensor with id = %s not found in hub with id = %s",
                        sensorId, hubId));
            }

            Sensor sensor = maybeSensor.get();

            Condition condition = new Condition();
            condition.setType(scenarioConditionAvro.getType());
            condition.setOperation(scenarioConditionAvro.getOperation());

            Object value = scenarioConditionAvro.getValue();

            switch (value) {
                case null -> condition.setValue(null);
                case Integer intValue -> condition.setValue(intValue);
                case Boolean boolValue -> condition.setValue(boolValue ? 1 : 0);
                default -> {
                    log.error("Condition value of unknown type: {}", value.getClass().getName());
                    throw new UnknownConditionValueException(String.format("Condition value of unknown type: %s",
                            value.getClass().getName()));
                }
            }

            condition = conditionRepository.save(condition);

            ScenarioCondition scenarioCondition = new ScenarioCondition();
            scenarioCondition.setScenario(scenario);
            scenarioCondition.setSensor(sensor);
            scenarioCondition.setCondition(condition);

            scenarioCondition = scenarioConditionRepository.save(scenarioCondition);
            log.debug("Created new scenario condition: {}", scenarioCondition);
        }

        for (DeviceActionAvro deviceActionAvro : deviceActionsAvro) {
            String sensorId = deviceActionAvro.getSensorId();

            Optional<Sensor> maybeSensor = sensorRepository.findByIdAndHubId(sensorId, hubId);

            if (maybeSensor.isEmpty()) {
                log.error("Sensor with id = {} not found in hub with id = {}", sensorId, hubId);
                throw new SensorNotFoundException(String.format("Sensor with id = %s not found in hub with id = %s",
                        sensorId, hubId));
            }

            Sensor sensor = maybeSensor.get();

            Action action = new Action();
            action.setType(deviceActionAvro.getType());
            action.setValue(deviceActionAvro.getValue());

            action = actionRepository.save(action);

            ScenarioAction scenarioAction = new ScenarioAction();
            scenarioAction.setScenario(scenario);
            scenarioAction.setSensor(sensor);
            scenarioAction.setAction(action);

            scenarioAction = scenarioActionRepository.save(scenarioAction);
            log.debug("Created new scenario action: {}", scenarioAction);
        }

        log.info("Created new scenario: {}", scenario);
    }
}

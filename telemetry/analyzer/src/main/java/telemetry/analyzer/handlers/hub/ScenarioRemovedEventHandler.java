package telemetry.analyzer.handlers.hub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;
import telemetry.analyzer.model.Scenario;
import telemetry.analyzer.repository.ActionRepository;
import telemetry.analyzer.repository.ConditionRepository;
import telemetry.analyzer.repository.ScenarioRepository;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScenarioRemovedEventHandler implements HubEventHandler {
    private final ScenarioRepository scenarioRepository;
    private final ConditionRepository conditionRepository;
    private final ActionRepository actionRepository;

    @Override
    public Class<?> getType() {
        return ScenarioRemovedEventAvro.class;
    }

    @Transactional
    @Override
    public void process(Object payload, String hubId) {
        ScenarioRemovedEventAvro scenarioRemovedEvent = (ScenarioRemovedEventAvro) payload;
        String scenarioName = scenarioRemovedEvent.getName();

        Optional<Scenario> maybeScenario = scenarioRepository.findByHubIdAndName(hubId, scenarioName);

        if (maybeScenario.isEmpty()) {
            log.warn("Scenario with name = {} for hub with id = {} not found", scenarioName, hubId);
            return;
        }

        Scenario scenario = maybeScenario.get();

        scenario.getScenarioConditions()
                .forEach(scenarioCondition -> conditionRepository.delete(scenarioCondition.getCondition()));
        scenario.getScenarioActions()
                .forEach(scenarioAction -> actionRepository.delete(scenarioAction.getAction()));

        scenarioRepository.deleteByNameAndHubId(scenarioName, hubId);
        log.info("Removed scenario with name = {} from scenarios of hub with id = {}", scenarioName, hubId);
    }
}

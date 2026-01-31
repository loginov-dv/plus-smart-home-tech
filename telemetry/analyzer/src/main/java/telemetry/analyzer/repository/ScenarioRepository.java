package telemetry.analyzer.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import telemetry.analyzer.model.Scenario;

import java.util.List;
import java.util.Optional;

public interface ScenarioRepository extends JpaRepository<Scenario, Long> {
    @EntityGraph(attributePaths = {"scenarioConditions", "scenarioConditions.condition",
            "scenarioConditions.sensor", "scenarioActions",
            "scenarioActions.action", "scenarioActions.sensor"})
    List<Scenario> findByHubId(String hubId);

    Optional<Scenario> findByHubIdAndName(String hubId, String name);

    void deleteByNameAndHubId(String name, String hubId);
}

package telemetry.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import telemetry.analyzer.model.ScenarioAction;
import telemetry.analyzer.model.ScenarioActionPK;

public interface ScenarioActionRepository extends JpaRepository<ScenarioAction, ScenarioActionPK> {
}

package telemetry.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import telemetry.analyzer.model.ScenarioCondition;
import telemetry.analyzer.model.ScenarioConditionPK;

public interface ScenarioConditionRepository extends JpaRepository<ScenarioCondition, ScenarioConditionPK> {
}

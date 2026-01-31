package telemetry.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import telemetry.analyzer.model.Action;

public interface ActionRepository extends JpaRepository<Action, Long> {
}

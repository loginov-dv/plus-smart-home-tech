package telemetry.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import telemetry.analyzer.model.Sensor;

import java.util.Optional;

public interface SensorRepository extends JpaRepository<Sensor, String> {

    Optional<Sensor> findByIdAndHubId(String id, String hubId);
}

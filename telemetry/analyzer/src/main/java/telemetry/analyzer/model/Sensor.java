package telemetry.analyzer.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "sensors")
@Getter
@Setter
@ToString
public class Sensor {
    @Id
    private String id;

    @Column(name = "hub_id")
    private String hubId;

    @OneToMany(mappedBy = "sensor", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<ScenarioCondition> scenarioConditions = new HashSet<>();

    @OneToMany(mappedBy = "sensor", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<ScenarioAction> scenarioActions = new HashSet<>();
}

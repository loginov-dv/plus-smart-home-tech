package telemetry.analyzer.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "scenarios",
        uniqueConstraints = @UniqueConstraint(columnNames = {"hub_id", "name"})
)
@Getter
@Setter
@ToString
public class Scenario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hub_id")
    private String hubId;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "scenario", cascade = CascadeType.REMOVE)
    @ToString.Exclude
    private Set<ScenarioCondition> scenarioConditions = new HashSet<>();

    @OneToMany(mappedBy = "scenario", cascade = CascadeType.REMOVE)
    @ToString.Exclude
    private Set<ScenarioAction> scenarioActions = new HashSet<>();
}

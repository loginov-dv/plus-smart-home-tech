package telemetry.analyzer.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "scenario_conditions")
@Getter
@Setter
@ToString
@IdClass(ScenarioConditionPK.class)
public class ScenarioCondition {
    @Id
    @ManyToOne
    @JoinColumn(name = "scenario_id", referencedColumnName = "id")
    private Scenario scenario;

    @Id
    @ManyToOne
    @JoinColumn(name = "sensor_id", referencedColumnName = "id")
    private Sensor sensor;

    @Id
    @ManyToOne
    @JoinColumn(name = "condition_id", referencedColumnName = "id")
    private Condition condition;
}

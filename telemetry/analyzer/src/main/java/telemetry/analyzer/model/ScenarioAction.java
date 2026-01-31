package telemetry.analyzer.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "scenario_actions")
@Getter
@Setter
@ToString
@IdClass(ScenarioActionPK.class)
public class ScenarioAction {
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
    @JoinColumn(name = "action_id", referencedColumnName = "id")
    private Action action;
}

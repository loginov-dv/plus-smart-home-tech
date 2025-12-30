package telemetry.collector.model.hub.scenario;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class ScenarioCondition {
    @NotNull
    private String sensorId;
    private ScenarioConditionType type;
    private ScenarioOperationType operation;
    private Integer value;
}

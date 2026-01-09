package telemetry.collector.model.hub.scenario;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;

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

    public ScenarioConditionAvro toAvro() {
        return ScenarioConditionAvro.newBuilder()
                .setSensorId(this.getSensorId())
                .setValue(this.getValue())
                .setType(this.getType().toAvro())
                .setOperation(this.getOperation().toAvro())
                .build();
    }
}

package telemetry.analyzer.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class ScenarioConditionPK implements Serializable {
    private Long scenario;
    private String sensor;
    private Long condition;
}

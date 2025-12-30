package telemetry.collector.model.hub.device;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class DeviceAction {
    @NotBlank
    private String sensorId;
    private DeviceActionType type;
    private Integer value;
}

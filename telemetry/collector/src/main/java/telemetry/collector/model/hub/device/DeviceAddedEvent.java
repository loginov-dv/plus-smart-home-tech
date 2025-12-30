package telemetry.collector.model.hub.device;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import telemetry.collector.model.hub.HubEvent;
import telemetry.collector.model.hub.HubEventType;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
public class DeviceAddedEvent extends HubEvent {
    @NotBlank
    private String id;
    @NotNull
    private DeviceType deviceType;

    @Override
    @NotNull
    public HubEventType getType() {
        return HubEventType.DEVICE_ADDED;
    }
}

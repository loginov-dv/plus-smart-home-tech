package telemetry.collector.model.rest.hub.device;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class DeviceAction {
    @NotBlank
    private String sensorId;
    private DeviceActionType type;
    private Integer value;

    public DeviceActionAvro toAvro() {
        return DeviceActionAvro.newBuilder()
                .setSensorId(this.getSensorId())
                .setType(this.getType().toAvro())
                .setValue(this.getValue())
                .build();
    }
}

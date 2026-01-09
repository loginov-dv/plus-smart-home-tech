package telemetry.collector.model.hub.device;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.avro.specific.SpecificRecordBase;

import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
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

    @Override
    public SpecificRecordBase toAvro() {
        DeviceAddedEventAvro deviceAddedEventAvro = DeviceAddedEventAvro.newBuilder()
                .setId(this.getId())
                .setType(this.getDeviceType().toAvro())
                .build();

        return HubEventAvro.newBuilder()
                .setHubId(this.getHubId())
                .setTimestamp(this.getTimestamp())
                .setPayload(deviceAddedEventAvro)
                .build();
    }
}

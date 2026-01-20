package telemetry.collector.model.rest.hub.device;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.avro.specific.SpecificRecordBase;

import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import telemetry.collector.model.rest.hub.HubEvent;
import telemetry.collector.model.rest.hub.HubEventType;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
public class DeviceRemovedEvent extends HubEvent {
    @NotBlank
    private String id;

    @Override
    @NotNull
    public HubEventType getType() {
        return HubEventType.DEVICE_REMOVED;
    }

    @Override
    public SpecificRecordBase toAvro() {
        DeviceRemovedEventAvro deviceRemovedEventAvro = DeviceRemovedEventAvro.newBuilder()
                .setId(this.getId())
                .build();

        return HubEventAvro.newBuilder()
                .setHubId(this.getHubId())
                .setTimestamp(this.getTimestamp())
                .setPayload(deviceRemovedEventAvro)
                .build();
    }
}

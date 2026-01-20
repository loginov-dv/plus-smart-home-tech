package telemetry.collector.model.rest.hub.scenario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.avro.specific.SpecificRecordBase;

import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;
import telemetry.collector.model.rest.hub.HubEvent;
import telemetry.collector.model.rest.hub.HubEventType;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
public class ScenarioRemovedEvent extends HubEvent {
    @NotBlank
    @Size(min = 3)
    private String name;

    @Override
    @NotNull
    public HubEventType getType() {
        return HubEventType.SCENARIO_REMOVED;
    }

    @Override
    public SpecificRecordBase toAvro() {
        ScenarioRemovedEventAvro scenarioRemovedEventAvro = ScenarioRemovedEventAvro.newBuilder()
                .setName(this.getName())
                .build();

        return HubEventAvro.newBuilder()
                .setHubId(this.getHubId())
                .setTimestamp(this.getTimestamp())
                .setPayload(scenarioRemovedEventAvro)
                .build();
    }
}

package telemetry.collector.model.hub.scenario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.avro.specific.SpecificRecordBase;

import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import telemetry.collector.model.hub.HubEvent;
import telemetry.collector.model.hub.HubEventType;
import telemetry.collector.model.hub.device.DeviceAction;

import java.util.List;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
public class ScenarioAddedEvent extends HubEvent {
    @NotBlank
    @Size(min = 3)
    private String name;
    @NotEmpty
    private List<ScenarioCondition> conditions;
    @NotEmpty
    private List<DeviceAction> actions;

    @Override
    @NotNull
    public HubEventType getType() {
        return HubEventType.SCENARIO_ADDED;
    }

    @Override
    public SpecificRecordBase toAvro() {
        List<ScenarioConditionAvro> scenarioConditionAvroList = conditions.stream()
                .map(ScenarioCondition::toAvro)
                .toList();

        List<DeviceActionAvro> deviceActionAvroList = actions.stream()
                .map(DeviceAction::toAvro)
                .toList();

        ScenarioAddedEventAvro scenarioAddedEventAvro = ScenarioAddedEventAvro.newBuilder()
                .setName(this.getName())
                .setConditions(scenarioConditionAvroList)
                .setActions(deviceActionAvroList)
                .build();

        return HubEventAvro.newBuilder()
                .setHubId(this.getHubId())
                .setTimestamp(this.getTimestamp())
                .setPayload(scenarioAddedEventAvro)
                .build();
    }
}

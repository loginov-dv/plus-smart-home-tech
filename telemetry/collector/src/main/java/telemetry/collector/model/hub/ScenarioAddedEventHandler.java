package telemetry.collector.model.hub;

import com.google.protobuf.Timestamp;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioAddedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioConditionProto;
import ru.yandex.practicum.kafka.telemetry.event.*;
import telemetry.collector.exception.UnknownTypeException;

import java.time.Instant;
import java.util.List;

@Component
public class ScenarioAddedEventHandler implements HubEventHandler {
    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.SCENARIO_ADDED;
    }

    @Override
    public SpecificRecordBase toAvro(HubEventProto hubEventProto) {
        ScenarioAddedEventProto scenarioAddedEventProto = hubEventProto.getScenarioAdded();
        List<ScenarioConditionAvro> scenarioConditionAvroList = scenarioAddedEventProto.getConditionList().stream()
                .map(this::scenarioConditionToAvro)
                .toList();
        List<DeviceActionAvro> deviceActionAvroList = scenarioAddedEventProto.getActionList().stream()
                .map(this::deviceActionToAvro)
                .toList();
        ScenarioAddedEventAvro scenarioAddedEventAvro = ScenarioAddedEventAvro.newBuilder()
                .setName(scenarioAddedEventProto.getName())
                .setConditions(scenarioConditionAvroList)
                .setActions(deviceActionAvroList)
                .build();
        Timestamp timestamp = hubEventProto.getTimestamp();

        return HubEventAvro.newBuilder()
                .setHubId(hubEventProto.getHubId())
                .setTimestamp(Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos()))
                .setPayload(scenarioAddedEventAvro)
                .build();
    }

    private ScenarioConditionAvro scenarioConditionToAvro(ScenarioConditionProto scenarioConditionProto) {
        Object value = null;

        if (scenarioConditionProto.hasBoolValue()) {
            value = scenarioConditionProto.getBoolValue();
        } else if (scenarioConditionProto.hasIntValue()) {
            value = scenarioConditionProto.getIntValue();
        }

        ConditionTypeAvro conditionTypeAvro;

        try {
            conditionTypeAvro = ConditionTypeAvro.valueOf(scenarioConditionProto.getType().name());
        } catch (IllegalArgumentException ex) {
            throw new UnknownTypeException("Unknown condition type: " + scenarioConditionProto.getType().name());
        }

        ConditionOperationAvro conditionOperationAvro;

        try {
            conditionOperationAvro = ConditionOperationAvro.valueOf(scenarioConditionProto.getOperation().name());
        } catch (IllegalArgumentException ex) {
            throw new UnknownTypeException("Unknown condition operation: " + scenarioConditionProto.getOperation().name());
        }

        return ScenarioConditionAvro.newBuilder()
                .setSensorId(scenarioConditionProto.getSensorId())
                .setValue(value)
                .setType(conditionTypeAvro)
                .setOperation(conditionOperationAvro)
                .build();
    }

    private DeviceActionAvro deviceActionToAvro(DeviceActionProto deviceActionProto) {
        ActionTypeAvro actionTypeAvro;

        try {
            actionTypeAvro = ActionTypeAvro.valueOf(deviceActionProto.getType().name());
        } catch (IllegalArgumentException ex) {
            throw new UnknownTypeException("Unknown action type: " + deviceActionProto.getType().name());
        }

        return DeviceActionAvro.newBuilder()
                .setSensorId(deviceActionProto.getSensorId())
                .setType(actionTypeAvro)
                .setValue(deviceActionProto.getValue())
                .build();
    }
}

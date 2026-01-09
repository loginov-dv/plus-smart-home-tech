package telemetry.collector.model.hub.scenario;

import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import telemetry.collector.exception.UnknownTypeException;

public enum ScenarioOperationType {
    EQUALS,
    GREATER_THAN,
    LOWER_THAN;

    public ConditionOperationAvro toAvro() {
        try {
            return ConditionOperationAvro.valueOf(this.name());
        } catch (IllegalArgumentException ex) {
            throw new UnknownTypeException("Unknown scenario operation type: " + this.name());
        }
    }
}

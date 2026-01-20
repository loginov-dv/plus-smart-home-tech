package telemetry.collector.model.rest.hub.scenario;

import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
import telemetry.collector.exception.UnknownTypeException;

public enum ScenarioConditionType {
    MOTION,
    LUMINOSITY,
    SWITCH,
    TEMPERATURE,
    CO2LEVEL,
    HUMIDITY;

    public ConditionTypeAvro toAvro() {
        try {
            return ConditionTypeAvro.valueOf(this.name());
        } catch (IllegalArgumentException e) {
            throw new UnknownTypeException("Unknown scenario condition type: " + this.name());
        }
    }
}

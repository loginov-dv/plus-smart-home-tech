package telemetry.collector.model.rest.hub.device;

import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;
import telemetry.collector.exception.UnknownTypeException;

public enum DeviceActionType {
    ACTIVATE,
    DEACTIVATE,
    INVERSE,
    SET_VALUE;

    public ActionTypeAvro toAvro() {
        try {
            return ActionTypeAvro.valueOf(this.name());
        } catch (IllegalArgumentException e) {
            throw new UnknownTypeException("Unknown device action type: " + this.name());
        }
    }
}

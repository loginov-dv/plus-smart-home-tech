package telemetry.collector.model.rest.hub.device;

import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import telemetry.collector.exception.UnknownTypeException;

public enum DeviceType {
    MOTION_SENSOR,
    TEMPERATURE_SENSOR,
    LIGHT_SENSOR,
    CLIMATE_SENSOR,
    SWITCH_SENSOR;

    public DeviceTypeAvro toAvro() {
        try {
            return DeviceTypeAvro.valueOf(this.name());
        } catch (IllegalArgumentException ex) {
            throw new UnknownTypeException("Unknown device type: " + this.name());
        }
    }
}

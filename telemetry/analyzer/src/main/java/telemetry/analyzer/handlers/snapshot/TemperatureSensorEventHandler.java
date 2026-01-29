package telemetry.analyzer.handlers.snapshot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;
import telemetry.analyzer.exception.InvalidConditionTypeException;
import telemetry.analyzer.model.Condition;

@Slf4j
@Component
public class TemperatureSensorEventHandler implements SensorEventHandler {
    @Override
    public Class<?> getType() {
        return TemperatureSensorAvro.class;
    }

    @Override
    public boolean checkCondition(Object event, Condition condition) {
        TemperatureSensorAvro temperatureSensor = (TemperatureSensorAvro) event;
        ConditionTypeAvro conditionType = condition.getType();
        ConditionOperationAvro conditionOperation = condition.getOperation();

        if (!conditionType.equals(ConditionTypeAvro.TEMPERATURE)) {
            log.error("Invalid condition type = {} for temperature sensor event", conditionType);
            throw new InvalidConditionTypeException(String.format("Invalid condition type = %s for temperature " +
                    "sensor event", conditionType.name()));
        }

        boolean result = false;

        if (conditionOperation.equals(ConditionOperationAvro.EQUALS)) {
            result = temperatureSensor.getTemperatureC() == condition.getValue(); // TODO: value = null in snapshot
        } else if (conditionOperation.equals(ConditionOperationAvro.GREATER_THAN)) {
            result = temperatureSensor.getTemperatureC() > condition.getValue();
        } else if (conditionOperation.equals(ConditionOperationAvro.LOWER_THAN)) {
            result = temperatureSensor.getTemperatureC() < condition.getValue();
        }

        return result;
    }
}

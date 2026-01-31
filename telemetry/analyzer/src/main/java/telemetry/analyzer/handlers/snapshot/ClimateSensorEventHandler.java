package telemetry.analyzer.handlers.snapshot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
import telemetry.analyzer.exception.InvalidConditionTypeException;
import telemetry.analyzer.model.Condition;

@Slf4j
@Component
public class ClimateSensorEventHandler implements SensorEventHandler {
    @Override
    public Class<?> getType() {
        return ClimateSensorAvro.class;
    }

    @Override
    public boolean checkCondition(Object event, Condition condition) {
        ClimateSensorAvro climateSensor = (ClimateSensorAvro) event;
        ConditionTypeAvro conditionType = condition.getType();
        ConditionOperationAvro conditionOperation = condition.getOperation();
        int valueActual;

        switch (conditionType) {
            case TEMPERATURE -> valueActual = climateSensor.getTemperatureC();
            case HUMIDITY -> valueActual = climateSensor.getHumidity();
            case CO2LEVEL -> valueActual = climateSensor.getCo2Level();
            default -> {
                log.error("Invalid condition type = {} for climate sensor event", conditionType);
                throw new InvalidConditionTypeException(String.format("Invalid condition type = %s for climate " +
                        "sensor event", conditionType.name()));
            }
        }

        boolean result = false;

        if (conditionOperation.equals(ConditionOperationAvro.EQUALS)) {
            result = valueActual == condition.getValue();
        } else if (conditionOperation.equals(ConditionOperationAvro.GREATER_THAN)) {
            result = valueActual > condition.getValue();
        } else if (conditionOperation.equals(ConditionOperationAvro.LOWER_THAN)) {
            result = valueActual < condition.getValue();
        }

        return result;
    }
}

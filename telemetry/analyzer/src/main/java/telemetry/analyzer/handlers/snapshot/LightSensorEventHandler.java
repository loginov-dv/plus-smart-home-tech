package telemetry.analyzer.handlers.snapshot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import telemetry.analyzer.exception.InvalidConditionTypeException;
import telemetry.analyzer.model.Condition;

@Slf4j
@Component
public class LightSensorEventHandler implements SensorEventHandler {
    @Override
    public Class<?> getType() {
        return LightSensorAvro.class;
    }

    @Override
    public boolean checkCondition(Object event, Condition condition) {
        LightSensorAvro lightSensor = (LightSensorAvro) event;
        ConditionTypeAvro conditionType = condition.getType();
        ConditionOperationAvro conditionOperation = condition.getOperation();

        if (!conditionType.equals(ConditionTypeAvro.LUMINOSITY)) {
            log.error("Invalid condition type = {} for light sensor event", conditionType);
            throw new InvalidConditionTypeException(String.format("Invalid condition type = %s for light " +
                    "sensor event", conditionType.name()));
        }

        boolean result = isResult(condition, conditionOperation, lightSensor);

        return result;
    }

    private boolean isResult(Condition condition, ConditionOperationAvro conditionOperation, LightSensorAvro lightSensor) {
        boolean result = false;

        if (conditionOperation.equals(ConditionOperationAvro.EQUALS)) {
            result = lightSensor.getLuminosity() == condition.getValue(); // TODO: null value
        } else if (conditionOperation.equals(ConditionOperationAvro.GREATER_THAN)) {
            result = lightSensor.getLuminosity() > condition.getValue();
        } else if (conditionOperation.equals(ConditionOperationAvro.LOWER_THAN)) {
            result = lightSensor.getLuminosity() < condition.getValue();
        }

        return result;
    }
}

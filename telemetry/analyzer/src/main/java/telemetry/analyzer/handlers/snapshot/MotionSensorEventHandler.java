package telemetry.analyzer.handlers.snapshot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import telemetry.analyzer.exception.InvalidConditionOperationException;
import telemetry.analyzer.exception.InvalidConditionTypeException;
import telemetry.analyzer.exception.InvalidConditionValueException;
import telemetry.analyzer.model.Condition;

@Slf4j
@Component
public class MotionSensorEventHandler implements SensorEventHandler {
    @Override
    public Class<?> getType() {
        return MotionSensorAvro.class;
    }

    @Override
    public boolean checkCondition(Object event, Condition condition) {
        MotionSensorAvro motionSensor = (MotionSensorAvro) event;
        ConditionTypeAvro conditionType = condition.getType();
        ConditionOperationAvro conditionOperation = condition.getOperation();

        if (!conditionType.equals(ConditionTypeAvro.MOTION)) {
            log.error("Invalid condition type = {} for light sensor event", conditionType);
            throw new InvalidConditionTypeException(String.format("Invalid condition type = %s for motion " +
                    "sensor event", conditionType.name()));
        }

        if (conditionOperation.equals(ConditionOperationAvro.EQUALS)) {
            boolean valueActual = motionSensor.getMotion();
            int valueTargetInt = condition.getValue();

            if (valueTargetInt > 1 || valueTargetInt < 0) {
                log.error("Invalid condition value = {} for motion sensor event", valueTargetInt);
                throw new InvalidConditionValueException(String.format("Invalid condition value = %d for motion " +
                        "sensor event", valueTargetInt));
            }

            boolean valueTarget = valueTargetInt == 1;

            return valueActual == valueTarget;
        } else {
            log.error("Invalid condition operation = {} for motion sensor event", conditionOperation);
            throw new InvalidConditionOperationException(String.format("Invalid condition operation = %s for motion " +
                    "sensor event", conditionOperation.name()));
        }
    }
}

package telemetry.analyzer.handlers.snapshot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import telemetry.analyzer.exception.InvalidConditionOperationException;
import telemetry.analyzer.exception.InvalidConditionTypeException;
import telemetry.analyzer.exception.InvalidConditionValueException;
import telemetry.analyzer.model.Condition;

@Slf4j
@Component
public class SwitchSensorEventHandler implements SensorEventHandler {
    @Override
    public Class<?> getType() {
        return SwitchSensorAvro.class;
    }

    @Override
    public boolean checkCondition(Object event, Condition condition) {
        SwitchSensorAvro switchSensor = (SwitchSensorAvro) event;
        ConditionTypeAvro conditionType = condition.getType();
        ConditionOperationAvro conditionOperation = condition.getOperation();

        if (!conditionType.equals(ConditionTypeAvro.SWITCH)) {
            log.error("Invalid condition type = {} for switch sensor event", conditionType);
            throw new InvalidConditionTypeException(String.format("Invalid condition type = %s for switch " +
                    "sensor event", conditionType.name()));
        }

        if (conditionOperation.equals(ConditionOperationAvro.EQUALS)) {
            boolean valueActual = switchSensor.getState();
            int valueTargetInt = condition.getValue();

            if (valueTargetInt > 1 || valueTargetInt < 0) {
                log.error("Invalid condition value = {} for switch sensor event", valueTargetInt);
                throw new InvalidConditionValueException(String.format("Invalid condition value = %d for switch " +
                        "sensor event", valueTargetInt));
            }

            boolean valueTarget = valueTargetInt == 1;

            return valueActual == valueTarget;
        } else {
            log.error("Invalid condition operation = {} for switch sensor event", conditionOperation);
            throw new InvalidConditionOperationException(String.format("Invalid condition operation = %s for switch " +
                    "sensor event", conditionOperation.name()));
        }
    }
}

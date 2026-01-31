package telemetry.analyzer.handlers.snapshot;

import telemetry.analyzer.model.Condition;

public interface SensorEventHandler {
    Class<?> getType();

    boolean checkCondition(Object event, Condition condition);
}

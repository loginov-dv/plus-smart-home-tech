package telemetry.collector.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Класс-валидатор для {@link ScenarioConditionValue}.
 */
public class ScenarioConditionValueValidator implements ConstraintValidator<ScenarioConditionValue, Object> {
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        return switch (value) {
            case null -> true;
            case Boolean ignored -> true;
            case Integer intValue -> intValue <= 1 && intValue >= 0;
            default -> false;
        };
    }
}

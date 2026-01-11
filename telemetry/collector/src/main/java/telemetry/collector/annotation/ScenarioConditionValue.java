package telemetry.collector.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для проверки корректности поля {@code value} у
 * {@link telemetry.collector.model.hub.scenario.ScenarioCondition}.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ScenarioConditionValueValidator.class)
public @interface ScenarioConditionValue {
    String message() default "Поле должно быть равно целому числу или значению логического типа, либо не задано";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

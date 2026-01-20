package telemetry.collector.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import telemetry.collector.model.rest.hub.scenario.ScenarioCondition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для проверки корректности поля {@code value} у
 * {@link ScenarioCondition}.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ScenarioConditionValueValidator.class)
public @interface ScenarioConditionValue {
    String message() default "Поле должно быть равно целому числу или значению логического типа, либо не задано";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

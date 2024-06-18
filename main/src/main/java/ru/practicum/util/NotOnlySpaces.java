package ru.practicum.util;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AnnotationValidator.class)
public @interface NotOnlySpaces {
    String message() default "The description cannot consist only of spaces";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

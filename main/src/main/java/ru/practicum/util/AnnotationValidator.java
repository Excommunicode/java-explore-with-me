package ru.practicum.util;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class AnnotationValidator implements ConstraintValidator<NotOnlySpaces, String> {


    @Override
    public void initialize(NotOnlySpaces constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null) {
            return true;
        }
        return !value.trim().isEmpty();
    }
}

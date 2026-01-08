package com.example.purchase.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class NotFutureDateValidator implements ConstraintValidator<NotFutureDate, LocalDate> {
    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        return value == null || !value.isAfter(LocalDate.now());
    }
}

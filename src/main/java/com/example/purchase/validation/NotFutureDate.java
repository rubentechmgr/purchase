package com.example.purchase.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NotFutureDateValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotFutureDate {
    String message() default "transactionDate must not be in the future";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

package com.example.purchase.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidCurrencyValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCurrency {
    String message() default "Currency Code must be a valid 3-letter ISO code (examples: USD, CAD, JPY)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
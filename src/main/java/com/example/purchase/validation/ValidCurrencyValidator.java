package com.example.purchase.validation;

import com.example.purchase.domain.TreasuryCurrency;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidCurrencyValidator implements ConstraintValidator<ValidCurrency, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // change: null or blank is NOT allowed
        if (value == null || value.isBlank()) {
            return false;
        }
        String code = value.trim().toUpperCase();
        if (code.length() != 3) {
            return false;
        }
        return TreasuryCurrency.fromCode(code) != null;
    }
}

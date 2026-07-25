package com.springboot.razorpay.vault.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class ExpiryMonthValidator
        implements ConstraintValidator<ExpiryYear, Integer> {

    @Override
    public boolean isValid(Integer inputMonth, ConstraintValidatorContext context) {
        if (inputMonth == null) {
            return false;
        }
        int currentMonth = LocalDate.now().getMonthValue();
        return inputMonth >= currentMonth;
    }
}

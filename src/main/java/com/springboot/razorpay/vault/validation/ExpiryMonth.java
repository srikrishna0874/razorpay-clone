package com.springboot.razorpay.vault.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(
        validatedBy = {ExpiryMonthValidator.class}
)
public @interface ExpiryMonth {

    String message() default "Expiry Month cannot be in past";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

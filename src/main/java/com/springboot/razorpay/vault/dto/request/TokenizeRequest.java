package com.springboot.razorpay.vault.dto.request;

import com.springboot.razorpay.vault.validation.ExpiryMonth;
import com.springboot.razorpay.vault.validation.ExpiryYear;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.LuhnCheck;

import java.util.UUID;

public record TokenizeRequest(

        @NotBlank(message = "PAN is required")
        @LuhnCheck(message = "Invalid card number")
        @Pattern(regexp = "^[0-9]{13,19}$", message = "PAN must be between 13 and 19 digits")
        String pan,

        @NotBlank(message = "CVV is required")
        @Pattern(regexp = "^[0-9]{3,4}$", message = "CVV must be 3 or 4 digits")
        String cvv,

        @NotNull(message = "Expiry month is required")
        @ExpiryMonth
        Integer expiryMonth,

        @NotNull(message = "Expiry year is required")
        @ExpiryYear
        Integer expiryYear,

        UUID customerId,

        @Size(min = 3, message = "Card Holder name should have atleast 3 characters")
        String cardHolderName
) {
}

// LuhnCheck : A simple mathematical formula used to validate identification
//             numbers like credit cards and IMEI codes
// https://www.geeksforgeeks.org/dsa/luhn-algorithm/

// what I understood:
// double every second digit from end (if res is >= 10, then calc sum of that res)
// now sum all these res' and finally add the sum of digits that were not doubled
// if final sum % 10 == 0, valid, else invalid.
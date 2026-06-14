package com.springboot.razorpay.merchant.dto.request;

import com.springboot.razorpay.common.enums.BusinessType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MerchantSignupRequest(
        @NotNull(message = "Name is required")
        @Size(max = 50, message = "Name should not be more than 50 characters long")
        String name,

        @Email(message = "Email is invalid")
        @NotNull(message = "Email is required")
        String email,

        @NotNull(message = "Password is required")
        @Size(min = 8, message = "Password should be atleast 8 characters long")
        String password,

        @Size(max = 50, message = "Business name should not be more than 50 characters long")
        String businessName,


        BusinessType businessType
) {
}

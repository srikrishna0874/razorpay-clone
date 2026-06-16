package com.springboot.razorpay.payment.dto.request;

import com.springboot.razorpay.common.entity.Money;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Map;

public record CreateOrderRequest(
        @NotNull(message = "Amount is required")
        Money money,

        @Size(max = 100)
        String receipt, //order-id known to merchant

        Map<String, Object> notes, // {user_phone, user_address, etc..}

        LocalDateTime expiresAt
) {
}

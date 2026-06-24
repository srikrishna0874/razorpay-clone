package com.springboot.razorpay.payment.gateway.dto;

import com.springboot.razorpay.common.entity.Money;
import com.springboot.razorpay.common.enums.PaymentMethod;

import java.util.Map;
import java.util.UUID;

public record PaymentRequest(
        UUID paymentId,

        UUID orderId,

        UUID merchantId,

        Money amount,

        PaymentMethod paymentMethod,

        Map<String, Object> methodDetails
) {
}

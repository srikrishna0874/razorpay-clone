package com.springboot.razorpay.payment.processor;

import com.springboot.razorpay.common.enums.PaymentMethod;
import com.springboot.razorpay.payment.processor.dto.PaymentProcessorRequest;
import com.springboot.razorpay.payment.processor.dto.PaymentProcessorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentProcessorRouter {

    private final Map<PaymentMethod, PaymentProcessor> paymentProcessors;

    public PaymentProcessorResponse charge(PaymentProcessorRequest paymentProcessorRequest) {
        PaymentProcessor paymentProcessor = paymentProcessors.get(paymentProcessorRequest.paymentMethod());

        if (paymentProcessor == null) {
            throw new IllegalArgumentException(
                    "PaymentProcessor not found for payment method: " + paymentProcessorRequest.paymentMethod());
        }

        return paymentProcessor.charge(paymentProcessorRequest);
    }
}

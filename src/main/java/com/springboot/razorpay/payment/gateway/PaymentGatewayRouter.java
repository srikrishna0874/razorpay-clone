package com.springboot.razorpay.payment.gateway;

import com.springboot.razorpay.common.enums.PaymentMethod;
import com.springboot.razorpay.payment.gateway.dto.PaymentRequest;
import com.springboot.razorpay.payment.gateway.dto.PaymentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentGatewayRouter {

    private final Map<PaymentMethod, PaymentAdapter> paymentAdapters;

    public PaymentResult initiatePayment(PaymentRequest paymentRequest) {
        PaymentAdapter paymentAdapter = paymentAdapters.get(paymentRequest.paymentMethod());

        if (paymentAdapter == null) {
            throw new IllegalArgumentException(
                    "No payment adapter registered for method " + paymentRequest.paymentMethod());
        }

        return paymentAdapter.initiatePayment(paymentRequest);
    }
}

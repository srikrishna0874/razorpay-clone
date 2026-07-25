package com.springboot.razorpay.payment.gateway.adapter;

import com.springboot.razorpay.payment.gateway.PaymentAdapter;
import com.springboot.razorpay.payment.gateway.dto.PaymentRequest;
import com.springboot.razorpay.payment.gateway.dto.PaymentResult;

import java.util.UUID;

public class CardPaymentAdapter implements PaymentAdapter {

    @Override
    public PaymentResult initiatePayment(PaymentRequest paymentRequest) {
        return null;
    }

    @Override
    public PaymentResult capture(UUID id) {
        return null;
    }
}

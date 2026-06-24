package com.springboot.razorpay.payment.gateway.adapter;

import com.springboot.razorpay.payment.gateway.PaymentAdapter;
import com.springboot.razorpay.payment.gateway.dto.PaymentRequest;
import com.springboot.razorpay.payment.gateway.dto.PaymentResult;

public class UpiPaymentAdapter implements PaymentAdapter {
    @Override
    public PaymentResult initiatePayment(PaymentRequest paymentRequest) {
        return null;
    }
}

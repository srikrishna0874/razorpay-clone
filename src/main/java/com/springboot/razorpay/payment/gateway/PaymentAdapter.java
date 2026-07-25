package com.springboot.razorpay.payment.gateway;

import com.springboot.razorpay.payment.gateway.dto.PaymentRequest;
import com.springboot.razorpay.payment.gateway.dto.PaymentResult;

import java.util.UUID;

public interface PaymentAdapter {

    PaymentResult initiatePayment(PaymentRequest paymentRequest);

    PaymentResult capture(UUID id);
}

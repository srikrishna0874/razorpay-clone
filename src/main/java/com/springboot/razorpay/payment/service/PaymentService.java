package com.springboot.razorpay.payment.service;

import com.springboot.razorpay.payment.dto.request.PaymentInitRequestDto;
import com.springboot.razorpay.payment.dto.response.PaymentResponse;

import java.util.UUID;

public interface PaymentService {

    PaymentResponse initiatePayment(UUID merchantId, PaymentInitRequestDto paymentInitRequestDto);

    PaymentResponse capturePayment(UUID merchantId, UUID paymentId);
}

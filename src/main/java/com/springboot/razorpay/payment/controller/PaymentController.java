package com.springboot.razorpay.payment.controller;

import com.springboot.razorpay.payment.dto.request.PaymentInitRequestDto;
import com.springboot.razorpay.payment.dto.response.PaymentResponse;
import com.springboot.razorpay.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping(path = "/v1/payments")
@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    UUID merchantId = UUID.fromString("bf141135-65eb-4276-9226-f1d38ac77baf"); // TODO : Replace with MerchantContext

    @PostMapping
    public ResponseEntity<PaymentResponse> initiatePayment(
            @RequestBody @Valid PaymentInitRequestDto paymentInitRequestDto) {

        return ResponseEntity.status(HttpStatus.CREATED).body(
                paymentService.initiatePayment(merchantId, paymentInitRequestDto)
        );
    }

    @PostMapping(path = "{paymentId}/capture")
    public ResponseEntity<PaymentResponse> capturePayment(@PathVariable UUID paymentId) {

        return ResponseEntity.ok(paymentService.capturePayment(merchantId, paymentId));
    }


}

package com.springboot.razorpay.payment.controller;

import com.springboot.razorpay.payment.dto.request.CreateOrderRequest;
import com.springboot.razorpay.payment.dto.response.OrderResponse;
import com.springboot.razorpay.payment.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    UUID merchantId = UUID.fromString("bf141135-65eb-4276-9226-f1d38ac77baf"); // TODO : Replace with MerchantContext

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody @Valid CreateOrderRequest createOrderRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(merchantId, createOrderRequest));
    }
}

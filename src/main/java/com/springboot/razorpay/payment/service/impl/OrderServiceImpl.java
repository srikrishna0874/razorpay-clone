package com.springboot.razorpay.payment.service.impl;

import com.springboot.razorpay.common.enums.OrderStatus;
import com.springboot.razorpay.common.exception.DuplicateResourceException;
import com.springboot.razorpay.payment.dto.request.CreateOrderRequest;
import com.springboot.razorpay.payment.dto.response.OrderResponse;
import com.springboot.razorpay.payment.entity.OrderRecord;
import com.springboot.razorpay.payment.repository.OrderRepository;
import com.springboot.razorpay.payment.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    @Value("${payment.order.default-order-expiry-minutes:30}")
    private int defaultOrderExpiryMinutes;

    @Override
    public OrderResponse createOrder(UUID merchantId, CreateOrderRequest createOrderRequest) {

        if (createOrderRequest.receipt() != null &&
                orderRepository.existsByMerchantIdAndReceipt(merchantId, createOrderRequest.receipt())) {
            throw new DuplicateResourceException("ORDER_RECEIPT_DUPLICATE",
                    "Order with receipt already exists" + createOrderRequest.receipt());
        }

        OrderRecord order = OrderRecord.builder()
                .merchantId(merchantId)
                .amount(createOrderRequest.money())
                .receipt(createOrderRequest.receipt())
                .notes(createOrderRequest.notes())
                .orderStatus(OrderStatus.CREATED)
                .expiresAt(createOrderRequest.expiresAt() != null ? createOrderRequest.expiresAt() :
                        LocalDateTime.now().plusMinutes(defaultOrderExpiryMinutes))
                .build();

        order = orderRepository.save(order);

        // TODO: Publish Kafka event about order creation

        return new OrderResponse(
                order.getId(),
                merchantId,
                order.getReceipt(),
                order.getAmount(),
                order.getOrderStatus(),
                order.getAttempts(),
                order.getNotes(),
                order.getExpiresAt(),
                null
        );
    }
}

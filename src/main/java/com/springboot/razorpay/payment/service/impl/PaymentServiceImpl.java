package com.springboot.razorpay.payment.service.impl;

import com.springboot.razorpay.common.enums.OrderStatus;
import com.springboot.razorpay.common.enums.PaymentEvent;
import com.springboot.razorpay.common.enums.PaymentStatus;
import com.springboot.razorpay.common.exception.BusinessRuleViolationException;
import com.springboot.razorpay.common.exception.ResourceNotFoundException;
import com.springboot.razorpay.payment.dto.request.PaymentInitRequestDto;
import com.springboot.razorpay.payment.dto.response.PaymentResponse;
import com.springboot.razorpay.payment.entity.OrderRecord;
import com.springboot.razorpay.payment.entity.Payment;
import com.springboot.razorpay.payment.gateway.PaymentGatewayRouter;
import com.springboot.razorpay.payment.gateway.dto.PaymentRequest;
import com.springboot.razorpay.payment.gateway.dto.PaymentResult;
import com.springboot.razorpay.payment.mapper.PaymentMapper;
import com.springboot.razorpay.payment.repository.OrderRepository;
import com.springboot.razorpay.payment.repository.PaymentRepository;
import com.springboot.razorpay.payment.service.PaymentService;
import com.springboot.razorpay.payment.statemachine.PaymentTransitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;

    private final PaymentRepository paymentRepository;

    private final PaymentGatewayRouter paymentGatewayRouter;

    private final PaymentMapper paymentMapper;

    private final PaymentTransitionService paymentTransitionService;

    @Override
    @Transactional
    public PaymentResponse initiatePayment(UUID merchantId, PaymentInitRequestDto paymentInitRequestDto) {

        OrderRecord orderRecord = orderRepository.findByIdAndMerchantId(paymentInitRequestDto.orderId(), merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", paymentInitRequestDto.orderId()));

        if (orderRecord.getOrderStatus() != OrderStatus.CREATED &&
                orderRecord.getOrderStatus() != OrderStatus.ATTEMPTED) {
            throw new BusinessRuleViolationException("ORDER_NOT_PAYABLE",
                    "Order cannot accept payment in status: " + orderRecord.getOrderStatus());
        }

        orderRecord.setOrderStatus(OrderStatus.ATTEMPTED);
        orderRecord.setAttempts(orderRecord.getAttempts() + 1);

        Payment payment = Payment.builder()
                .order(orderRecord)
                .merchantId(merchantId)
                .amount(orderRecord.getAmount())
                .status(PaymentStatus.CREATED)
                .method(paymentInitRequestDto.paymentMethod())
                .methodDetails(paymentInitRequestDto.methodDetails())
                .build();

        payment = paymentRepository.save(payment);

        PaymentRequest paymentRequest = new PaymentRequest(
                payment.getId(),
                paymentInitRequestDto.orderId(),
                merchantId,
                orderRecord.getAmount(),
                paymentInitRequestDto.paymentMethod(),
                paymentInitRequestDto.methodDetails()
        );

        PaymentResult paymentResult = paymentGatewayRouter.initiatePayment(paymentRequest);

        switch (paymentResult) {
            case PaymentResult.Pending pending -> payment.setProcessorReference(pending.registrationRef());

            case PaymentResult.Failure failure -> {
                paymentTransitionService.applyTransition(payment, PaymentEvent.AUTHORIZE_FAIL);
                payment.setErrorCode(failure.errorCode());
                payment.setErrorDescription(failure.errorDescription());
            }

            case PaymentResult.Success success -> {
                log.warn("Invalid state");
                return null;
            }

        }

        payment = paymentRepository.save(payment);
        orderRepository.save(orderRecord);

        // TODO : Send a Kafka event about payment initiation

        return paymentMapper.toResponse(payment);
    }

    @Override
    public PaymentResponse capturePayment(UUID merchantId, UUID paymentId) {
        Payment payment = paymentRepository.findByIdAndMerchantId(paymentId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

        paymentTransitionService.applyTransition(payment, PaymentEvent.CAPTURE_REQUEST);

        PaymentResult paymentResult = paymentGatewayRouter.capture(payment.getMethod(), payment.getId());

        if (paymentResult instanceof PaymentResult.Success(String bankReference)) {

            paymentTransitionService.applyTransition(payment, PaymentEvent.CAPTURE_SUCCESS);
            payment.setCapturedAt(LocalDateTime.now());
            log.info("Payment captured successfully, paymentId = {}, bankReference = {}", payment.getId(),
                    bankReference);
        } else if (paymentResult instanceof PaymentResult.Failure(String errorCode, String errorDescription)) {
            paymentTransitionService.applyTransition(payment, PaymentEvent.CAPTURE_FAIL);
            payment.setErrorCode(errorCode);
            payment.setErrorDescription(errorDescription);

            log.warn("Payment capture failed, paymentId = {}", paymentId);
        }

        payment = paymentRepository.save(payment);

        // TODO : Send a Kafka event

        return paymentMapper.toResponse(payment);
    }
}

//sOlid principle
// Open for extension, close for modification
package com.springboot.razorpay.payment.gateway.adapter;

import com.springboot.razorpay.common.enums.PaymentMethod;
import com.springboot.razorpay.payment.gateway.PaymentAdapter;
import com.springboot.razorpay.payment.gateway.dto.PaymentRequest;
import com.springboot.razorpay.payment.gateway.dto.PaymentResult;
import com.springboot.razorpay.payment.processor.PaymentProcessorRouter;
import com.springboot.razorpay.payment.processor.dto.PaymentProcessorRequest;
import com.springboot.razorpay.payment.processor.dto.PaymentProcessorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class UpiPaymentAdapter implements PaymentAdapter {

    private final PaymentProcessorRouter paymentProcessorRouter;

    @Override
    public PaymentResult initiatePayment(PaymentRequest paymentRequest) {
        log.info("Initiating payment with UPI adapter, paymentId = {}", paymentRequest.paymentId());

        try {
            PaymentProcessorRequest paymentProcessorRequest =
                    PaymentProcessorRequest.nonCard(paymentRequest.paymentId(), PaymentMethod.UPI,
                            paymentRequest.amount(), paymentRequest.methodDetails());

            PaymentProcessorResponse paymentProcessorResponse = paymentProcessorRouter.charge(paymentProcessorRequest);

            return switch (paymentProcessorResponse) {
                case PaymentProcessorResponse.Failure failure ->
                        new PaymentResult.Failure(failure.errorCode(), failure.errorDescription());
                case PaymentProcessorResponse.Pending pending ->
                        new PaymentResult.Pending(pending.processorReference());
                case PaymentProcessorResponse.Success success -> new PaymentResult.Success(success.bankReference());
            };
        } catch (Exception e) {
            log.warn("UPI payment failed, paymentId = {}", e.getMessage());
            return new PaymentResult.Failure("UPI_FAILED", e.getMessage());
        }

    }

    @Override
    public PaymentResult capture(UUID id) {
        return new PaymentResult.Success("UPI_REF");
    }
}

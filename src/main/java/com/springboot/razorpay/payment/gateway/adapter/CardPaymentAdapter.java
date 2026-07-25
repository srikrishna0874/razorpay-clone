package com.springboot.razorpay.payment.gateway.adapter;

import com.springboot.razorpay.payment.gateway.PaymentAdapter;
import com.springboot.razorpay.payment.gateway.dto.PaymentRequest;
import com.springboot.razorpay.payment.gateway.dto.PaymentResult;
import com.springboot.razorpay.payment.processor.dto.PaymentProcessorResponse;
import com.springboot.razorpay.vault.service.VaultService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Component
public class CardPaymentAdapter implements PaymentAdapter {

    private final VaultService vaultService;

    @Override
    public PaymentResult initiatePayment(PaymentRequest paymentRequest) {

        String token = (String) paymentRequest.methodDetails().get("token");

        PaymentProcessorResponse paymentProcessorResponse =
                vaultService.charge(paymentRequest.paymentId(), token, paymentRequest.amount(),
                        paymentRequest.methodDetails());

        return switch (paymentProcessorResponse) {
            case PaymentProcessorResponse.Success success -> new PaymentResult.Success(success.bankReference());

            case PaymentProcessorResponse.Failure failure -> new PaymentResult.Failure(failure.errorCode(),
                    failure.errorDescription());

            case PaymentProcessorResponse.Pending pending -> new PaymentResult.Pending(pending.processorReference());
        };
    }

    @Override
    public PaymentResult capture(UUID id) {
        return null;
    }
}

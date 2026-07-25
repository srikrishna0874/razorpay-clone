package com.springboot.razorpay.payment.processor.strategy;

import com.springboot.razorpay.common.util.RandomizerUtil;
import com.springboot.razorpay.payment.processor.PaymentProcessor;
import com.springboot.razorpay.payment.processor.dto.PaymentProcessorRequest;
import com.springboot.razorpay.payment.processor.dto.PaymentProcessorResponse;

public class NetBankingPaymentProcessor implements PaymentProcessor {
    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest paymentProcessorRequest) {

        final String BANK_CODE_FAIL = "BANK_CODE_FAIL";

        String bankCode = paymentProcessorRequest.methodDetails() != null ?
                paymentProcessorRequest.methodDetails().get("BANK").toString() : null;

        // simulation
        if (BANK_CODE_FAIL.equals(bankCode)) {
            return new PaymentProcessorResponse.Failure(
                    "BANK_REJECTED",
                    "Bank rejected the transaction registration."
            );
        }

        String processorRef = "NBK_PROCESSOR_" + RandomizerUtil.randomBase64(16);

//        String redirectRef = "https://REDIRECT_BANK.com/" + processorRef;

        return new PaymentProcessorResponse.Pending(processorRef);


    }
}

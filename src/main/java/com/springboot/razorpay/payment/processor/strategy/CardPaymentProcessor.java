package com.springboot.razorpay.payment.processor.strategy;

import com.springboot.razorpay.common.util.RandomizerUtil;
import com.springboot.razorpay.payment.processor.PaymentProcessor;
import com.springboot.razorpay.payment.processor.dto.PaymentProcessorRequest;
import com.springboot.razorpay.payment.processor.dto.PaymentProcessorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CardPaymentProcessor implements PaymentProcessor {

    public static final String PAN_CARD_DECLINED = "4000000000000002";
    public static final String PAN_CARD_EXPIRED = "4000000000000002";

    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest paymentProcessorRequest) {

        String pan = paymentProcessorRequest.pan();

        if (PAN_CARD_DECLINED.equals(pan)) {
            log.warn("Card declined payment processor");
            return new PaymentProcessorResponse.Failure("CARD_DECLINED", "The card was declined by the bank");
        }

        if (PAN_CARD_EXPIRED.equals(pan)) {
            log.warn("Card expired payment processor");
            return new PaymentProcessorResponse.Failure("CARD_EXPIRED", "The card has expired");
        }

        String processorRef = "CARD_PROCESSOR_" + RandomizerUtil.randomBase64(16);

        return new PaymentProcessorResponse.Pending(processorRef);
    }
}

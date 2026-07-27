package com.springboot.razorpay.payment.simulator;

import com.springboot.razorpay.common.enums.ChaosMode;
import com.springboot.razorpay.common.enums.PaymentStatus;
import com.springboot.razorpay.common.util.RandomizerUtil;
import com.springboot.razorpay.payment.entity.Payment;
import com.springboot.razorpay.payment.repository.PaymentRepository;
import com.springboot.razorpay.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class BankCallbackSimulator {

    private final PaymentRepository paymentRepository;

    private final PaymentService paymentService;

    private final SimulatorConfig simulatorConfig;

    @Scheduled(fixedDelayString = "${payment.simulator.poll-interval-ms:5000}")
    public void processCallback() {
        LocalDateTime globalWindow = LocalDateTime.now().minusSeconds(1);

        List<Payment> authorizingPayments =
                paymentRepository.findByStatusAndCreatedAtBefore(PaymentStatus.AUTHORIZING, globalWindow);

        log.info("Simulating payments for {} payments", authorizingPayments.size());

        if (authorizingPayments.isEmpty()) return;

        for (Payment payment : authorizingPayments) {
            simulateCallback(payment);
        }
    }

    private void simulateCallback(Payment payment) {
        SimulatorConfig.MethodSimulatorConfig methodSimulatorConfig =
                simulatorConfig.configForMethod(payment.getMethod());

        LocalDateTime dueAt = dueAt(payment, methodSimulatorConfig);

        if (LocalDateTime.now().isBefore(dueAt)) {
            return;
        }

        ChaosMode chaosMode = simulatorConfig.getChaosMode();

        switch (chaosMode) {
            case SUCCESS -> resolve(payment, true);
            case FAILURE -> resolve(payment, false);
            case TIMEOUT -> {
                log.debug("BankCallbackSimulator: Timeout occurred for payment {}", payment.getId());
            }
            case NORMAL, SLOW -> resolve(payment, shouldApprove(payment, methodSimulatorConfig));
        }
    }

    private void resolve(Payment payment, boolean approve) {
        if (approve) {
            String bankReference = "SIM_BANK_REF" + RandomizerUtil.randomBase64(8);

            paymentService.resolveAuthorization(payment.getId(), true, bankReference, null, null);
        } else {
            paymentService.resolveAuthorization(payment.getId(), false, null, "SIM_BANK_ERROR_CODE",
                    "Simulated bank declined");
        }
    }

    private boolean shouldApprove(Payment payment, SimulatorConfig.MethodSimulatorConfig methodSimulatorConfig) {
        int bucket = Math.abs(payment.getId().hashCode()) % 100;

        return bucket < methodSimulatorConfig.getSuccessRate();
    }

    private LocalDateTime dueAt(Payment payment, SimulatorConfig.MethodSimulatorConfig methodSimulatorConfig) {

        int range = methodSimulatorConfig.getMaxDelaySeconds() - methodSimulatorConfig.getMinDelaySeconds();

        int delaySeconds =
                methodSimulatorConfig.getMinDelaySeconds() + Math.abs(payment.getId().hashCode()) % (range + 1);

        if (simulatorConfig.getChaosMode() == ChaosMode.SLOW) {
            delaySeconds *= 2;
        }

        return payment.getCreatedAt().plusSeconds(delaySeconds);
    }
}

// this is Razorpay's internal testing component that simulates bank behavior in a dev/test environment. In
// production, real banks would call Razorpay with actual callbacks.

// BankCallbackSimulator: Simulates Issuer Bank callbacks in test environment
// Scheduled task polls AUTHORIZING payments every 5s, applies simulated delay (dueAt),
// then resolves with approve/decline based on ChaosMode
// Replaces real bank callbacks during dev/testing - no external bank dependency needed

// dueAt():
// E.g., minDelay=2s, maxDelay=5s, paymentCreatedAt=2:00 PM
// Calculates: 2 + (paymentId.hashCode() % 4) = 3s delay (just to get random delay in seconds between [minDelay,
// maxDelay]
// Returns: 2:00:03 PM (created time + delay)
// If ChaosMode.SLOW: multiplies 2s more → 2:00:06 PM
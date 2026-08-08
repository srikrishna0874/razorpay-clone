package com.springboot.razorpay.merchant.cache;

import com.springboot.razorpay.common.enums.Environment;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApiKeyCacheEntry(
        String keyId,

        String keySecretHash,

        String previousKeySecretKeyHash,

        LocalDateTime gracePeriodExpiresAt,

        UUID merchantId,

        Environment environment,

        boolean enabled
) {

    public boolean isInGracePeriod() {
        return gracePeriodExpiresAt != null && LocalDateTime.now().isBefore(gracePeriodExpiresAt);
    }

}

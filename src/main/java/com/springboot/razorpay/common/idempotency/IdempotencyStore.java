package com.springboot.razorpay.common.idempotency;

import java.time.Duration;
import java.util.Optional;

public interface IdempotencyStore {

    String IN_PROGRESS = "__IN_PROGRESS__";

    boolean setIdempotencyKeyIfAbsent(String key, Duration ttl);

    void storeIdempotencyKey(String key, String value, Duration ttl);

    Optional<String> getIdempotencyKeyValue(String key);

    void deleteIdempotencyKey(String key);
}

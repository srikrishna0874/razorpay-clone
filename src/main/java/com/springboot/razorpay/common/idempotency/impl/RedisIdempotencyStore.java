package com.springboot.razorpay.common.idempotency.impl;

import com.springboot.razorpay.common.idempotency.IdempotencyStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;


@Component
@Slf4j
@RequiredArgsConstructor
public class RedisIdempotencyStore implements IdempotencyStore {

    private static final String PREFIX = "idempotency:";

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean setIdempotencyKeyIfAbsent(String key, Duration ttl) {

        try {
            Boolean set = stringRedisTemplate.opsForValue().setIfAbsent(PREFIX + key, IN_PROGRESS, ttl);

            return Boolean.TRUE.equals(set);
        } catch (DataAccessException e) {
            log.warn("Idempotency store unavailable for key {}", key, e);
            return true;
        }

    }

    @Override
    public void storeIdempotencyKey(String key, String value, Duration ttl) {
        try {
            stringRedisTemplate.opsForValue().set(PREFIX + key, value, ttl);
        } catch (DataAccessException e) {
            log.warn("Failed to persist, failing open for key = {}", key, e);
        }

    }

    @Override
    public Optional<String> getIdempotencyKeyValue(String key) {
        try {
            return Optional.ofNullable(stringRedisTemplate.opsForValue().get(PREFIX + key));
        } catch (DataAccessException e) {
            log.warn("Idempotency store unavailable for key {}", key, e);
            return Optional.empty();
        }

    }

    @Override
    public void deleteIdempotencyKey(String key) {
        try {
            stringRedisTemplate.delete(PREFIX + key);
        } catch (DataAccessException e) {
            log.warn("Idempotency store unavailable for key {}", key, e);
        }
    }
}

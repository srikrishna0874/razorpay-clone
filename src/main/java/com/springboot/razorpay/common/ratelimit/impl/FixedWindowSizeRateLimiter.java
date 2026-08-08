package com.springboot.razorpay.common.ratelimit.impl;

import com.springboot.razorpay.common.ratelimit.RateLimitResult;
import com.springboot.razorpay.common.ratelimit.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;


@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rate-limit.method", havingValue = "fixed")
public class FixedWindowSizeRateLimiter implements RateLimiter {

    private final StringRedisTemplate redisTemplate;

    @Override
    public RateLimitResult checkRateLimit(String key, int maxRequestsAllowed, long windowSeconds) {

        String redisKey = "ratelimit:fixed:" + key;

        Long count = redisTemplate.opsForValue().increment(redisKey);

        if (count == null) {
            return RateLimitResult.allowed(maxRequestsAllowed);
        }

        if (count == 1) {
            redisTemplate.expire(redisKey, Duration.ofSeconds(windowSeconds));
        }

        if (count > maxRequestsAllowed) {
            Long ttl = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
            int retryAfterSeconds = (ttl != null && ttl > 0) ? ttl.intValue() : (int) windowSeconds;

            return RateLimitResult.denied(retryAfterSeconds);
        }

        return RateLimitResult.allowed((int) (maxRequestsAllowed - count));

    }
}

package com.springboot.razorpay.common.ratelimit.impl;

import com.springboot.razorpay.common.ratelimit.RateLimitResult;
import com.springboot.razorpay.common.ratelimit.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rate-limit.method", havingValue = "sliding")
public class SlidingWindowRateLimiter implements RateLimiter {

    private final StringRedisTemplate redisTemplate;

    @Override
    public RateLimitResult checkRateLimit(String key, int maxRequestsAllowed, long windowSeconds) {

        long curTimeInMs = System.currentTimeMillis(); // cur
        long recentWindowStartInMs = curTimeInMs - windowSeconds * 1000; // start

        String redisKey = "ratelimit:sliding:" + key;

        var zset = redisTemplate.opsForZSet();
        zset.removeRangeByScore(redisKey, Double.NEGATIVE_INFINITY, recentWindowStartInMs);

        Long count = zset.zCard(redisKey);

        long currentCount = count != null ? count : 0;

        if (currentCount >= maxRequestsAllowed) {
            var oldest = zset.rangeWithScores(redisKey, 0, 0);
            int retryAfter = 1;

            if (oldest != null && !oldest.isEmpty()) {
                Double oldestScore = oldest.iterator().next().getScore();
                if (oldestScore != null) {
                    long windowExpiresMs = oldestScore.longValue() + windowSeconds * 1000;
                    retryAfter = (int) Math.ceil((double) (windowExpiresMs - curTimeInMs) / 1000);
                }
            }

            return RateLimitResult.denied(retryAfter);
        }

        zset.add(redisKey, UUID.randomUUID().toString(), curTimeInMs);
        redisTemplate.expire(redisKey, Duration.ofSeconds(windowSeconds + 1));

        return RateLimitResult.allowed((int) (maxRequestsAllowed - currentCount - 1));

    }
}

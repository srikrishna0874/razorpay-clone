package com.springboot.razorpay.operations.webhook;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WebhookRetryQueue {

    private final StringRedisTemplate redisTemplate;

    @Value("${app.webhook.delivery.redis-key:webhook-retry}")
    private String key;

    public void enqueueWebhookEvent(UUID webhookEventId, LocalDateTime retryAt) {
        long time = getTime();
        redisTemplate.opsForZSet().add(key, webhookEventId.toString(), time);
    }

    public Set<UUID> pollDue(int limit) {

        long now = getTime();
        Set<ZSetOperations.TypedTuple<String>> due = redisTemplate
                .opsForZSet().rangeByScoreWithScores(key, 0, now, 0, limit);

        if (due == null || due.isEmpty()) {
            return Set.of();
        }

        due.forEach(tuple -> redisTemplate.opsForZSet()
                .remove(key, Objects.requireNonNull(tuple.getScore())));

        return due.stream()
                .map(tuple -> UUID.fromString(Objects.requireNonNull(tuple.getValue())))
                .collect(Collectors.toSet());

    }

    private static long getTime() {
        return LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    public void enqueueWebhookEventIfAbsent(UUID id, LocalDateTime nextRetryAt) {
        redisTemplate.opsForZSet().addIfAbsent(key, id.toString(), getTime());
    }
}

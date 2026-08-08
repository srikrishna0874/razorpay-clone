package com.springboot.razorpay.merchant.cache.impl;

import com.springboot.razorpay.merchant.cache.ApiKeyCache;
import com.springboot.razorpay.merchant.cache.ApiKeyCacheEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisApiKeyCache implements ApiKeyCache {

    private static final String API_KEY_CACHE_PREFIX = "apikey:";

    private static final Duration TTL = Duration.ofMinutes(5);

    private final StringRedisTemplate stringRedisTemplate;

    private final ObjectMapper objectMapper;

    @Override
    public Optional<ApiKeyCacheEntry> getApiKeyByKeyId(String keyId) {

        try {
            String resultJson = stringRedisTemplate.opsForValue().get(API_KEY_CACHE_PREFIX + keyId);
            if (resultJson == null) {
                return Optional.empty();
            }

            return Optional.of(objectMapper.readValue(resultJson, ApiKeyCacheEntry.class));
        } catch (Exception e) {
            log.warn("API Key cache read failed for keyId: {}", keyId);
            return Optional.empty();
        }
    }

    @Override
    public void putApiKeyToCache(String keyId, ApiKeyCacheEntry apiKeyCacheEntry) {

        try {
            stringRedisTemplate.opsForValue()
                    .set(API_KEY_CACHE_PREFIX + keyId, objectMapper.writeValueAsString(apiKeyCacheEntry), TTL);
        } catch (Exception e) {
            log.warn("API Key cache write failed for keyId: {}", keyId);
        }

    }

    @Override
    public void evictApiKeyFromCache(String keyId) {
        stringRedisTemplate.delete(API_KEY_CACHE_PREFIX + keyId);
    }
}

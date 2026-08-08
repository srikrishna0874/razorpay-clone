package com.springboot.razorpay.merchant.cache;

import java.util.Optional;

public interface ApiKeyCache {

    Optional<ApiKeyCacheEntry> getApiKeyByKeyId(String keyId);

    void putApiKeyToCache(String keyId, ApiKeyCacheEntry apiKeyCacheEntry);

    void evictApiKeyFromCache(String keyId);

}

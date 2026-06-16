package com.springboot.razorpay.merchant.service;


import com.springboot.razorpay.merchant.dto.request.CreateApiKeyRequest;
import com.springboot.razorpay.merchant.dto.response.ApiKeyCreateResponse;
import com.springboot.razorpay.merchant.dto.response.ApiKeyResponse;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

public interface ApiKeyService {

    ApiKeyCreateResponse createApiKey(UUID merchantId, @Valid CreateApiKeyRequest createApiKeyRequest);

    List<ApiKeyResponse> getAllApiKeysByMerchant(UUID merchantId);

    void revoke(UUID merchantId, UUID keyId);

    ApiKeyCreateResponse rotateApiKey(UUID merchantId, UUID keyId);
}

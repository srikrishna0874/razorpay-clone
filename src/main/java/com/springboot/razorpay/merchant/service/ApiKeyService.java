package com.springboot.razorpay.merchant.service;


import com.springboot.razorpay.merchant.dto.request.CreateApiKeyRequest;
import com.springboot.razorpay.merchant.dto.response.ApiKeyCreateResponse;
import jakarta.validation.Valid;
import java.util.UUID;

public interface ApiKeyService {

    ApiKeyCreateResponse createApiKey(UUID merchantId, @Valid CreateApiKeyRequest createApiKeyRequest);
}

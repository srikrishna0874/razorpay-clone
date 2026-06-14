package com.springboot.razorpay.merchant.service.impl;

import com.springboot.razorpay.common.exception.ResourceNotFoundException;
import com.springboot.razorpay.merchant.dto.request.CreateApiKeyRequest;
import com.springboot.razorpay.merchant.dto.response.ApiKeyCreateResponse;
import com.springboot.razorpay.merchant.entity.ApiKey;
import com.springboot.razorpay.merchant.entity.Merchant;
import com.springboot.razorpay.merchant.repository.ApiKeyRepository;
import com.springboot.razorpay.merchant.repository.MerchantRepository;
import com.springboot.razorpay.merchant.service.ApiKeyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiKeyServiceImpl implements ApiKeyService {

    private final MerchantRepository merchantRepository;

    private final ApiKeyRepository apiKeyRepository;

    @Override
    public ApiKeyCreateResponse createApiKey(UUID merchantId, CreateApiKeyRequest createApiKeyRequest) {

        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("merchant", merchantId));

        String keyId = "rzp_" + createApiKeyRequest.environment().name().toUpperCase() + "big_random_string";

        String rawSecret = "big_random_secret"; // TODO : Replace with cryptographic random hex

        ApiKey apiKey = ApiKey.builder()
                .merchant(merchant)
                .keyId(keyId)
                .keySecretHash(rawSecret) // TODO : Encode with BCryptEncoder
                .environment(createApiKeyRequest.environment())
                .build();

        apiKey = apiKeyRepository.save(apiKey);

        return new ApiKeyCreateResponse(apiKey.getId(), keyId, rawSecret, createApiKeyRequest.environment());
    }
}

package com.springboot.razorpay.merchant.controller;

import com.springboot.razorpay.merchant.dto.request.CreateApiKeyRequest;
import com.springboot.razorpay.merchant.dto.response.ApiKeyCreateResponse;
import com.springboot.razorpay.merchant.dto.response.ApiKeyResponse;
import com.springboot.razorpay.merchant.security.MerchantContext;
import com.springboot.razorpay.merchant.service.ApiKeyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(path = "/v1/merchants/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    private final MerchantContext merchantContext;

    @PostMapping
    public ResponseEntity<ApiKeyCreateResponse> createApiKey(
            @Valid @RequestBody CreateApiKeyRequest createApiKeyRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                apiKeyService.createApiKey(merchantContext.getMerchantId(), createApiKeyRequest)
        );
    }

    @GetMapping
    public ResponseEntity<List<ApiKeyResponse>> getAllApiKeys() {
        return ResponseEntity.ok(apiKeyService.getAllApiKeysByMerchant(merchantContext.getMerchantId()));
    }

    @DeleteMapping("/{keyId}")
    public ResponseEntity<Void> revokeApiKey(@PathVariable UUID keyId) {
        apiKeyService.revoke(merchantContext.getMerchantId(), keyId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{keyId}/rotate")
    public ResponseEntity<ApiKeyCreateResponse> rotateApiKey(@PathVariable UUID keyId) {
        return ResponseEntity.ok(apiKeyService.rotateApiKey(merchantContext.getMerchantId(), keyId));
    }


}

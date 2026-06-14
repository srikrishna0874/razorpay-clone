package com.springboot.razorpay.merchant.controller;

import com.springboot.razorpay.merchant.dto.request.CreateApiKeyRequest;
import com.springboot.razorpay.merchant.dto.response.ApiKeyCreateResponse;
import com.springboot.razorpay.merchant.service.ApiKeyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(path = "/v1/merchants/{merchantId}/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    @PostMapping
    public ResponseEntity<ApiKeyCreateResponse> createApiKey(@PathVariable UUID merchantId,
                                                             @Valid @RequestBody
                                                             CreateApiKeyRequest createApiKeyRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                apiKeyService.createApiKey(merchantId, createApiKeyRequest)
        );
    }

}

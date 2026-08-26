package com.springboot.razorpay.merchant.controller;

import com.springboot.razorpay.merchant.dto.request.UpdateWebhookConfigRequest;
import com.springboot.razorpay.merchant.dto.response.WebhookConfigResponse;
import com.springboot.razorpay.merchant.security.MerchantContext;
import com.springboot.razorpay.merchant.service.WebhookConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/merchants/webhooks")
@RequiredArgsConstructor
public class WebhookConfigController {

    private final WebhookConfigService webhookConfigService;

    private MerchantContext merchantContext;

    @PostMapping
    public ResponseEntity<WebhookConfigResponse> createWebhookConfig(
            @Valid @RequestBody UpdateWebhookConfigRequest request) {
        return ResponseEntity.ok(webhookConfigService.createWebhook(merchantContext.getMerchantId(), request));
    }

    @GetMapping
    public ResponseEntity<List<WebhookConfigResponse>> getAllWebhookConfig() {
        return ResponseEntity.ok(webhookConfigService.listAllWebhooks(merchantContext.getMerchantId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WebhookConfigResponse> getWebhookConfigById(@PathVariable UUID id) {
        return ResponseEntity.ok(webhookConfigService.getWebhookById(merchantContext.getMerchantId(), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WebhookConfigResponse> updateWebhookConfig(@PathVariable UUID id,
                                                                     @Valid @RequestBody
                                                                     UpdateWebhookConfigRequest request) {
        return ResponseEntity.ok(webhookConfigService.updateWebhook(merchantContext.getMerchantId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWebhookConfig(@PathVariable UUID id) {
        webhookConfigService.deleteWebhookById(merchantContext.getMerchantId(), id);

        return ResponseEntity.noContent().build();
    }

}

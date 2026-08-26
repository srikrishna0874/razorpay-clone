package com.springboot.razorpay.merchant.service;

import com.springboot.razorpay.merchant.dto.request.UpdateWebhookConfigRequest;
import com.springboot.razorpay.merchant.dto.response.WebhookConfigResponse;

import java.util.List;
import java.util.UUID;

public interface WebhookConfigService {

    WebhookConfigResponse createWebhook(UUID merchantId, UpdateWebhookConfigRequest request);

    List<WebhookConfigResponse> listAllWebhooks(UUID merchantId);

    WebhookConfigResponse getWebhookById(UUID merchantId, UUID webhookConfigId);

    WebhookConfigResponse updateWebhook(UUID merchantId, UUID webhookConfigId, UpdateWebhookConfigRequest request);

    void deleteWebhookById(UUID merchantId, UUID webhookConfigId);

}

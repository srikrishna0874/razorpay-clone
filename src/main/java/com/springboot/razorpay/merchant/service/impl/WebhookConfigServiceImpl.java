package com.springboot.razorpay.merchant.service.impl;

import com.springboot.razorpay.common.exception.ResourceNotFoundException;
import com.springboot.razorpay.common.util.RandomizerUtil;
import com.springboot.razorpay.merchant.api.MerchantWebhookApi;
import com.springboot.razorpay.merchant.dto.request.UpdateWebhookConfigRequest;
import com.springboot.razorpay.merchant.dto.response.WebhookConfigResponse;
import com.springboot.razorpay.common.dto.WebhookTarget;
import com.springboot.razorpay.merchant.entity.Merchant;
import com.springboot.razorpay.merchant.entity.MerchantWebhookConfig;
import com.springboot.razorpay.merchant.mapper.WebhookConfigMapper;
import com.springboot.razorpay.merchant.repository.MerchantRepository;
import com.springboot.razorpay.merchant.repository.WebhookConfigRepository;
import com.springboot.razorpay.merchant.service.WebhookConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookConfigServiceImpl implements WebhookConfigService, MerchantWebhookApi {

    private final WebhookConfigRepository webhookConfigRepository;

    private final MerchantRepository merchantRepository;

    private final BytesEncryptor bytesEncryptor;

    private final WebhookConfigMapper webhookConfigMapper;

    @Override
    public WebhookConfigResponse createWebhook(UUID merchantId, UpdateWebhookConfigRequest request) {

        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant", merchantId));

        String rawSecret = RandomizerUtil.randomBase64(32);
        byte[] rawSecretBytes = rawSecret.getBytes(StandardCharsets.UTF_8);
        String encryptedSecret = Base64.getEncoder()
                .encodeToString(bytesEncryptor.encrypt(rawSecretBytes));

        MerchantWebhookConfig webhookConfig = MerchantWebhookConfig.builder()
                .merchant(merchant)
                .targetUrl(request.targetUrl())
                .enabled(true)
                .eventTypes(request.eventTypes())
                .webhookSecret(encryptedSecret)
                .build();

        webhookConfig = webhookConfigRepository.save(webhookConfig);

        return webhookConfigMapper.toResponse(webhookConfig, rawSecret);

    }

    @Override
    public List<WebhookConfigResponse> listAllWebhooks(UUID merchantId) {

        return webhookConfigRepository.findByMerchant_Id(merchantId).stream()
                .map(config -> webhookConfigMapper.toResponse(config, null))
                .toList();

    }

    @Override
    public WebhookConfigResponse getWebhookById(UUID merchantId, UUID webhookConfigId) {
        MerchantWebhookConfig webhookConfig = requireOwnedConfig(merchantId, webhookConfigId);

        return webhookConfigMapper.toResponse(webhookConfig, null);
    }

    @Override
    @Transactional
    public WebhookConfigResponse updateWebhook(UUID merchantId, UUID webhookConfigId,
                                               UpdateWebhookConfigRequest request) {
        MerchantWebhookConfig webhookConfig = requireOwnedConfig(merchantId, webhookConfigId);

        webhookConfig.setTargetUrl(request.targetUrl());
        webhookConfig.setEventTypes(request.eventTypes());

        log.info("Merchant webhook config updated for id={}, merchantId = {}", webhookConfigId, merchantId);

        return webhookConfigMapper.toResponse(webhookConfig, null);
    }

    @Override
    @Transactional
    public void deleteWebhookById(UUID merchantId, UUID webhookConfigId) {
        MerchantWebhookConfig webhookConfig = requireOwnedConfig(merchantId, webhookConfigId);

        webhookConfigRepository.delete(webhookConfig);

        log.info("Merchant webhook config deleted id = {}, merchantId = {}", webhookConfigId, merchantId);
    }

    private MerchantWebhookConfig requireOwnedConfig(UUID merchantId, UUID webhookConfigId) {
        return webhookConfigRepository.findByIdAndMerchant_Id(webhookConfigId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("MerchantWebhookConfig", webhookConfigId));
    }

    @Override
    public List<WebhookTarget> getActiveConfigForEvent(UUID merchantId, String eventType) {

        return webhookConfigRepository.findByMerchant_IdAndEnabledTrue(merchantId)
                .stream()
                .filter(config -> config.isSubscribedTo(eventType))
                .map(config -> {
                    byte[] decryptedSecretBytes =
                            bytesEncryptor.decrypt(config.getWebhookSecret().getBytes(StandardCharsets.UTF_8));
                    return new WebhookTarget(config.getId(), config.getTargetUrl(),
                            new String(decryptedSecretBytes, StandardCharsets.UTF_8));
                })
                .toList();
    }
}

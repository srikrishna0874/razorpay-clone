package com.springboot.razorpay.merchant.repository;

import com.springboot.razorpay.merchant.entity.MerchantWebhookConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WebhookConfigRepository extends JpaRepository<MerchantWebhookConfig, UUID> {

    List<MerchantWebhookConfig> findByMerchant_Id(UUID merchantId);

    Optional<MerchantWebhookConfig> findByIdAndMerchant_Id(UUID webhookConfigId, UUID merchantId);

    List<MerchantWebhookConfig> findByMerchant_IdAndEnabledTrue(UUID merchantId);
}

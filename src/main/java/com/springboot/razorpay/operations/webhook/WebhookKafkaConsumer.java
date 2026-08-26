package com.springboot.razorpay.operations.webhook;

import com.springboot.razorpay.common.dto.WebhookTarget;
import com.springboot.razorpay.common.enums.WebhookEventStatus;
import com.springboot.razorpay.common.util.SignerUtil;
import com.springboot.razorpay.merchant.api.MerchantWebhookApi;
import com.springboot.razorpay.operations.entity.WebhookEvent;
import com.springboot.razorpay.operations.repository.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookKafkaConsumer {

    private final WebhookEventRepository webhookEventRepository;

    private final MerchantWebhookApi merchantWebhookApi;

    private final ObjectMapper objectMapper;

    private final SignerUtil signerUtil;

    private final WebhookRetryQueue retryQueue;


    @KafkaListener(topics = {
            "${app.kafka.topics.payment:payments.events}",
            "${app.kafka.topics.order:orders.events}",
            "${app.kafka.topics.refund:refunds.events}",
            "${app.kafka.topics.settlement:settlements.events}"
    })
    public void onWebhookEvent(ConsumerRecord<String, Map<String, Object>> record, Acknowledgment acknowledgment) {

        try {
            Map<String, Object> envelope = record.value();
            Map<String, Object> data = (Map<String, Object>) envelope.get("data");

            String eventType = envelope.get("eventType").toString();

            Object merchantIdObject = data.get("merchantId");
            if (merchantIdObject == null) {
                log.warn("No merchantId was found and skipping the event: {}", eventType);
                acknowledgment.acknowledge();

                return;
            }

            UUID merchantId = UUID.fromString(merchantIdObject.toString());

            List<WebhookTarget> targets = merchantWebhookApi.getActiveConfigForEvent(merchantId, eventType);
            if (targets.isEmpty()) {
                log.debug("No webhook target was found and skipping the event: {}", eventType);
                acknowledgment.acknowledge();
                return;
            }

            Map<String, Object> signatureData = Map.of("event", eventType,
                    "payload", data);
            String signatureJson = objectMapper.writeValueAsString(signatureData);

            for (WebhookTarget target : targets) {
                String signature = signerUtil.signPayload(signatureJson, target.webhookSecret());

                WebhookEvent webhookEvent = WebhookEvent.builder()
                        .merchantId(merchantId)
                        .eventType(eventType)
                        .payload(data)
                        .targetUrl(target.targetUrl())
                        .signature(signature)
                        .status(WebhookEventStatus.PENDING)
                        .nextRetryAt(LocalDateTime.now())
                        .build();

                webhookEvent = webhookEventRepository.save(webhookEvent);

                retryQueue.enqueueWebhookEvent(webhookEvent.getId(), webhookEvent.getNextRetryAt());
            }

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Webhook consumer failed to process the record, offset: {}", record.offset(), e);

//            TODO: check exceptions for acknowledgement
        }

    }
}

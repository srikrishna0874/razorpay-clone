package com.springboot.razorpay.payment.outbox;

import com.springboot.razorpay.common.enums.EventAggregateType;
import com.springboot.razorpay.payment.entity.OutboxEvent;
import com.springboot.razorpay.payment.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {

    private OutboxEventRepository outboxEventRepository;

    public void publishOutboxEvent(EventAggregateType aggregateType, UUID aggregateId, String eventType,
                                   Map<String, Object> payload) {
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(payload)
                .build();

        outboxEventRepository.save(outboxEvent);
    }


}

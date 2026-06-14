package com.github.jokicmilica.inventory_service.service;

import com.github.jokicmilica.inventory_service.model.OutboxEntry;
import com.github.jokicmilica.inventory_service.model.OutboxStatus;
import com.github.jokicmilica.inventory_service.repository.OutboxRepository;
import com.github.jokicmilica.model.OrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class OutboxScheduler {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, OrderResult> kafkaTemplate;

    @Scheduled(fixedDelay = 5000)
    public void publishPendingEntries() {
        List<OutboxEntry> pending = outboxRepository.findByStatus(OutboxStatus.PENDING);
        for (OutboxEntry entry : pending) {
            try {
                kafkaTemplate.send("order-results", entry.getOrderId(), entry.getPayload())
                        .get(5, TimeUnit.SECONDS);
                entry.setStatus(OutboxStatus.PUBLISHED);
                outboxRepository.save(entry);
            } catch (Exception e) {
                log.error("Failed to publish outbox entry: {}", entry.getOrderId(), e);
            }
        }
    }

}

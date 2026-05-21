package com.github.jokicmilica.inventory_service.consumer;

import com.github.jokicmilica.constants.KafkaTopics;
import com.github.jokicmilica.inventory_service.producer.OrderResultProducer;
import com.github.jokicmilica.inventory_service.service.InventoryService;
import com.github.jokicmilica.model.OrderEvent;
import com.github.jokicmilica.model.OrderResult;
import com.github.jokicmilica.model.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class InventoryConsumer {

    private final InventoryService inventoryService;
    private final OrderResultProducer orderResultProducer;

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @KafkaListener(topics = KafkaTopics.ORDERS, groupId = "${spring.kafka.consumer.group-id}")
    public void consume(OrderEvent event) {
        log.info("Received order event for orderId: {}", event.orderId());
        OrderResult result = inventoryService.processOrder(event);
        orderResultProducer.send(result);
    }

    @DltHandler
    public void handleDlt(OrderEvent event, Throwable exception) {
        log.error("Order failed all retries, orderId: {}, reason: {}", event.orderId(), exception.getMessage(), exception);
        try {
            orderResultProducer.send(event.orderId(), OrderStatus.FAILED,
                    String.format("Order failed after all retry attempts: %s", exception.getMessage()));
        } catch (RuntimeException sendException) {
            log.error("Failed to publish FAILED result from DLT handler, orderId: {}", event.orderId(), sendException);
        }
    }
}
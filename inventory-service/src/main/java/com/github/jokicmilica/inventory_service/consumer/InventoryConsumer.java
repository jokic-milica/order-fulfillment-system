package com.github.jokicmilica.inventory_service.consumer;

import com.github.jokicmilica.constants.KafkaTopics;
import com.github.jokicmilica.inventory_service.exception.NonRetryableException;
import com.github.jokicmilica.inventory_service.producer.OrderResultProducer;
import com.github.jokicmilica.inventory_service.service.InventoryService;
import com.github.jokicmilica.model.OrderEvent;
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
            attempts = "4",
            backoff = @Backoff(delay = 1000, multiplier = 2),
            exclude = {NonRetryableException.class}
    )
    @KafkaListener(topics = KafkaTopics.ORDERS, groupId = "${spring.kafka.consumer.group-id}")
    public void consume(OrderEvent event) {
        log.info("Received order event for orderId: {}", event.orderId());
        inventoryService.processOrder(event);
    }

    @DltHandler
    public void handleDlt(OrderEvent event) {
        log.error("Order failed all retries, orderId: {}", event.orderId());
        orderResultProducer.send(event.orderId(), OrderStatus.FAILED, "Order failed after all retry attempts");
    }
}
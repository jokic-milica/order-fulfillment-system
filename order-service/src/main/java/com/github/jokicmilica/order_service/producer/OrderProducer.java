package com.github.jokicmilica.order_service.producer;

import com.github.jokicmilica.constants.KafkaTopics;
import com.github.jokicmilica.model.OrderEvent;
import com.github.jokicmilica.model.OrderStatus;
import com.github.jokicmilica.order_service.exception.DuplicateOrderException;
import com.github.jokicmilica.order_service.exception.KafkaProducerException;
import com.github.jokicmilica.order_service.model.OrderRequest;
import com.github.jokicmilica.order_service.service.OrderStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RequiredArgsConstructor
@Component
@Slf4j
public class OrderProducer {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    private final OrderStatusService orderStatusService;

    public void send(OrderRequest request) {
        OrderEvent event = new OrderEvent(
                request.orderId(),
                request.itemId(),
                request.quantity(),
                OrderStatus.PENDING,
                Instant.now()
        );

        boolean reserved = orderStatusService.reserveIfAbsent(request.orderId(), OrderStatus.PENDING);
        if (!reserved) {
            log.warn("Duplicate order detected, orderId: {}", request.orderId());
            throw new DuplicateOrderException("Order already exists: " + request.orderId());
        }

        try {
            kafkaTemplate.send(KafkaTopics.ORDERS, event.orderId(), event)
                    .get(5, TimeUnit.SECONDS);
            log.info("Order event sent successfully, orderId: {}", event.orderId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            orderStatusService.removeStatus(request.orderId());
            log.error("Interrupted while sending order event, orderId: {}", event.orderId(), e);
            throw new KafkaProducerException("Interrupted while sending order event");
        } catch (ExecutionException | TimeoutException e) {
            orderStatusService.removeStatus(request.orderId());
            log.error("Failed to send order event, orderId: {}", event.orderId(), e);
            throw new KafkaProducerException("Failed to publish order event");
        }
    }
}

package com.github.jokicmilica.order_service.producer;

import com.github.jokicmilica.order_service.exception.KafkaProducerException;
import com.github.jokicmilica.order_service.model.OrderEvent;
import com.github.jokicmilica.order_service.model.OrderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderProducer {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @Value("${kafka.topic.orders}")
    private String ordersTopic;

    public void send(OrderRequest request) {
        OrderEvent event = new OrderEvent(
                request.orderId(),
                request.itemId(),
                request.quantity(),
                "PENDING",
                Instant.now()
        );

        try {
            kafkaTemplate.send(ordersTopic, event.orderId(), event)
                    .get(5, TimeUnit.SECONDS);
            log.info("Order event sent successfully for orderId: {}", event.orderId());
        } catch (Exception ex) {
            log.error("Failed to send order event for orderId: {}", event.orderId(), ex);
            throw new KafkaProducerException("Failed to publish order event");
        }
    }
}

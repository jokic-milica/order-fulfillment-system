package com.github.jokicmilica.inventory_service.producer;

import com.github.jokicmilica.constants.KafkaTopics;
import com.github.jokicmilica.model.OrderResult;
import com.github.jokicmilica.model.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import com.github.jokicmilica.exception.KafkaProducerException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderResultProducer {

    private final KafkaTemplate<String, OrderResult> kafkaTemplate;

    public void send(String orderId, OrderStatus status, String message) {
        send(new OrderResult(orderId, status, message));
    }

    public void send(OrderResult result) {
        String orderId = result.orderId();
        try {
            kafkaTemplate.send(KafkaTopics.ORDER_RESULTS, orderId, result)
                    .get(5, TimeUnit.SECONDS);
            log.info("Order result sent, orderId: {}, status: {}", orderId, result.status());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while sending order result, orderId: {}", orderId, e);
            throw new KafkaProducerException("Interrupted while sending order result");
        } catch (ExecutionException | TimeoutException e) {
            log.error("Failed to send order result, orderId: {}", orderId, e);
            throw new KafkaProducerException("Failed to send order result");
        }
    }
}
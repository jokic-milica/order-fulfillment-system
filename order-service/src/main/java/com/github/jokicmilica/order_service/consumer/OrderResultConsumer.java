package com.github.jokicmilica.order_service.consumer;

import com.github.jokicmilica.constants.KafkaTopics;
import com.github.jokicmilica.model.OrderResult;
import com.github.jokicmilica.order_service.service.OrderStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderResultConsumer {

    private final OrderStatusService orderStatusService;

    @KafkaListener(topics = KafkaTopics.ORDER_RESULTS, groupId = "${spring.kafka.consumer.group-id}")
    public void consume(OrderResult result) {
        log.info("Received order result for orderId: {}, status: {}", result.orderId(), result.status());
        orderStatusService.updateOrderResult(result);
    }
}

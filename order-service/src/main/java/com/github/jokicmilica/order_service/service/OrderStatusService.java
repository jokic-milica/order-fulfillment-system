package com.github.jokicmilica.order_service.service;

import com.github.jokicmilica.model.OrderResult;
import com.github.jokicmilica.model.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class OrderStatusService {

    private final Map<String, OrderResult> orderResultMap = new ConcurrentHashMap<>();

    public void updateOrderResult(OrderResult result) {
        orderResultMap.put(result.orderId(), result);
        log.info("Order status updated, orderId: {}, status: {}, message: {}", result.orderId(), result.status(), result.message());
    }
    public boolean reserveIfAbsent(String orderId, OrderStatus status) {
        OrderResult pending = new OrderResult(orderId, status, String.format("Order with id %s received and queued for processing", orderId));
        return orderResultMap.putIfAbsent(orderId, pending) == null;
    }

    public Optional<OrderResult> getOrderResult(String orderId) {
        return Optional.ofNullable(orderResultMap.get(orderId));
    }

    public void removeOrderResult(String orderId) {
        orderResultMap.remove(orderId);
    }
}

package com.github.jokicmilica.order_service.service;

import com.github.jokicmilica.model.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class OrderStatusService {

    private final Map<String, OrderStatus> orderStatusMap = new ConcurrentHashMap<>();

    public void updateStatus(String orderId, OrderStatus status) {
        orderStatusMap.put(orderId, status);
        log.info("Order status updated, orderId: {}, status: {}", orderId, status);
    }

    public Optional<OrderStatus> getStatus(String orderId) {
        return Optional.ofNullable(orderStatusMap.get(orderId));
    }

    public boolean reserveIfAbsent(String orderId, OrderStatus status) {
        return orderStatusMap.putIfAbsent(orderId, status) == null;
    }

    public void removeStatus(String orderId) {
        orderStatusMap.remove(orderId);
    }
}

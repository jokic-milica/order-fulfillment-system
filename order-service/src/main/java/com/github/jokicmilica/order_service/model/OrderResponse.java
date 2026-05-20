package com.github.jokicmilica.order_service.model;

public record OrderResponse(
        String orderId,
        String status,
        String message
) {}

package com.github.jokicmilica.model;

public record OrderResult(
        String orderId,
        OrderStatus status,
        String message
) {}

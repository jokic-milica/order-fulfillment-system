package com.github.jokicmilica.model;

public record OrderEvent(
        String orderId,
        String itemId,
        int quantity
) {}

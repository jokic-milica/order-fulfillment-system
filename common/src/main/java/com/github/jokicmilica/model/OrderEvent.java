package com.github.jokicmilica.model;

import java.time.Instant;

public record OrderEvent(
        String orderId,
        String itemId,
        int quantity,
        OrderStatus status,
        Instant createdAt
) {}

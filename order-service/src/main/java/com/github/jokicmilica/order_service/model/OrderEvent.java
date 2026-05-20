package com.github.jokicmilica.order_service.model;

import java.time.Instant;

public record OrderEvent(
        String orderId,
        String itemId,
        int quantity,
        String status,
        Instant createdAt
) {}

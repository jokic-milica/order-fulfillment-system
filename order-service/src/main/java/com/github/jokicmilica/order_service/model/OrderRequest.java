package com.github.jokicmilica.order_service.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record OrderRequest(
        @NotBlank(message = "orderId is required")
        String orderId,

        @NotBlank(message = "itemId is required")
        String itemId,

        @Positive(message = "quantity must be positive")
        int quantity
) {}

package com.github.jokicmilica.order_service.exception;

import java.time.Instant;

public record ErrorMessage(
        int statusCode,
        Instant timestamp,
        String message,
        String description
) {}

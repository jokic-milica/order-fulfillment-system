package com.github.jokicmilica.exception;

import java.time.Instant;

public record ErrorMessage(
        int statusCode,
        Instant timestamp,
        String message
) {}

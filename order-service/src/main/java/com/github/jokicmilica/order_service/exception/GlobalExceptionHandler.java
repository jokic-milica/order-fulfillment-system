package com.github.jokicmilica.order_service.exception;

import com.github.jokicmilica.exception.ErrorMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(com.github.jokicmilica.exception.KafkaProducerException.class)
    public ResponseEntity<ErrorMessage> handleKafkaProducerException(com.github.jokicmilica.exception.KafkaProducerException ex) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorMessage(
                        503,
                        Instant.now(),
                        "Unable to process order, please try again later"
                ));
    }

    @ExceptionHandler(DuplicateOrderException.class)
    public ResponseEntity<ErrorMessage> handleDuplicateOrder(DuplicateOrderException ex) {
        log.warn("Duplicate order: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorMessage(409, Instant.now(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessage> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity
                .badRequest()
                .body(new ErrorMessage(
                        400,
                        Instant.now(),
                        String.format("Validation failed: %s", message)
                ));
    }
}

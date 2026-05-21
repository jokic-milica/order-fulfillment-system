package com.github.jokicmilica.order_service.exception;

public class DuplicateOrderException extends RuntimeException {
    public DuplicateOrderException (String message) {super(message);}
}

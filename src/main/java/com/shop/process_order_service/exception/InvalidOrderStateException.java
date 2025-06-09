package com.shop.process_order_service.exception;

public class InvalidOrderStateException extends BusinessException {
    public InvalidOrderStateException(String message) {
        super(message, "INVALID_ORDER_STATE");
    }
    
    public InvalidOrderStateException(String message, Object... args) {
        super(message, "INVALID_ORDER_STATE", args);
    }
}
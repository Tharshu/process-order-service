package com.shop.process_order_service.exception;

public class CustomerNotFoundException extends BusinessException {
    public CustomerNotFoundException(String message) {
        super(message, "CUSTOMER_NOT_FOUND");
    }
    
    public CustomerNotFoundException(String message, Object... args) {
        super(message, "CUSTOMER_NOT_FOUND", args);
    }
}
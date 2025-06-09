package com.shop.process_order_service.exception;

import lombok.Getter;

@Getter
public abstract class BusinessException extends RuntimeException {
    private final String errorCode;
    private final Object[] args;
    
    protected BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.args = new Object[0];
    }
    
    protected BusinessException(String message, String errorCode, Object... args) {
        super(message);
        this.errorCode = errorCode;
        this.args = args;
    }
    
    protected BusinessException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.args = new Object[0];
    }
}
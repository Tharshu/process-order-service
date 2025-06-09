package com.shop.process_order_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StandardApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private List<ValidationError> errors;
    private String errorCode;
    private LocalDateTime timestamp;
    private String path;
    
    public static <T> StandardApiResponse<T> success(T data) {
        return StandardApiResponse.<T>builder()
                .success(true)
                .message("Operation completed successfully")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static <T> StandardApiResponse<T> success(T data, String message) {
        return StandardApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static <T> StandardApiResponse<T> error(String message, String errorCode) {
        return StandardApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static <T> StandardApiResponse<T> validationError(List<ValidationError> errors) {
        return StandardApiResponse.<T>builder()
                .success(false)
                .message("Validation failed")
                .errors(errors)
                .errorCode("VALIDATION_ERROR")
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    @Data
    @Builder
    public static class ValidationError {
        private String field;
        private String message;
        private Object rejectedValue;
    }
}
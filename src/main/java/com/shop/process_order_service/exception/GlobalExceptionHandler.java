package com.shop.process_order_service.exception;

import com.shop.process_order_service.dto.StandardApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<StandardApiResponse<Void>> handleOrderNotFoundException(
            OrderNotFoundException ex, HttpServletRequest request) {
        log.error("Order not found: ", ex);
        StandardApiResponse<Void> response = StandardApiResponse.<Void>builder()
                .success(false)
                .message(ex.getMessage())
                .errorCode("ORDER_NOT_FOUND")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<StandardApiResponse<Void>> handleCustomerNotFound(CustomerNotFoundException e) {
        log.error("Customer not found: ", e);
        StandardApiResponse<Void> error = StandardApiResponse.<Void>builder()
            .timestamp(LocalDateTime.now())
            .success(false)
            .errorCode("CUSTOMER_NOT_FOUND")
            .message(e.getMessage())
            .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
}


    @ExceptionHandler(QueueFullException.class)
    public ResponseEntity<StandardApiResponse<Void>> handleQueueFull(QueueFullException e) {
        log.error("Queue full: ", e);
        StandardApiResponse<Void>  error = StandardApiResponse.<Void>builder()
                .timestamp(LocalDateTime.now())
                .success(false)
                .errorCode("QUEUE_FULL")
                .message(e.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardApiResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        StandardApiResponse<Void> error = StandardApiResponse.<Void>builder()
                .timestamp(LocalDateTime.now())
                .success(false)
                .errorCode("VALIDATION_FAILED")
                .message("Invalid input parameters")
                .build();
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardApiResponse<Void>> handleGenericException(
            Exception ex, HttpServletRequest request) {
        log.error("Unexpected error: ", ex);
        StandardApiResponse<Void> response = StandardApiResponse.<Void>builder()
                .success(false)
                .message("An unexpected error occurred: " + ex.getMessage())
                .errorCode("INTERNAL_SERVER_ERROR")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<StandardApiResponse<Void>> handleTooManyRequests(TooManyRequestsException e) {
        log.error("Too many requests: ", e);
        StandardApiResponse<Void>  error = StandardApiResponse.<Void>builder()
                .timestamp(LocalDateTime.now())
                .success(false)
                .errorCode("TOO_MANY_REQUESTS")
                .message("Request limit exceeded. Please try again later")
                .build();
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
    }
}


package com.shop.process_order_service.controller;

import com.shop.process_order_service.dto.*;
import com.shop.process_order_service.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Order Management", description = "APIs for processing customer orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Process a new order", description = "Creates a new order and adds it to the queue")
    public ResponseEntity<StandardApiResponse<OrderResponseDto>> createOrder(
            @Valid @RequestBody OrderRequestDto request,
            HttpServletRequest httpRequest) {
        
        log.info("Processing order request for customer: {}, shop: {}", 
                request.getCustomerId(), request.getCoffeeShopId());
        
        OrderResponseDto response = orderService.processOrder(request);
        
        StandardApiResponse<OrderResponseDto> apiResponse = StandardApiResponse
                .success(response, "Order processed successfully");
        apiResponse.setPath(httpRequest.getRequestURI());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order details", description = "Retrieves details of a specific order")
    public ResponseEntity<StandardApiResponse<OrderResponseDto>> getOrder(
            @PathVariable @Positive Long orderId,
            HttpServletRequest httpRequest) {
        log.info("Received request to get order with id: {}", orderId);
        try {
            OrderResponseDto orderRes = orderService.getOrder(orderId);
            log.info("Successfully retrieved order: {}", orderRes);
            StandardApiResponse<OrderResponseDto> response = StandardApiResponse.<OrderResponseDto>builder()
                    .success(true)
                    .data(orderRes)
                    .message("Order retrieved successfully")
                    .timestamp(LocalDateTime.now())
                    .path(httpRequest.getRequestURI())
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error occurred while getting order: ", e);
            throw e;
        }
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get customer orders", description = "Retrieves all orders for a specific customer")
    public ResponseEntity<StandardApiResponse<List<OrderResponseDto>>> getOrdersByCustomer(
            @Parameter(description = "Customer ID", required = true)
            @PathVariable @Positive Long customerId,
            HttpServletRequest httpRequest) {
        
        List<OrderResponseDto> orders = orderService.getCustomerOrders(customerId);
        
        StandardApiResponse<List<OrderResponseDto>> apiResponse = StandardApiResponse
                .success(orders, String.format("Retrieved %d orders for customer", orders.size()));
        apiResponse.setPath(httpRequest.getRequestURI());
        
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{orderId}/queue-status")
    @Operation(summary = "Get queue position", description = "Gets the current queue position for an order")
    public ResponseEntity<StandardApiResponse<QueuePositionDto>> getOrderQueueStatus(
            @Parameter(description = "Order ID", required = true)
            @PathVariable @Positive Long orderId,
            @Parameter(description = "Customer ID", required = true)
            @RequestParam @Positive Long customerId,
            HttpServletRequest httpRequest) {
        
        QueuePositionDto position = orderService.getQueuePosition(orderId, customerId);
        
        StandardApiResponse<QueuePositionDto> apiResponse = StandardApiResponse
                .success(position, "Queue position retrieved successfully");
        apiResponse.setPath(httpRequest.getRequestURI());
        
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{orderId}")
    @Operation(summary = "Cancel order", description = "Cancels an existing order")
    public ResponseEntity<StandardApiResponse<Void>> cancelOrder(
            @Parameter(description = "Order ID", required = true)
            @PathVariable @Positive Long orderId,
            @Parameter(description = "Customer ID", required = true)
            @RequestParam @Positive Long customerId,
            HttpServletRequest httpRequest) {
        
        orderService.cancelOrder(orderId, customerId);
        
        StandardApiResponse<Void> apiResponse = StandardApiResponse
                .success(null, "Order cancelled successfully");
        apiResponse.setPath(httpRequest.getRequestURI());
        
        return ResponseEntity.ok(apiResponse);
    }
}
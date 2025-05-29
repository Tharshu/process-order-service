package com.shop.process_order_service.controller;


import com.shop.process_order_service.dto.OrderRequestDto;
import com.shop.process_order_service.dto.OrderResponseDto;
import com.shop.process_order_service.dto.QueuePositionDto;
import com.shop.process_order_service.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order Management", description = "APIs for processing customer orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Process a new order", description = "Creates a new order and adds it to the queue")
    public ResponseEntity<OrderResponseDto> processOrder(@Valid @RequestBody OrderRequestDto request) {
        try {
            log.info("Received order request: {}", request);
            OrderResponseDto response = orderService.processOrder(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error processing order: ", e);
            throw e;
        }
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order details", description = "Retrieves details of a specific order")
    public ResponseEntity<OrderResponseDto> getOrder(@PathVariable Long orderId) {
        OrderResponseDto response = orderService.getOrder(orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get customer orders", description = "Retrieves all orders for a specific customer")
    public ResponseEntity<List<OrderResponseDto>> getCustomerOrders(@PathVariable Long customerId) {
        List<OrderResponseDto> orders = orderService.getCustomerOrders(customerId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}/queue-position")
    @Operation(summary = "Get queue position", description = "Gets the current queue position for an order")
    public ResponseEntity<QueuePositionDto> getQueuePosition(
            @PathVariable Long orderId,
            @RequestParam Long customerId) {
        QueuePositionDto position = orderService.getQueuePosition(orderId, customerId);
        return ResponseEntity.ok(position);
    }

    @DeleteMapping("/{orderId}")
    @Operation(summary = "Cancel order", description = "Cancels an existing order")
    public ResponseEntity<Void> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam Long customerId) {
        orderService.cancelOrder(orderId, customerId);
        return ResponseEntity.noContent().build();
    }
}

package com.shop.process_order_service.controller;

import com.shop.process_order_service.dto.CustomerResponseDto;
import com.shop.process_order_service.dto.OrderResponseDto;
import com.shop.process_order_service.dto.StandardApiResponse;
import com.shop.process_order_service.exception.CustomerNotFoundException;
import com.shop.process_order_service.service.CustomerService;
import com.shop.process_order_service.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@Slf4j
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Validated
@Tag(name = "Customer Management", description = "APIs for managing customers and their related data")
public class CustomerController {

    private final OrderService orderService;
    private final CustomerService customerService;

    @GetMapping("/{customerId}/history")
    @Operation(summary = "Get customer order history", description = "Retrieves all orders for a specific customer")
    public ResponseEntity<StandardApiResponse<List<OrderResponseDto>>> getCustomerOrderHistory(
            @Parameter(description = "Customer ID", required = true)
            @PathVariable @Positive Long customerId,
            HttpServletRequest httpRequest) {

        try {
            List<OrderResponseDto> orders = orderService.getCustomerOrders(customerId);

            StandardApiResponse<List<OrderResponseDto>> apiResponse = StandardApiResponse
                    .success(orders, String.format("Retrieved %d orders for customer", orders.size()));
            apiResponse.setPath(httpRequest.getRequestURI());

            return ResponseEntity.ok(apiResponse);
        } catch (CustomerNotFoundException e) {
            log.error("No orders found for customer ID: {}", customerId, e);
            StandardApiResponse<List<OrderResponseDto>> apiResponse = StandardApiResponse
                    .error("No orders found for customer", "CUSTOMER_NOT_FOUND");
            apiResponse.setPath(httpRequest.getRequestURI());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
        }
    }

    @GetMapping
    public ResponseEntity<StandardApiResponse<List<CustomerResponseDto>>> getAllCustomers() {
        List<CustomerResponseDto> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(StandardApiResponse.success(customers));
    }
}


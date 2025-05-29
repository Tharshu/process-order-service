package com.shop.process_order_service.controller;

import com.shop.process_order_service.dto.ApiResponse;
import com.shop.process_order_service.dto.OrderResponseDto;
import com.shop.process_order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final OrderService orderService;

    @GetMapping("/{id}/history")
    public ResponseEntity<ApiResponse<List<OrderResponseDto>>> getOrderHistory(@PathVariable Long id) {
        List<OrderResponseDto> history = orderService.getCustomerOrders(id);
        return ResponseEntity.ok(ApiResponse.success(history));
    }
}


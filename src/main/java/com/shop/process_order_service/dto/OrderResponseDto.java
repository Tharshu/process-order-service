package com.shop.process_order_service.dto;

import lombok.Data;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.shop.process_order_service.entity.OrderStatus;

@Data
@Builder
public class OrderResponseDto {
    private Long orderId;
    private String customerName;
    private String coffeeShopName;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private Integer queuePosition;
    private Integer estimatedWaitTime;
    private List<OrderItemResponseDto> items;
    private LocalDateTime createdAt;
    
}


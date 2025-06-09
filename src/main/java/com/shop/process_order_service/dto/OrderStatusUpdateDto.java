package com.shop.process_order_service.dto;

import com.shop.process_order_service.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderStatusUpdateDto {
    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "New status is required")
    private OrderStatus newStatus;
}


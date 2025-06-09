package com.shop.process_order_service.dto;


import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Data
public class OrderRequestDto {
    @NotNull(message = "Customer ID is required")
    private Long customerId;
    
    @NotNull(message = "Coffee shop ID is required")
    private Long coffeeShopId;
    
    @NotEmpty(message = "Order items cannot be empty")
    private List<OrderItemDto> items;

}

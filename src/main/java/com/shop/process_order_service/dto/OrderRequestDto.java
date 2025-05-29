package com.shop.process_order_service.dto;


import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.util.List;

@Data
public class OrderRequestDto {
    @NotNull(message = "Customer ID is required")
    private Long customerId;
    
    @NotNull(message = "Coffee shop ID is required")
    private Long coffeeShopId;
    
    @NotEmpty(message = "Order items cannot be empty")
    private List<OrderItemDto> items;
    
    @Data
    public static class OrderItemDto {
        @NotNull(message = "Menu item ID is required")
        private Long menuItemId;
        
        @Positive(message = "Quantity must be positive")
        private Integer quantity;
        
        private String notes;
    }
}

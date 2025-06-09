package com.shop.process_order_service.dto;


import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class OrderItemDto {

    @NotNull(message = "Menu item ID is required")
    private Long menuItemId;

    @NotBlank
    private String itemName;

    @Min(1)
    @Positive(message = "Quantity must be positive")
    private int quantity;

    private String notes;

    @DecimalMin(value = "0.0", inclusive = false)
    private double price;
}


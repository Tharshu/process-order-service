package com.shop.process_order_service.dto;


import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class OrderItemDTO {
    @NotBlank
    private String itemName;

    @Min(1)
    private int quantity;

    @DecimalMin(value = "0.0", inclusive = false)
    private double price;
}


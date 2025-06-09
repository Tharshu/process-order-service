package com.shop.process_order_service.dto;

import jakarta.persistence.Column;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
public class CustomerResponseDto {
    private int id;
    private String name;
    private String email;
    private String phone;
    private String homeAddress;
    private String workAddress;
}

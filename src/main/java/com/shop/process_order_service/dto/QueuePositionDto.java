package com.shop.process_order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueuePositionDto {
    private Long orderId;
    private Integer currentPosition;
    private Integer totalInQueue;
    private Integer estimatedWaitTime;
    private String status;
}

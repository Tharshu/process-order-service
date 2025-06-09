package com.shop.process_order_service.controller;


import com.shop.process_order_service.dto.OrderStatusUpdateDto;
import com.shop.process_order_service.dto.StandardApiResponse;
import com.shop.process_order_service.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/shops")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Shop Management", description = "APIs for managing shop-related operations")
public class ShopController {

    private final OrderService orderService;

    @PutMapping("/{shopId}/orders/status")
    @Operation(summary = "Update order status", description = "Updates the status of order for a specific shop")
    public ResponseEntity<StandardApiResponse<String>> updateOrderStatuses(
            @PathVariable Long shopId,
            @RequestBody @Validated OrderStatusUpdateDto updates,
            HttpServletRequest httpRequest) {
        log.info("Updating order status for shop: {}", shopId);
        orderService.updateOrderStatuses(shopId, updates);
        StandardApiResponse<Void> apiResponse = StandardApiResponse
                .success(null, "Order status updated successfully");
        apiResponse.setPath(httpRequest.getRequestURI());
        return ResponseEntity.status(HttpStatusCode.valueOf(202)).build();
    }
}


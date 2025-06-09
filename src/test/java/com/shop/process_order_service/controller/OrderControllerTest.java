package com.shop.process_order_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.process_order_service.dto.*;
import com.shop.process_order_service.entity.OrderStatus;
import com.shop.process_order_service.service.OrderService;

import lombok.Data;
import lombok.Builder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    enum OrderStatus {
        PENDING, PROCESSING, COMPLETED, CANCELLED
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private OrderRequestDto validOrderRequest;
    private OrderResponseDto mockOrderResponse;
    private QueuePositionDto mockQueuePosition;

    @BeforeEach
    void setUp() {
        // Setup valid order request
        OrderItemDto orderItem = new OrderItemDto();
        orderItem.setMenuItemId(1L);
        orderItem.setQuantity(2);
        orderItem.setPrice(4.50);

        validOrderRequest = new OrderRequestDto();
        validOrderRequest.setCustomerId(1L);
        validOrderRequest.setCoffeeShopId(1L);
        validOrderRequest.setItems(Arrays.asList(orderItem));

        // Setup mock order response
        OrderItemResponseDto responseItem = OrderItemResponseDto.builder()
                .itemName("Espresso")
                .notes("TEST")
                .quantity(2)
                .totalPrice(BigDecimal.valueOf(9.00))
                .unitPrice(BigDecimal.valueOf(4.50))
                .build();

        mockOrderResponse = OrderResponseDto.builder()
                .orderId(1L)
                .customerName("John Doe")
                .coffeeShopName("Coffee Central")
                .status(com.shop.process_order_service.entity.OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(9.00))
                .queuePosition(5)
                .estimatedWaitTime(15)
                .items(Arrays.asList(responseItem))
                .createdAt(LocalDateTime.now())
                .build();

        // Setup mock queue position
        mockQueuePosition = QueuePositionDto.builder()
                .orderId(1L)
                .currentPosition(3)
                .totalInQueue(10)
                .estimatedWaitTime(12)
                .status("PENDING")
                .build();
    }

    @Test
    void processOrder_ValidRequest_ShouldReturnCreatedStatus() throws Exception {
        // Given
        when(orderService.processOrder(any(OrderRequestDto.class))).thenReturn(mockOrderResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validOrderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Order processed successfully"))
                .andExpect(jsonPath("$.data.orderId").value(1L))
                .andExpect(jsonPath("$.data.customerName").value("John Doe"))
                .andExpect(jsonPath("$.data.coffeeShopName").value("Coffee Central"))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.totalAmount").value(9.00))
                .andExpect(jsonPath("$.data.queuePosition").value(5))
                .andExpect(jsonPath("$.data.estimatedWaitTime").value(15))
                .andExpect(jsonPath("$.path").value("/api/v1/orders"));

        verify(orderService, times(1)).processOrder(any(OrderRequestDto.class));
    }

    @Test
    void processOrder_InvalidRequest_MissingCustomerId_ShouldReturnBadRequest() throws Exception {
        // Given
        validOrderRequest.setCustomerId(null);

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validOrderRequest)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).processOrder(any(OrderRequestDto.class));
    }

    @Test
    void processOrder_InvalidRequest_MissingCoffeeShopId_ShouldReturnBadRequest() throws Exception {
        // Given
        validOrderRequest.setCoffeeShopId(null);

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validOrderRequest)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).processOrder(any(OrderRequestDto.class));
    }

    @Test
    void processOrder_InvalidRequest_EmptyItems_ShouldReturnBadRequest() throws Exception {
        // Given
        validOrderRequest.setItems(Collections.emptyList());

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validOrderRequest)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).processOrder(any(OrderRequestDto.class));
    }

    @Test
    void processOrder_InvalidRequest_NullItems_ShouldReturnBadRequest() throws Exception {
        // Given
        validOrderRequest.setItems(null);

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validOrderRequest)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).processOrder(any(OrderRequestDto.class));
    }

    @Test
    void processOrder_ServiceException_ShouldPropagateException() throws Exception {
        // Given
        when(orderService.processOrder(any(OrderRequestDto.class)))
                .thenThrow(new RuntimeException("Service unavailable"));

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validOrderRequest)))
                .andExpect(status().isInternalServerError());

        verify(orderService, times(1)).processOrder(any(OrderRequestDto.class));
    }

    @Test
    void getOrder_ValidOrderId_ShouldReturnOrderDetails() throws Exception {
        // Given
        when(orderService.getOrder(1L)).thenReturn(mockOrderResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Order retrieved successfully"))
                .andExpect(jsonPath("$.data.orderId").value(1L))
                .andExpect(jsonPath("$.data.customerName").value("John Doe"))
                .andExpect(jsonPath("$.data.coffeeShopName").value("Coffee Central"))
                .andExpect(jsonPath("$.path").value("/api/v1/orders/1"));

        verify(orderService, times(1)).getOrder(1L);
    }

    @Test
    void getOrder_ServiceException_ShouldPropagateException() throws Exception {
        // Given
        when(orderService.getOrder(1L)).thenThrow(new RuntimeException("Order not found"));

        // When & Then
        mockMvc.perform(get("/api/v1/orders/1"))
                .andExpect(status().isInternalServerError());

        verify(orderService, times(1)).getOrder(1L);
    }

    @Test
    void getCustomerOrders_ValidCustomerId_ShouldReturnOrdersList() throws Exception {
        // Given
        List<OrderResponseDto> orders = Arrays.asList(mockOrderResponse);
        when(orderService.getCustomerOrders(1L)).thenReturn(orders);

        // When & Then
        mockMvc.perform(get("/api/v1/orders/customer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Retrieved 1 orders for customer"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].orderId").value(1L))
                .andExpect(jsonPath("$.data[0].customerName").value("John Doe"))
                .andExpect(jsonPath("$.path").value("/api/v1/orders/customer/1"));

        verify(orderService, times(1)).getCustomerOrders(1L);
    }

    @Test
    void getCustomerOrders_ValidCustomerId_EmptyList_ShouldReturnEmptyList() throws Exception {
        // Given
        when(orderService.getCustomerOrders(1L)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/v1/orders/customer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Retrieved 0 orders for customer"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.path").value("/api/v1/orders/customer/1"));

        verify(orderService, times(1)).getCustomerOrders(1L);
    }

    @Test
    void getCustomerOrders_ServiceException_ShouldPropagateException() throws Exception {
        // Given
        when(orderService.getCustomerOrders(1L)).thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(get("/api/v1/orders/customer/1"))
                .andExpect(status().isInternalServerError());

        verify(orderService, times(1)).getCustomerOrders(1L);
    }

    @Test
    void getQueuePosition_ValidParameters_ShouldReturnQueuePosition() throws Exception {
        // Given
        when(orderService.getQueuePosition(1L, 1L)).thenReturn(mockQueuePosition);

        // When & Then
        mockMvc.perform(get("/api/v1/orders/1/queue-position")
                        .param("customerId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Queue position retrieved successfully"))
                .andExpect(jsonPath("$.data.orderId").value(1L))
                .andExpect(jsonPath("$.data.currentPosition").value(3))
                .andExpect(jsonPath("$.data.totalInQueue").value(10))
                .andExpect(jsonPath("$.data.estimatedWaitTime").value(12))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.path").value("/api/v1/orders/1/queue-position"));

        verify(orderService, times(1)).getQueuePosition(1L, 1L);
    }

    @Test
    void getQueuePosition_MissingCustomerId_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/orders/1/queue-position"))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).getQueuePosition(anyLong(), anyLong());
    }

    @Test
    void getQueuePosition_ServiceException_ShouldPropagateException() throws Exception {
        // Given
        when(orderService.getQueuePosition(1L, 1L)).thenThrow(new RuntimeException("Queue service unavailable"));

        // When & Then
        mockMvc.perform(get("/api/v1/orders/1/queue-position")
                        .param("customerId", "1"))
                .andExpect(status().isInternalServerError());

        verify(orderService, times(1)).getQueuePosition(1L, 1L);
    }

    @Test
    void cancelOrder_ValidParameters_ShouldReturnSuccessMessage() throws Exception {
        // Given
        doNothing().when(orderService).cancelOrder(1L, 1L);

        // When & Then
        mockMvc.perform(delete("/api/v1/orders/1")
                        .param("customerId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Order cancelled successfully"))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.path").value("/api/v1/orders/1"));

        verify(orderService, times(1)).cancelOrder(1L, 1L);
    }

    @Test
    void cancelOrder_MissingCustomerId_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/orders/1"))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).cancelOrder(anyLong(), anyLong());
    }

    @Test
    void cancelOrder_ServiceException_ShouldPropagateException() throws Exception {
        // Given
        doThrow(new RuntimeException("Order cannot be cancelled")).when(orderService).cancelOrder(1L, 1L);

        // When & Then
        mockMvc.perform(delete("/api/v1/orders/1")
                        .param("customerId", "1"))
                .andExpect(status().isInternalServerError());

        verify(orderService, times(1)).cancelOrder(1L, 1L);
    }
}

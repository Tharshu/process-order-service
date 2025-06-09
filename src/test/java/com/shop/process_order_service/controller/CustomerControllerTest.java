package com.shop.process_order_service.controller;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.shop.process_order_service.dto.OrderResponseDto;
import com.shop.process_order_service.dto.OrderItemResponseDto;
import com.shop.process_order_service.dto.StandardApiResponse;
import com.shop.process_order_service.entity.OrderStatus;
import com.shop.process_order_service.exception.CustomerNotFoundException;
import com.shop.process_order_service.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {
    @Mock
    private OrderService orderService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private CustomerController customerController; // Update this to match your actual controller class name

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(customerController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // For LocalDateTime serialization
    }

    @Test
    void getCustomerOrderHistory_Success_WithMultipleOrders() throws Exception {
        // Arrange
        Long customerId = 1L;
        List<OrderResponseDto> mockOrders = createMockOrders();
        String expectedPath = "/customers/1/history";

        when(orderService.getCustomerOrders(customerId)).thenReturn(mockOrders);
        when(httpServletRequest.getRequestURI()).thenReturn(expectedPath);

        // Act
        ResponseEntity<StandardApiResponse<List<OrderResponseDto>>> response =
                customerController.getCustomerOrderHistory(customerId, httpServletRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        StandardApiResponse<List<OrderResponseDto>> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
        assertEquals(mockOrders, body.getData());
        assertEquals("Retrieved 3 orders for customer", body.getMessage());
        assertEquals(expectedPath, body.getPath());

        verify(orderService).getCustomerOrders(customerId);
        verify(httpServletRequest).getRequestURI();
    }

    @Test
    void getCustomerOrderHistory_Success_WithEmptyOrderList() throws Exception {
        // Arrange
        Long customerId = 2L;
        List<OrderResponseDto> emptyOrders = Collections.emptyList();
        String expectedPath = "/customers/2/history";

        when(orderService.getCustomerOrders(customerId)).thenReturn(emptyOrders);
        when(httpServletRequest.getRequestURI()).thenReturn(expectedPath);

        // Act
        ResponseEntity<StandardApiResponse<List<OrderResponseDto>>> response =
                customerController.getCustomerOrderHistory(customerId, httpServletRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        StandardApiResponse<List<OrderResponseDto>> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
        assertEquals(emptyOrders, body.getData());
        assertEquals("Retrieved 0 orders for customer", body.getMessage());
        assertEquals(expectedPath, body.getPath());

        verify(orderService).getCustomerOrders(customerId);
        verify(httpServletRequest).getRequestURI();
    }

    @Test
    void getCustomerOrderHistory_CustomerNotFound_ReturnsNotFound() throws Exception {
        // Arrange
        Long customerId = 999L;
        String expectedPath = "/customers/999/history";
        CustomerNotFoundException exception = new CustomerNotFoundException("Customer not found");

        when(orderService.getCustomerOrders(customerId)).thenThrow(exception);
        when(httpServletRequest.getRequestURI()).thenReturn(expectedPath);

        // Act
        ResponseEntity<StandardApiResponse<List<OrderResponseDto>>> response =
                customerController.getCustomerOrderHistory(customerId, httpServletRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        StandardApiResponse<List<OrderResponseDto>> body = response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertNull(body.getData());
        assertEquals("No orders found for customer", body.getMessage());
        assertEquals("CUSTOMER_NOT_FOUND", body.getErrorCode());
        assertEquals(expectedPath, body.getPath());

        verify(orderService).getCustomerOrders(customerId);
        verify(httpServletRequest).getRequestURI();
    }

    @Test
    void getCustomerOrderHistory_Integration_WithMockMvc_Success() throws Exception {
        // This test uses MockMvc for full integration testing
        Long customerId = 1L;
        List<OrderResponseDto> mockOrders = createMockOrders();

        when(orderService.getCustomerOrders(customerId)).thenReturn(mockOrders);

        mockMvc.perform(get("/customers/{id}/history", customerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Retrieved 3 orders for customer"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].orderId").value(1))
                .andExpect(jsonPath("$.data[0].totalAmount").value(99.99))
                .andExpect(jsonPath("$.data[0].customerName").value("John Doe"))
                .andExpect(jsonPath("$.data[0].coffeeShopName").value("Coffee Central"))
                .andExpect(jsonPath("$.data[0].queuePosition").value(1))
                .andExpect(jsonPath("$.data[0].estimatedWaitTime").value(15))
                .andExpect(jsonPath("$.data[0].estimatedWaitTime").value(15))
                .andExpect(jsonPath("$.data[1].orderId").value(2))
                .andExpect(jsonPath("$.data[2].orderId").value(3));

        verify(orderService).getCustomerOrders(customerId);
    }

    @Test
    void getCustomerOrderHistory_Integration_WithMockMvc_CustomerNotFound() throws Exception {
        Long customerId = 999L;
        CustomerNotFoundException exception = new CustomerNotFoundException("Customer not found");

        when(orderService.getCustomerOrders(customerId)).thenThrow(exception);

        mockMvc.perform(get("/customers/{id}/history", customerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("No orders found for customer"))
                .andExpect(jsonPath("$.errorCode").value("CUSTOMER_NOT_FOUND"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(orderService).getCustomerOrders(customerId);
    }

    @Test
    void getCustomerOrderHistory_VerifyMethodInteractions() {
        // Arrange
        Long customerId = 1L;
        List<OrderResponseDto> mockOrders = Arrays.asList(createSingleOrder());
        String expectedPath = "/customers/1/history";

        when(orderService.getCustomerOrders(customerId)).thenReturn(mockOrders);
        when(httpServletRequest.getRequestURI()).thenReturn(expectedPath);

        // Act
        customerController.getCustomerOrderHistory(customerId, httpServletRequest);

        // Assert - Verify all method calls
        verify(orderService, times(1)).getCustomerOrders(customerId);
        verify(httpServletRequest, times(1)).getRequestURI();
        verifyNoMoreInteractions(orderService, httpServletRequest);
    }

    @Test
    void getCustomerOrderHistory_EdgeCase_LargeCustomerId() {
        // Arrange
        Long largeCustomerId = Long.MAX_VALUE;
        List<OrderResponseDto> mockOrders = Collections.singletonList(createSingleOrder());
        String expectedPath = "/customers/" + largeCustomerId + "/history";

        when(orderService.getCustomerOrders(largeCustomerId)).thenReturn(mockOrders);
        when(httpServletRequest.getRequestURI()).thenReturn(expectedPath);

        // Act
        ResponseEntity<StandardApiResponse<List<OrderResponseDto>>> response =
                customerController.getCustomerOrderHistory(largeCustomerId, httpServletRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Retrieved 1 orders for customer", response.getBody().getMessage());
    }

    @Test
    void getCustomerOrderHistory_ValidateResponseStructure() {
        // Arrange
        Long customerId = 1L;
        List<OrderResponseDto> mockOrders = createMockOrders();
        String expectedPath = "/customers/1/history";

        when(orderService.getCustomerOrders(customerId)).thenReturn(mockOrders);
        when(httpServletRequest.getRequestURI()).thenReturn(expectedPath);

        // Act
        ResponseEntity<StandardApiResponse<List<OrderResponseDto>>> response =
                customerController.getCustomerOrderHistory(customerId, httpServletRequest);

        // Assert - Validate complete response structure
        StandardApiResponse<List<OrderResponseDto>> body = response.getBody();
        assertNotNull(body.getData());
        assertNotNull(body.getMessage());
        assertNotNull(body.getPath());
        assertTrue(body.getMessage().contains("Retrieved"));
        assertTrue(body.getMessage().contains("orders for customer"));
        assertEquals(mockOrders.size(), body.getData().size());
    }

    // Helper methods for creating test data
    private List<OrderResponseDto> createMockOrders() {
        return Arrays.asList(
                createOrderDto(1L, BigDecimal.valueOf(99.99), OrderStatus.COMPLETED),
                createOrderDto(2L, BigDecimal.valueOf(149.50), OrderStatus.PENDING),
                createOrderDto(3L, BigDecimal.valueOf(75.25), OrderStatus.PROCESSING)
        );
    }

    private OrderResponseDto createSingleOrder() {
        return createOrderDto(1L, BigDecimal.valueOf(99.99), OrderStatus.COMPLETED);
    }

    private OrderResponseDto createOrderDto(Long orderId, BigDecimal amount, OrderStatus status) {
        OrderResponseDto orderDto = OrderResponseDto.builder()
                .orderId(orderId)
                .totalAmount(amount)
                .status(status)
                .createdAt(LocalDateTime.now())
                .customerName("John Doe")
                .coffeeShopName("Coffee Central")
                .queuePosition(1)
                .estimatedWaitTime(15)
                .items(createMockOrderItems())
                .build();
        return orderDto;
    }

    private List<OrderItemResponseDto> createMockOrderItems() {
        OrderItemResponseDto item1 = OrderItemResponseDto.builder().itemName("Espresso").quantity(1).unitPrice(BigDecimal.valueOf(3.50)).totalPrice(BigDecimal.valueOf(3.50)).notes("Regular").build();
        OrderItemResponseDto item2 = OrderItemResponseDto.builder().itemName("Cappuccino").quantity(1).unitPrice(BigDecimal.valueOf(4.00)).totalPrice(BigDecimal.valueOf(4.00)).notes("With milk").build();

        return Arrays.asList(item1, item2);
    }
}

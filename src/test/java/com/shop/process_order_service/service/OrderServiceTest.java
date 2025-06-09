package com.shop.process_order_service.service;

import com.shop.process_order_service.dto.*;
import com.shop.process_order_service.entity.*;
import com.shop.process_order_service.exception.*;
import com.shop.process_order_service.mapper.OrderMapper;
import com.shop.process_order_service.repository.CoffeeShopRepository;
import com.shop.process_order_service.repository.CustomerRepository;
import com.shop.process_order_service.repository.MenuItemRepository;
import com.shop.process_order_service.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private CoffeeShopRepository coffeeShopRepository;
    @Mock
    private MenuItemRepository menuItemRepository;
    @Mock
    private QueueService queueService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    private Customer testCustomer;
    private CoffeeShop testCoffeeShop;
    private MenuItem testMenuItem;
    private Order testOrder;
    private OrderRequestDto testOrderRequest;
    private OrderItemDto testOrderItemDto;
    private OrderResponseDto testOrderResponse;

    @BeforeEach
    void setUp() {
        // Setup test data
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setName("John Doe");
        testCustomer.setMobileNumber("1234567890");
        testCustomer.setLoyaltyScore(5);

        testCoffeeShop = new CoffeeShop();
        testCoffeeShop.setId(1L);
        testCoffeeShop.setName("Test Coffee Shop");
        testCoffeeShop.setMaxQueueSize(10);

        testMenuItem = new MenuItem();
        testMenuItem.setId(1L);
        testMenuItem.setName("Cappuccino");
        testMenuItem.setPrice(new BigDecimal("4.50"));
        testMenuItem.setAvailable(true);

        testOrderItemDto = new OrderItemDto();
        testOrderItemDto.setMenuItemId(1L);
        testOrderItemDto.setQuantity(2);

        testOrderRequest = new OrderRequestDto();
        testOrderRequest.setCustomerId(1L);
        testOrderRequest.setCoffeeShopId(1L);
        testOrderRequest.setItems(Arrays.asList(testOrderItemDto));

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setCustomer(testCustomer);
        testOrder.setCoffeeShop(testCoffeeShop);
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setTotalAmount(new BigDecimal("9.00"));
        testOrder.setQueuePosition(1);
        testOrder.setEstimatedWaitTime(15);
        testOrder.setCreatedAt(LocalDateTime.now());

        testOrderResponse = OrderResponseDto.builder()
                .orderId(1L)
                .customerName("John Doe")
                .coffeeShopName("Test Coffee Shop")
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("9.00"))
                .queuePosition(1)
                .estimatedWaitTime(15)
                .build();
    }

    @Test
    void processOrder_Success() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(coffeeShopRepository.findById(1L)).thenReturn(Optional.of(testCoffeeShop));
        when(orderRepository.countActiveOrdersByShop(1L)).thenReturn(5);
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(testMenuItem));
        when(queueService.assignQueuePosition(1L)).thenReturn(1);
        when(queueService.calculateEstimatedWaitTime(1L, 1)).thenReturn(15);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.toItemEntity(any(OrderItemDto.class))).thenReturn(new OrderItem());
        when(orderMapper.toDto(any(Order.class))).thenReturn(testOrderResponse);

        // When
        OrderResponseDto result = orderService.processOrder(testOrderRequest);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getOrderId());
        verify(customerRepository).save(any(Customer.class));
        verify(notificationService).sendOrderConfirmation(any(Order.class));
        assertEquals(6, testCustomer.getLoyaltyScore()); // Incremented by 1
    }

    @Test
    void processOrder_CustomerNotFound() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        CustomerNotFoundException exception = assertThrows(
                CustomerNotFoundException.class,
                () -> orderService.processOrder(testOrderRequest)
        );
        assertEquals("Customer not found: 1", exception.getMessage());
    }

    @Test
    void processOrder_ShopNotFound() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(coffeeShopRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ShopNotFoundException exception = assertThrows(
                ShopNotFoundException.class,
                () -> orderService.processOrder(testOrderRequest)
        );
        assertEquals("Coffee shop not found: 1", exception.getMessage());
    }

    @Test
    void processOrder_QueueFull() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(coffeeShopRepository.findById(1L)).thenReturn(Optional.of(testCoffeeShop));
        when(orderRepository.countActiveOrdersByShop(1L)).thenReturn(10); // At max capacity

        // When & Then
        QueueFullException exception = assertThrows(
                QueueFullException.class,
                () -> orderService.processOrder(testOrderRequest)
        );
        assertTrue(exception.getMessage().contains("Queue is full"));
    }

    @Test
    void processOrder_MenuItemNotFound() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(coffeeShopRepository.findById(1L)).thenReturn(Optional.of(testCoffeeShop));
        when(orderRepository.countActiveOrdersByShop(1L)).thenReturn(5);
        when(menuItemRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderService.processOrder(testOrderRequest)
        );
        assertEquals("Menu item not found: 1", exception.getMessage());
    }

    @Test
    void processOrder_MenuItemNotAvailable() {
        // Given
        testMenuItem.setAvailable(false);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(coffeeShopRepository.findById(1L)).thenReturn(Optional.of(testCoffeeShop));
        when(orderRepository.countActiveOrdersByShop(1L)).thenReturn(5);
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(testMenuItem));

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderService.processOrder(testOrderRequest)
        );
        assertEquals("Menu item not available: Cappuccino", exception.getMessage());
    }

    @Test
    void getOrder_Success() {
        // Given
        when(orderRepository.findByIdWithOrderItems(1L)).thenReturn(Optional.of(testOrder));
        when(orderMapper.toDto(testOrder)).thenReturn(testOrderResponse);

        // When
        OrderResponseDto result = orderService.getOrder(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getOrderId());
    }

    @Test
    void getOrder_NotFound() {
        // Given
        when(orderRepository.findByIdWithOrderItems(1L)).thenReturn(Optional.empty());

        // When & Then
        OrderNotFoundException exception = assertThrows(
                OrderNotFoundException.class,
                () -> orderService.getOrder(1L)
        );
        assertEquals("Order not found with ID: 1", exception.getMessage());
    }

    @Test
    void getCustomerOrders_Success() {
        // Given
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findOrdersWithCustomerByCustomerId(1L)).thenReturn(orders);
        when(orderMapper.toDto(testOrder)).thenReturn(testOrderResponse);

        // When
        List<OrderResponseDto> result = orderService.getCustomerOrders(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getOrderId());
    }

    @Test
    void getCustomerOrders_NoOrdersFound() {
        // Given
        when(orderRepository.findOrdersWithCustomerByCustomerId(1L)).thenReturn(Collections.emptyList());

        // When & Then
        CustomerNotFoundException exception = assertThrows(
                CustomerNotFoundException.class,
                () -> orderService.getCustomerOrders(1L)
        );
        assertEquals("No orders found for customer with ID: 1", exception.getMessage());
    }

    @Test
    void getQueuePosition_ActiveOrder() {
        // Given
        when(orderRepository.findByIdAndCustomerId(1L, 1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.countActiveOrdersByShop(1L)).thenReturn(5);

        // When
        QueuePositionDto result = orderService.getQueuePosition(1L, 1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getOrderId());
        assertEquals(1, result.getCurrentPosition());
        assertEquals(5, result.getTotalInQueue());
        assertEquals(15, result.getEstimatedWaitTime());
        assertEquals("PENDING", result.getStatus());
    }

    @Test
    void getQueuePosition_CompletedOrder() {
        // Given
        testOrder.setStatus(OrderStatus.COMPLETED);
        when(orderRepository.findByIdAndCustomerId(1L, 1L)).thenReturn(Optional.of(testOrder));

        // When
        QueuePositionDto result = orderService.getQueuePosition(1L, 1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getOrderId());
        assertEquals(0, result.getCurrentPosition());
        assertEquals(0, result.getTotalInQueue());
        assertEquals(0, result.getEstimatedWaitTime());
        assertEquals("COMPLETED", result.getStatus());
    }

    @Test
    void getQueuePosition_CancelledOrder() {
        // Given
        testOrder.setStatus(OrderStatus.CANCELLED);
        when(orderRepository.findByIdAndCustomerId(1L, 1L)).thenReturn(Optional.of(testOrder));

        // When
        QueuePositionDto result = orderService.getQueuePosition(1L, 1L);

        // Then
        assertNotNull(result);
        assertEquals("CANCELLED", result.getStatus());
        assertEquals(0, result.getCurrentPosition());
    }

    @Test
    void getQueuePosition_OrderNotFound() {
        // Given
        when(orderRepository.findByIdAndCustomerId(1L, 1L)).thenReturn(Optional.empty());

        // When & Then
        OrderNotFoundException exception = assertThrows(
                OrderNotFoundException.class,
                () -> orderService.getQueuePosition(1L, 1L)
        );
        assertEquals("Order not found", exception.getMessage());
    }

    @Test
    void cancelOrder_Success() {
        // Given
        when(orderRepository.findByIdAndCustomerId(1L, 1L)).thenReturn(Optional.of(testOrder));

        // When
        orderService.cancelOrder(1L, 1L);

        // Then
        assertEquals(OrderStatus.CANCELLED, testOrder.getStatus());
        verify(orderRepository).save(testOrder);
        verify(queueService).reorderQueue(1L);
        verify(notificationService).sendOrderCancellation("1234567890", 1L);
    }

    @Test
    void cancelOrder_OrderNotFound() {
        // Given
        when(orderRepository.findByIdAndCustomerId(1L, 1L)).thenReturn(Optional.empty());

        // When & Then
        OrderNotFoundException exception = assertThrows(
                OrderNotFoundException.class,
                () -> orderService.cancelOrder(1L, 1L)
        );
        assertEquals("Order not found", exception.getMessage());
    }

    @Test
    void cancelOrder_CompletedOrder() {
        // Given
        testOrder.setStatus(OrderStatus.COMPLETED);
        when(orderRepository.findByIdAndCustomerId(1L, 1L)).thenReturn(Optional.of(testOrder));

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderService.cancelOrder(1L, 1L)
        );
        assertEquals("Cannot cancel completed order", exception.getMessage());
    }

    @Test
    void updateOrderStatuses_Success() {
        // Given
        OrderStatusUpdateDto update = new OrderStatusUpdateDto();
        update.setOrderId(1L);
        update.setNewStatus(OrderStatus.PROCESSING);

        when(coffeeShopRepository.findById(1L)).thenReturn(Optional.of(testCoffeeShop));
        when(orderRepository.findByIdInAndCoffeeShopId(1L, 1L)).thenReturn(testOrder);

        // When
        orderService.updateOrderStatuses(1L, update);

        // Then
        assertEquals(OrderStatus.PROCESSING, testOrder.getStatus());
        verify(orderRepository).save(testOrder);
    }

    @Test
    void updateOrderStatuses_CompletedStatus() {
        // Given
        OrderStatusUpdateDto update = new OrderStatusUpdateDto();
        update.setOrderId(1L);
        update.setNewStatus(OrderStatus.COMPLETED);

        when(coffeeShopRepository.findById(1L)).thenReturn(Optional.of(testCoffeeShop));
        when(orderRepository.findByIdInAndCoffeeShopId(1L, 1L)).thenReturn(testOrder);

        // When
        orderService.updateOrderStatuses(1L, update);

        // Then
        assertEquals(OrderStatus.COMPLETED, testOrder.getStatus());
        verify(queueService).reorderQueue(1L);
        verify(orderRepository).save(testOrder);
    }

    @Test
    void updateOrderStatuses_ShopNotFound() {
        // Given
        OrderStatusUpdateDto update = new OrderStatusUpdateDto();
        update.setOrderId(1L);
        update.setNewStatus(OrderStatus.PROCESSING);

        when(coffeeShopRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ShopNotFoundException exception = assertThrows(
                ShopNotFoundException.class,
                () -> orderService.updateOrderStatuses(1L, update)
        );
        assertEquals("Coffee shop not found: 1", exception.getMessage());
    }

    @Test
    void updateOrderStatuses_OrderNotFound() {
        // Given
        OrderStatusUpdateDto update = new OrderStatusUpdateDto();
        update.setOrderId(1L);
        update.setNewStatus(OrderStatus.PROCESSING);

        when(coffeeShopRepository.findById(1L)).thenReturn(Optional.of(testCoffeeShop));
        when(orderRepository.findByIdInAndCoffeeShopId(1L, 1L)).thenReturn(null);

        // When & Then
        OrderNotFoundException exception = assertThrows(
                OrderNotFoundException.class,
                () -> orderService.updateOrderStatuses(1L, update)
        );
        assertTrue(exception.getMessage().contains("No order found for shop"));
    }

    @Test
    void updateOrderStatuses_InvalidStatusTransition() {
        // Given
        testOrder.setStatus(OrderStatus.PROCESSING);
        OrderStatusUpdateDto update = new OrderStatusUpdateDto();
        update.setOrderId(1L);
        update.setNewStatus(OrderStatus.PENDING); // Invalid transition

        when(coffeeShopRepository.findById(1L)).thenReturn(Optional.of(testCoffeeShop));
        when(orderRepository.findByIdInAndCoffeeShopId(1L, 1L)).thenReturn(testOrder);

        // When & Then
        InvalidOrderStateException exception = assertThrows(
                InvalidOrderStateException.class,
                () -> orderService.updateOrderStatuses(1L, update)
        );
        assertTrue(exception.getMessage().contains("Invalid status transition"));
    }

    @Test
    void isValidStatusTransition_PendingToConfirmed() {
        // Test valid transitions from PENDING
        OrderStatusUpdateDto update1 = new OrderStatusUpdateDto();
        update1.setOrderId(1L);
        update1.setNewStatus(OrderStatus.CONFIRMED);

        when(coffeeShopRepository.findById(1L)).thenReturn(Optional.of(testCoffeeShop));
        when(orderRepository.findByIdInAndCoffeeShopId(1L, 1L)).thenReturn(testOrder);

        assertDoesNotThrow(() -> orderService.updateOrderStatuses(1L, update1));
    }

    @Test
    void isValidStatusTransition_PendingToCancelled() {
        // Test PENDING to CANCELLED
        OrderStatusUpdateDto update = new OrderStatusUpdateDto();
        update.setOrderId(1L);
        update.setNewStatus(OrderStatus.CANCELLED);

        when(coffeeShopRepository.findById(1L)).thenReturn(Optional.of(testCoffeeShop));
        when(orderRepository.findByIdInAndCoffeeShopId(1L, 1L)).thenReturn(testOrder);

        assertDoesNotThrow(() -> orderService.updateOrderStatuses(1L, update));
    }

    @Test
    void isValidStatusTransition_PendingToProcessing() {
        // Test PENDING to PROCESSING
        OrderStatusUpdateDto update = new OrderStatusUpdateDto();
        update.setOrderId(1L);
        update.setNewStatus(OrderStatus.PROCESSING);

        when(coffeeShopRepository.findById(1L)).thenReturn(Optional.of(testCoffeeShop));
        when(orderRepository.findByIdInAndCoffeeShopId(1L, 1L)).thenReturn(testOrder);

        assertDoesNotThrow(() -> orderService.updateOrderStatuses(1L, update));
    }

    @Test
    void isValidStatusTransition_PendingToCompleted() {
        // Test PENDING to COMPLETED
        OrderStatusUpdateDto update = new OrderStatusUpdateDto();
        update.setOrderId(1L);
        update.setNewStatus(OrderStatus.COMPLETED);

        when(coffeeShopRepository.findById(1L)).thenReturn(Optional.of(testCoffeeShop));
        when(orderRepository.findByIdInAndCoffeeShopId(1L, 1L)).thenReturn(testOrder);

        assertDoesNotThrow(() -> orderService.updateOrderStatuses(1L, update));
    }

    @Test
    void isValidStatusTransition_ConfirmedToProcessing() {
        // Test CONFIRMED to PROCESSING
        testOrder.setStatus(OrderStatus.CONFIRMED);
        OrderStatusUpdateDto update = new OrderStatusUpdateDto();
        update.setOrderId(1L);
        update.setNewStatus(OrderStatus.PROCESSING);

        when(coffeeShopRepository.findById(1L)).thenReturn(Optional.of(testCoffeeShop));
        when(orderRepository.findByIdInAndCoffeeShopId(1L, 1L)).thenReturn(testOrder);

        assertDoesNotThrow(() -> orderService.updateOrderStatuses(1L, update));
    }

    @Test
    void isValidStatusTransition_ConfirmedToCancelled() {
        // Test CONFIRMED to CANCELLED
        testOrder.setStatus(OrderStatus.CONFIRMED);
        OrderStatusUpdateDto update = new OrderStatusUpdateDto();
        update.setOrderId(1L);
        update.setNewStatus(OrderStatus.CANCELLED);

        when(coffeeShopRepository.findById(1L)).thenReturn(Optional.of(testCoffeeShop));
        when(orderRepository.findByIdInAndCoffeeShopId(1L, 1L)).thenReturn(testOrder);

        assertDoesNotThrow(() -> orderService.updateOrderStatuses(1L, update));
    }

    @Test
    void isValidStatusTransition_ConfirmedToCompleted() {
        // Test CONFIRMED to COMPLETED
        testOrder.setStatus(OrderStatus.CONFIRMED);
        OrderStatusUpdateDto update = new OrderStatusUpdateDto();
        update.setOrderId(1L);
        update.setNewStatus(OrderStatus.COMPLETED);

        when(coffeeShopRepository.findById(1L)).thenReturn(Optional.of(testCoffeeShop));
        when(orderRepository.findByIdInAndCoffeeShopId(1L, 1L)).thenReturn(testOrder);

        assertDoesNotThrow(() -> orderService.updateOrderStatuses(1L, update));
    }

    @Test
    void isValidStatusTransition_ProcessingToCompleted() {
        // Test PROCESSING to COMPLETED
        testOrder.setStatus(OrderStatus.PROCESSING);
        OrderStatusUpdateDto update = new OrderStatusUpdateDto();
        update.setOrderId(1L);
        update.setNewStatus(OrderStatus.COMPLETED);

        when(coffeeShopRepository.findById(1L)).thenReturn(Optional.of(testCoffeeShop));
        when(orderRepository.findByIdInAndCoffeeShopId(1L, 1L)).thenReturn(testOrder);

        assertDoesNotThrow(() -> orderService.updateOrderStatuses(1L, update));
    }


    @Test
    void isValidStatusTransition_InvalidFromCompleted() {
        // Test invalid transition from COMPLETED
        testOrder.setStatus(OrderStatus.COMPLETED);
        OrderStatusUpdateDto update = new OrderStatusUpdateDto();
        update.setOrderId(1L);
        update.setNewStatus(OrderStatus.PENDING);

        when(coffeeShopRepository.findById(1L)).thenReturn(Optional.of(testCoffeeShop));
        when(orderRepository.findByIdInAndCoffeeShopId(1L, 1L)).thenReturn(testOrder);

        InvalidOrderStateException exception = assertThrows(
                InvalidOrderStateException.class,
                () -> orderService.updateOrderStatuses(1L, update)
        );
        assertTrue(exception.getMessage().contains("Invalid status transition"));
    }

    @Test
    void isValidStatusTransition_InvalidFromCancelled() {
        // Test invalid transition from CANCELLED
        testOrder.setStatus(OrderStatus.CANCELLED);
        OrderStatusUpdateDto update = new OrderStatusUpdateDto();
        update.setOrderId(1L);
        update.setNewStatus(OrderStatus.PROCESSING);

        when(coffeeShopRepository.findById(1L)).thenReturn(Optional.of(testCoffeeShop));
        when(orderRepository.findByIdInAndCoffeeShopId(1L, 1L)).thenReturn(testOrder);

        InvalidOrderStateException exception = assertThrows(
                InvalidOrderStateException.class,
                () -> orderService.updateOrderStatuses(1L, update)
        );
        assertTrue(exception.getMessage().contains("Invalid status transition"));
    }

    @Test
    void processOrder_MultipleItems() {
        // Given - Order with multiple items
        OrderItemDto item2 = new OrderItemDto();
        item2.setMenuItemId(2L);
        item2.setQuantity(1);

        MenuItem menuItem2 = new MenuItem();
        menuItem2.setId(2L);
        menuItem2.setName("Latte");
        menuItem2.setPrice(new BigDecimal("5.00"));
        menuItem2.setAvailable(true);

        testOrderRequest.setItems(Arrays.asList(testOrderItemDto, item2));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(coffeeShopRepository.findById(1L)).thenReturn(Optional.of(testCoffeeShop));
        when(orderRepository.countActiveOrdersByShop(1L)).thenReturn(5);
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(testMenuItem));
        when(menuItemRepository.findById(2L)).thenReturn(Optional.of(menuItem2));
        when(queueService.assignQueuePosition(1L)).thenReturn(1);
        when(queueService.calculateEstimatedWaitTime(1L, 1)).thenReturn(15);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.toItemEntity(any(OrderItemDto.class))).thenReturn(new OrderItem());
        when(orderMapper.toDto(any(Order.class))).thenReturn(testOrderResponse);

        // When
        OrderResponseDto result = orderService.processOrder(testOrderRequest);

        // Then
        assertNotNull(result);
        verify(menuItemRepository, times(2)).findById(anyLong());
    }
}
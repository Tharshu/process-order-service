package com.shop.process_order_service.service;

import com.shop.process_order_service.dto.OrderRequestDto;
import com.shop.process_order_service.dto.OrderResponseDto;
import com.shop.process_order_service.entity.CoffeeShop;
import com.shop.process_order_service.entity.Customer;
import com.shop.process_order_service.entity.MenuItem;
import com.shop.process_order_service.entity.Order;
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
import java.util.Arrays;
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

    @InjectMocks
    private OrderService orderService;

    private Customer testCustomer;
    private CoffeeShop testShop;
    private MenuItem testMenuItem;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setName("Test Customer");
        testCustomer.setMobileNumber("+94771234567");
        testCustomer.setLoyaltyScore(0);

        testShop = new CoffeeShop();
        testShop.setId(1L);
        testShop.setName("Test Coffee Shop");
        testShop.setMaxQueueSize(50);

        testMenuItem = new MenuItem();
        testMenuItem.setId(1L);
        testMenuItem.setName("Test Coffee");
        testMenuItem.setPrice(BigDecimal.valueOf(500.00));
        testMenuItem.setAvailable(true);
        testMenuItem.setCoffeeShop(testShop);
    }

    @Test
    void processOrder_Success() {
        // Arrange
        OrderRequestDto request = new OrderRequestDto();
        request.setCustomerId(1L);
        request.setCoffeeShopId(1L);

        OrderRequestDto.OrderItemDto itemDto = new OrderRequestDto.OrderItemDto();
        itemDto.setMenuItemId(1L);
        itemDto.setQuantity(2);
        request.setItems(Arrays.asList(itemDto));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(coffeeShopRepository.findById(1L)).thenReturn(Optional.of(testShop));
        when(orderRepository.countActiveOrdersByShop(1L)).thenReturn(5);
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(testMenuItem));
        when(queueService.assignQueuePosition(1L)).thenReturn(6);
        when(queueService.calculateEstimatedWaitTime(1L, 6)).thenReturn(30);

        Order savedOrder = new Order();
        savedOrder.setId(1L);
        savedOrder.setCustomer(testCustomer);
        savedOrder.setCoffeeShop(testShop);
        savedOrder.setTotalAmount(BigDecimal.valueOf(1000.00));
        savedOrder.setQueuePosition(6);
        savedOrder.setEstimatedWaitTime(30);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // Act
        OrderResponseDto result = orderService.processOrder(request);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Customer", result.getCustomerName());
        assertEquals("Test Coffee Shop", result.getCoffeeShopName());

        verify(notificationService).sendOrderConfirmation(any(Order.class));
        verify(customerRepository).save(testCustomer);
        assertEquals(1, testCustomer.getLoyaltyScore());
    }

    @Test
    void processOrder_QueueFull_ThrowsException() {
        // Arrange
        OrderRequestDto request = new OrderRequestDto();
        request.setCustomerId(1L);
        request.setCoffeeShopId(1L);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(coffeeShopRepository.findById(1L)).thenReturn(Optional.of(testShop));
        when(orderRepository.countActiveOrdersByShop(1L)).thenReturn(50); // Queue is full

        // Act & Assert
        assertThrows(Exception.class, () -> orderService.processOrder(request));
    }
}
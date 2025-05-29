package com.shop.process_order_service.service;


import com.shop.process_order_service.dto.OrderItemResponseDto;
import com.shop.process_order_service.dto.OrderRequestDto;
import com.shop.process_order_service.dto.OrderResponseDto;
import com.shop.process_order_service.dto.QueuePositionDto;
import com.shop.process_order_service.entity.*;
import com.shop.process_order_service.exception.OrderNotFoundException;
import com.shop.process_order_service.exception.QueueFullException;
import com.shop.process_order_service.exception.ShopNotFoundException;
import com.shop.process_order_service.mapper.OrderMapper; // Import OrderMapper
import com.shop.process_order_service.repository.CoffeeShopRepository;
import com.shop.process_order_service.repository.CustomerRepository;
import com.shop.process_order_service.repository.MenuItemRepository;
import com.shop.process_order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final CoffeeShopRepository coffeeShopRepository;
    private final MenuItemRepository menuItemRepository;
    private final QueueService queueService;
    private final NotificationService notificationService;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponseDto processOrder(OrderRequestDto request) {
        log.info("Processing order for customer: {}, shop: {}",
                request.getCustomerId(), request.getCoffeeShopId());

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found: " + request.getCustomerId()));

        CoffeeShop shop = coffeeShopRepository.findById(request.getCoffeeShopId())
                .orElseThrow(() -> new ShopNotFoundException("Coffee shop not found: " + request.getCoffeeShopId()));

        Integer currentQueueSize = orderRepository.countActiveOrdersByShop(shop.getId());
        if (currentQueueSize >= shop.getMaxQueueSize()) {
            throw new QueueFullException("Queue is full for shop: " + shop.getName() + ". Please try again later.");
        }

        Order order = new Order();
        order.setCustomer(customer);
        order.setCoffeeShop(shop);
        order.setStatus(OrderStatus.PENDING);

        BigDecimal totalAmount = BigDecimal.ZERO;

        List<OrderItem> orderItems = request.getItems().stream()
            .map(itemDto -> {
                MenuItem menuItem = menuItemRepository.findById(itemDto.getMenuItemId())
                        .orElseThrow(() -> new RuntimeException("Menu item not found: " + itemDto.getMenuItemId()));
                if (!menuItem.getAvailable()) {
                    throw new RuntimeException("Menu item not available: " + menuItem.getName());
                }
                OrderItem orderItem = orderMapper.toItemEntity(itemDto);
                orderItem.setOrder(order);
                orderItem.setMenuItem(menuItem);
                orderItem.setUnitPrice(menuItem.getPrice());
                orderItem.setTotalPrice(menuItem.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity())));
                return orderItem;
            })
            .collect(Collectors.toList());

        order.setOrderItems(orderItems);

        for (OrderItem item : orderItems) {
            totalAmount = totalAmount.add(item.getTotalPrice());
        }
        order.setTotalAmount(totalAmount);

        Integer queuePosition = queueService.assignQueuePosition(shop.getId());
        order.setQueuePosition(queuePosition);
        order.setEstimatedWaitTime(queueService.calculateEstimatedWaitTime(shop.getId(), queuePosition));

        Order savedOrder = orderRepository.save(order);

        customer.setLoyaltyScore(customer.getLoyaltyScore() + 1);
        customerRepository.save(customer);

        notificationService.sendOrderConfirmation(savedOrder);

        log.info("Order processed successfully: {}", savedOrder.getId());
        return orderMapper.toDto(savedOrder);
    }

    public OrderResponseDto getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
        return orderMapper.toDto(order);
    }

    public List<OrderResponseDto> getCustomerOrders(Long customerId) {
        List<Order> orders = orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        return orders.stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    public QueuePositionDto getQueuePosition(Long orderId, Long customerId) {
        Order order = orderRepository.findByIdAndCustomerId(orderId, customerId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        if (order.getStatus() == OrderStatus.COMPLETED ||
                order.getStatus() == OrderStatus.CANCELLED) {
            return QueuePositionDto.builder()
                    .orderId(orderId)
                    .currentPosition(0)
                    .totalInQueue(0)
                    .estimatedWaitTime(0)
                    .status(order.getStatus().toString())
                    .build();
        }

        Integer totalInQueue = orderRepository.countActiveOrdersByShop(order.getCoffeeShop().getId());

        return QueuePositionDto.builder()
                .orderId(orderId)
                .currentPosition(order.getQueuePosition())
                .totalInQueue(totalInQueue)
                .estimatedWaitTime(order.getEstimatedWaitTime())
                .status(order.getStatus().toString())
                .build();
    }

    @Transactional
    public void cancelOrder(Long orderId, Long customerId) {
        Order order = orderRepository.findByIdAndCustomerId(orderId, customerId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new RuntimeException("Cannot cancel completed order");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        queueService.reorderQueue(order.getCoffeeShop().getId());

        notificationService.sendOrderCancellation(order);

        log.info("Order cancelled: {}", orderId);
    }
}
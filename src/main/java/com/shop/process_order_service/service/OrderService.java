package com.shop.process_order_service.service;


import com.shop.process_order_service.dto.*;
import com.shop.process_order_service.entity.*;
import com.shop.process_order_service.exception.*;
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
import java.util.Map;
import java.util.function.Function;
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
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found: " + request.getCustomerId()));

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

    @Transactional(readOnly = true)
    public OrderResponseDto getOrder(Long orderId) {
        log.info("Fetching order with id: {}", orderId);
        Order order = orderRepository.findByIdWithOrderItems(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));
        log.info("Order found: {}", order);
        return orderMapper.toDto(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDto> getCustomerOrders(Long customerId) {
        List<Order> orders = orderRepository.findOrdersWithCustomerByCustomerId(customerId);
        if (orders.isEmpty()) {
            throw new CustomerNotFoundException("No orders found for customer with ID: " + customerId);
        }
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

        String mobileNumber = order.getCustomer().getMobileNumber();

        notificationService.sendOrderCancellation(mobileNumber, order.getId());

        log.info("Order cancelled: {}", orderId);
    }


    @Transactional
    public void updateOrderStatuses(Long shopId, OrderStatusUpdateDto update) {
        log.info("Updating order status for shop: {}, update: {}", shopId, update);

        CoffeeShop shop = coffeeShopRepository.findById(shopId)
                .orElseThrow(() -> new ShopNotFoundException("Coffee shop not found: " + shopId));

        Order orderToUpdate = orderRepository.findByIdInAndCoffeeShopId(update.getOrderId(), shopId);

        if (orderToUpdate == null) {
            throw new OrderNotFoundException("No order found for shop: " + shopId + " and order ID: " + update.getOrderId());
        }

        OrderStatus currentStatus = orderToUpdate.getStatus();
        OrderStatus newStatus = update.getNewStatus();

        if (!isValidStatusTransition(currentStatus, newStatus)) {
            throw new InvalidOrderStateException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }

        orderToUpdate.setStatus(newStatus);

        if (newStatus == OrderStatus.COMPLETED) {
            queueService.reorderQueue(shopId);
        }

        orderRepository.save(orderToUpdate);
        log.info("Updated order status for order: {} in shop: {} from {} to {}",
                orderToUpdate.getId(), shopId, currentStatus, newStatus);
    }

    private boolean isValidStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        return switch (currentStatus) {
            case PENDING ->
                    newStatus == OrderStatus.CONFIRMED || newStatus == OrderStatus.CANCELLED || newStatus == OrderStatus.PROCESSING || newStatus == OrderStatus.COMPLETED;
            case CONFIRMED ->
                    newStatus == OrderStatus.PROCESSING || newStatus == OrderStatus.CANCELLED || newStatus == OrderStatus.COMPLETED;
            case PROCESSING -> newStatus == OrderStatus.COMPLETED || newStatus == OrderStatus.CANCELLED;
            default -> false;
        };
    }


}
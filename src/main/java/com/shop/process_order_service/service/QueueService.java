package com.shop.process_order_service.service;


import com.shop.process_order_service.entity.Order;
import com.shop.process_order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueueService {

    private final OrderRepository orderRepository;
    private static final int AVERAGE_PREPARATION_TIME_MINUTES = 5;

    public Integer assignQueuePosition(Long shopId) {
        Integer currentCount = orderRepository.countActiveOrdersByShop(shopId);
        return currentCount + 1;
    }

    public Integer calculateEstimatedWaitTime(Long shopId, Integer queuePosition) {
        return queuePosition * AVERAGE_PREPARATION_TIME_MINUTES;
    }

    public void reorderQueue(Long shopId) {
        List<Order> activeOrders = orderRepository.findActiveOrdersByShop(shopId);

        for (int i = 0; i < activeOrders.size(); i++) {
            Order order = activeOrders.get(i);
            order.setQueuePosition(i + 1);
            order.setEstimatedWaitTime(calculateEstimatedWaitTime(shopId, i + 1));
        }

        orderRepository.saveAll(activeOrders);
        log.info("Queue reordered for shop: {}", shopId);
    }
}

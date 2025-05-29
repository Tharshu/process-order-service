package com.shop.process_order_service.service;

import com.shop.process_order_service.entity.Order;
import com.shop.process_order_service.exception.NotificationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    @Async
    @Retryable(value = NotificationException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void sendOrderConfirmation(Order order) {
        try {

            log.info("Sending order confirmation to customer: {} for order: {}",
                    order.getCustomer().getMobileNumber(), order.getId());
            log.info("Order confirmation sent successfully for order: {}", order.getId());

        } catch (Exception e) {
            log.error("Failed to send notification for order: {}. Error: {}", order.getId(), e.getMessage());
            if (!(e instanceof NotificationException)) {
                throw new NotificationException("Failed to send order confirmation due to an unexpected error: " + e.getMessage(), e);
            }
            throw e;
        }
    }

    @Async
    public void sendOrderCancellation(Order order) {
        log.info("Sending order cancellation notification to customer: {} for order: {}",
                order.getCustomer().getMobileNumber(), order.getId());
        try {
            Thread.sleep(1000);
            log.info("Order cancellation notification sent successfully for order: {}", order.getId());
        } catch (InterruptedException e) {
            log.error("Error sending cancellation notification for order {}: {}", order.getId(), e.getMessage());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Unexpected error sending cancellation notification for order {}: {}", order.getId(), e.getMessage());
        }
    }

    @Async
    public void sendQueueUpdateNotification(Order order) {
        log.info("Sending queue update notification to customer: {} for order: {}. New position: {}, Estimated wait: {} mins",
                order.getCustomer().getMobileNumber(), 
                order.getId(),
                order.getQueuePosition(),
                order.getEstimatedWaitTime());
        log.info("Queue update notification sent successfully for order: {}", order.getId());
    }
}

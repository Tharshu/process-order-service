package com.shop.process_order_service.repository;


import com.shop.process_order_service.entity.Order;
import com.shop.process_order_service.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("select o from Order o left join fetch o.customer where o.customer.id = :customerId order by o.createdAt desc")
    List<Order> findOrdersWithCustomerByCustomerId(@Param("customerId") Long customerId);

    List<Order> findByCoffeeShopIdAndStatusInOrderByQueuePosition(
            Long coffeeShopId, List<OrderStatus> statuses);

    @Query("SELECT o FROM Order o WHERE o.coffeeShop.id = :shopId " +
            "AND o.status IN ('PENDING', 'CONFIRMED', 'IN_PROGRESS') " +
            "ORDER BY o.queuePosition")
    List<Order> findActiveOrdersByShop(@Param("shopId") Long shopId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.coffeeShop.id = :shopId " +
            "AND o.status IN ('PENDING', 'CONFIRMED', 'IN_PROGRESS')")
    Integer countActiveOrdersByShop(@Param("shopId") Long shopId);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.id = :orderId")
    Optional<Order> findByIdWithOrderItems(@Param("orderId") Long orderId);



    Optional<Order> findByIdAndCustomerId(Long orderId, Long customerId);

    @Query("SELECT o FROM Order o WHERE o.id IN :orderId AND o.coffeeShop.id = :shopId")
    Order findByIdInAndCoffeeShopId(Long orderId, Long shopId);

}
package com.shop.process_order_service.repository;


import com.shop.process_order_service.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByCoffeeShopIdAndAvailableTrue(Long coffeeShopId);
    List<MenuItem> findByCoffeeShopId(Long coffeeShopId);
}
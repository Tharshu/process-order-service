package com.shop.process_order_service.repository;


import com.shop.process_order_service.entity.CoffeeShop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoffeeShopRepository extends JpaRepository<CoffeeShop, Long> {

    @Query("SELECT c FROM CoffeeShop c WHERE " +
            "(6371 * acos(cos(radians(:lat)) * cos(radians(c.latitude)) * " +
            "cos(radians(c.longitude) - radians(:lng)) + sin(radians(:lat)) * " +
            "sin(radians(c.latitude)))) <= :radius ORDER BY " +
            "(6371 * acos(cos(radians(:lat)) * cos(radians(c.latitude)) * " +
            "cos(radians(c.longitude) - radians(:lng)) + sin(radians(:lat)) * " +
            "sin(radians(c.latitude))))")
    List<CoffeeShop> findNearbyShops(@Param("lat") Double latitude,
                                     @Param("lng") Double longitude,
                                     @Param("radius") Double radiusKm);
}

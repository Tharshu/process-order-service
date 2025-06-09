package com.shop.process_order_service.mapper;

import com.shop.process_order_service.dto.*;
import com.shop.process_order_service.entity.Customer;
import com.shop.process_order_service.entity.Order;
import com.shop.process_order_service.entity.OrderItem;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "phone", target = "mobileNumber")
    @Mapping(source = "address", target = "homeAddress")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "workAddress", ignore = true)
    @Mapping(target = "loyaltyScore", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Customer toEntity(CustomerRequestDto dto);

    @Mapping(source = "items", target = "orderItems")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "coffeeShop", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "queuePosition", ignore = true)
    @Mapping(target = "estimatedWaitTime", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Order toEntity(OrderRequestDto dto);

    @Mapping(source = "id", target = "orderId")
    @Mapping(source = "customer.name", target = "customerName")
    @Mapping(source = "coffeeShop.name", target = "coffeeShopName")
    @Mapping(source = "orderItems", target = "items")
    OrderResponseDto toDto(Order order);

    @Mapping(source = "menuItem.name", target = "itemName")
    OrderItemResponseDto toOrderItemResponseDto(OrderItem orderItem);

    List<OrderItemResponseDto> toOrderItemResponseDtoList(List<OrderItem> orderItems);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "menuItem", ignore = true)
    @Mapping(source = "quantity", target = "quantity")
    @Mapping(source = "notes", target = "notes")
    @Mapping(target = "unitPrice", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    OrderItem toItemEntity(OrderItemDto itemDto);
}
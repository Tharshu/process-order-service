package com.shop.process_order_service.mapper;

import com.shop.process_order_service.dto.CustomerRequestDto;
import com.shop.process_order_service.dto.CustomerResponseDto;
import com.shop.process_order_service.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "phone", source = "mobileNumber")
    @Mapping(target = "homeAddress", source = "homeAddress")
    @Mapping(target = "workAddress", source = "workAddress")
    CustomerResponseDto toDto(Customer customer);

    @Mapping(source = "phone", target = "mobileNumber")
    @Mapping(source = "address", target = "homeAddress")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "workAddress", ignore = true)
    @Mapping(target = "loyaltyScore", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Customer toEntity(CustomerRequestDto dto);
}

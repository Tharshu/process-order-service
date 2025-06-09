package com.shop.process_order_service.service;

import com.shop.process_order_service.dto.CustomerResponseDto;
import com.shop.process_order_service.entity.Customer;
import com.shop.process_order_service.mapper.CustomerMapper;
import com.shop.process_order_service.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public List<CustomerResponseDto> getAllCustomers() {
        List<Customer> customers = customerRepository.findAll();
        return customers.stream()
                .map(customerMapper::toDto)
                .collect(Collectors.toList());
    }
}


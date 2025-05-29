package com.shop.process_order_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableRetry
public class ProcessOrderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProcessOrderServiceApplication.class, args);
	}

}

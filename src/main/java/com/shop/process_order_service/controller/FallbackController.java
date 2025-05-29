package com.shop.process_order_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FallbackController {

    @RequestMapping("/**")
    public ResponseEntity<String> fallback() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("The requested resource was not found on this server.");
    }
}

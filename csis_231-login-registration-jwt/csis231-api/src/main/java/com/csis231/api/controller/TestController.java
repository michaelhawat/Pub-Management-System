package com.csis231.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {
    
    @GetMapping
    public Map<String, String> test() {
        return Map.of("status", "Backend is working!", "message", "Pub Management System API is running");
    }
    
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "OK", "timestamp", java.time.LocalDateTime.now().toString());
    }
}

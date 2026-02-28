package com.atozshop.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Home Controller
 * Provides a welcome page and API information
 */
@RestController
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", "A to Z Shop Management");
        response.put("version", "0.1.0-SNAPSHOT");
        response.put("status", "running");

        Map<String, String> links = new HashMap<>();
        links.put("swagger", "/swagger-ui.html");
        links.put("api-docs", "/v3/api-docs");
        links.put("health", "/api/v1/auth/health");

        response.put("links", links);
        response.put("message", "Welcome! Visit /swagger-ui.html for API documentation");

        return ResponseEntity.ok(response);
    }
}

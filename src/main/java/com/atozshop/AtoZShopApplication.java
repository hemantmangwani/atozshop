package com.atozshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * A to Z Shop Management Application
 *
 * Main entry point for the Spring Boot application.
 *
 * Features:
 * - POS Billing with barcode scanning
 * - Inventory Management with stock ledger
 * - E-commerce Website
 * - Order Management & Delivery Tracking
 * - Business Analytics & Reporting
 *
 * @author Hemant Mangwani
 * @version 0.1.0
 */
@SpringBootApplication
@EnableJpaAuditing
public class AtoZShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(AtoZShopApplication.class, args);
        System.out.println("\n" +
            "╔══════════════════════════════════════════════════════════╗\n" +
            "║                                                          ║\n" +
            "║     A to Z Shop Management Application Started! 🚀      ║\n" +
            "║                                                          ║\n" +
            "║  API Documentation: http://localhost:8080/swagger-ui    ║\n" +
            "║  H2 Console:        http://localhost:8080/h2-console    ║\n" +
            "║                                                          ║\n" +
            "╚══════════════════════════════════════════════════════════╝\n");
    }
}

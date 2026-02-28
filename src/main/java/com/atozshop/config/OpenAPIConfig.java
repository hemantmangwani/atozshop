package com.atozshop.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger Configuration
 * Provides interactive API documentation
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "A to Z Shop Management API",
        version = "0.1.0",
        description = "Comprehensive shop management system with POS, inventory, e-commerce, and analytics",
        contact = @Contact(
            name = "Hemant Mangwani",
            email = "hemant.mangwani@example.com"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Local Development"),
        @Server(url = "https://api.atozshop.com", description = "Production")
    }
)
@SecurityScheme(
    name = "Bearer Authentication",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer",
    in = SecuritySchemeIn.HEADER
)
public class OpenAPIConfig {
}

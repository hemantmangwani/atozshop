package com.atozshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * JWT Response DTO
 * Returned after successful login
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {

    private String token;
    private String type = "Bearer";
    private Long id;
    private String email;
    private String username;
    private String fullName;
    private Long tenantId;
    private List<String> roles;
    private Long customerId; // Customer ID for online orders (if user is a customer)
}

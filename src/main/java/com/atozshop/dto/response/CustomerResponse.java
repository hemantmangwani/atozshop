package com.atozshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponse {

    private Long id;
    private Long tenantId;
    private String customerCode;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private String gstin;
    private Integer loyaltyPoints;
    private BigDecimal totalPurchases;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

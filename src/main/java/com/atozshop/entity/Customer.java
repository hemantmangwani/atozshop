package com.atozshop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Customer entity for POS billing and loyalty tracking
 * Auto-generates customer code: CUST-YYYYMMDD-XXX
 */
@Entity
@Table(
    name = "customers",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tenantId", "phone"}),
        @UniqueConstraint(columnNames = {"tenantId", "customerCode"})
    },
    indexes = {
        @Index(name = "idx_customers_tenant", columnList = "tenantId"),
        @Index(name = "idx_customers_phone", columnList = "tenantId, phone"),
        @Index(name = "idx_customers_code", columnList = "tenantId, customerCode"),
        @Index(name = "idx_customers_active", columnList = "tenantId, isActive")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 50)
    private String customerCode;  // Auto: CUST-YYYYMMDD-XXX

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(length = 255)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 20)
    private String postalCode;

    @Column(length = 50)
    private String gstin;

    @Column(nullable = false)
    @Builder.Default
    private Integer loyaltyPoints = 0;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalPurchases = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}

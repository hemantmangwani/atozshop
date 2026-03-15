package com.atozshop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Discount/Offer master entity
 * Defines promotional offers with flexible rules
 */
@Entity
@Table(
    name = "discounts",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tenantId", "discountCode"})
    },
    indexes = {
        @Index(name = "idx_discounts_tenant", columnList = "tenantId"),
        @Index(name = "idx_discounts_active", columnList = "tenantId, isActive"),
        @Index(name = "idx_discounts_code", columnList = "tenantId, discountCode"),
        @Index(name = "idx_discounts_dates", columnList = "tenantId, validFrom, validTo")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Discount extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 50)
    private String discountCode;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DiscountType discountType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;  // 10 for 10%, or 500 for ₹500 off

    @Column(precision = 12, scale = 2)
    private BigDecimal minPurchaseAmount;  // Minimum bill amount required

    @Column(precision = 12, scale = 2)
    private BigDecimal maxDiscountAmount;  // Cap on discount amount

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicableOn applicableOn;

    @Column
    private LocalDate validFrom;

    @Column
    private LocalDate validTo;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Discount type enumeration
     * PERCENTAGE: discountValue represents percentage (10 = 10%)
     * FIXED_AMOUNT: discountValue represents fixed amount (500 = ₹500 off)
     */
    public enum DiscountType {
        PERCENTAGE,
        FIXED_AMOUNT
    }

    /**
     * Applicable on enumeration
     * Defines where the discount can be applied
     */
    public enum ApplicableOn {
        ITEM,       // Specific product items
        BILL,       // Entire bill
        CATEGORY    // Product category
    }
}

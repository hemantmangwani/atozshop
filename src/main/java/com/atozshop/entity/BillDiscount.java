package com.atozshop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Bill discount entity
 * Historical record of discounts applied to bills
 * Stores discount details as snapshots for historical accuracy
 */
@Entity
@Table(
    name = "bill_discounts",
    indexes = {
        @Index(name = "idx_bill_discounts_bill", columnList = "billId"),
        @Index(name = "idx_bill_discounts_discount", columnList = "discountId")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillDiscount extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long billId;

    @Column
    private Long discountId;  // NULL for ad-hoc discounts

    @Column(nullable = false, length = 200)
    private String discountName;  // Snapshot at time of application

    @Column(length = 50)
    private String discountCode;  // Snapshot at time of application

    @Column(nullable = false, length = 20)
    private String discountType;  // Snapshot: PERCENTAGE or FIXED_AMOUNT

    @Column(precision = 10, scale = 2)
    private BigDecimal discountValue;  // Snapshot of discount value

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount;  // Actual amount deducted from bill
}

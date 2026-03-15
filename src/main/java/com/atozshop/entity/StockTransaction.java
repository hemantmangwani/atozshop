package com.atozshop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_transactions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "transaction_number"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "transaction_number", nullable = false, length = 50)
    private String transactionNumber;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "supplier_name", length = 200)
    private String supplierName;

    @Column(name = "total_quantity", nullable = false)
    @Builder.Default
    private Integer totalQuantity = 0;

    @Column(name = "total_cost", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalCost = BigDecimal.ZERO;

    @Column(name = "expected_revenue", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal expectedRevenue = BigDecimal.ZERO;

    @Column(name = "expected_profit", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal expectedProfit = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.DRAFT;

    @Column(length = 1000)
    private String notes;

    @Column(name = "created_by")
    private Long createdBy;

    public enum Status {
        DRAFT,
        CONFIRMED,
        CANCELLED
    }
}

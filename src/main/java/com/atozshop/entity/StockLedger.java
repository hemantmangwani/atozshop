package com.atozshop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_ledger", indexes = {
    @Index(name = "idx_stock_ledger_variant", columnList = "tenant_id,variant_id,store_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockLedger extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "variant_id", nullable = false)
    private Long variantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private TransactionType transactionType;

    @Column(name = "transaction_id")
    private Long transactionId;

    @Column(name = "quantity_change", nullable = false)
    private Integer quantityChange;

    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;

    @Column(name = "cost_price_snapshot", precision = 10, scale = 2)
    private BigDecimal costPriceSnapshot;

    @Column(name = "selling_price_snapshot", precision = 10, scale = 2)
    private BigDecimal sellingPriceSnapshot;

    @Column(length = 500)
    private String remarks;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "created_by")
    private Long createdBy;

    public enum TransactionType {
        INCOMING,
        SALE,
        RETURN,
        ADJUSTMENT
    }
}

package com.atozshop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Bill/Invoice entity for POS sales transactions
 * Auto-generates bill number: BIL-YYYYMMDD-XXX
 * Status workflow: DRAFT → CONFIRMED → (optional) CANCELLED
 */
@Entity
@Table(
    name = "bills",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tenantId", "billNumber"})
    },
    indexes = {
        @Index(name = "idx_bills_tenant_store", columnList = "tenantId, storeId"),
        @Index(name = "idx_bills_date", columnList = "tenantId, billDate"),
        @Index(name = "idx_bills_customer", columnList = "customerId"),
        @Index(name = "idx_bills_status", columnList = "tenantId, status"),
        @Index(name = "idx_bills_payment_status", columnList = "tenantId, paymentStatus"),
        @Index(name = "idx_bills_number", columnList = "tenantId, billNumber"),
        @Index(name = "idx_bills_cashier", columnList = "cashierId")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bill extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @Column(nullable = false)
    private Long storeId;

    @Column
    private Long customerId;  // NULL for walk-in customers

    @Column
    private Long cashierId;  // User who created the bill

    @Column(nullable = false, length = 50)
    private String billNumber;  // Auto: BIL-YYYYMMDD-XXX

    @Column(nullable = false)
    private LocalDateTime billDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BillType billType;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalItems = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalQuantity = 0;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal balanceAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BillStatus status = BillStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column
    private Long createdBy;

    /**
     * Bill type enumeration
     */
    public enum BillType {
        SALES,          // Normal sales transaction
        SALES_RETURN    // Return/refund transaction
    }

    /**
     * Bill status enumeration
     * DRAFT: Bill being created, items can be added/removed
     * CONFIRMED: Bill finalized, stock deducted, immutable
     * CANCELLED: Bill cancelled (only DRAFT bills can be cancelled)
     */
    public enum BillStatus {
        DRAFT,
        CONFIRMED,
        CANCELLED
    }

    /**
     * Payment status enumeration
     */
    public enum PaymentStatus {
        UNPAID,    // No payment received
        PARTIAL,   // Partially paid
        PAID,      // Fully paid
        REFUNDED   // Refunded (for returns)
    }
}

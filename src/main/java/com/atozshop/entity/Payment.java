package com.atozshop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment transaction entity
 * Supports multiple payment methods and split payments
 */
@Entity
@Table(
    name = "payments",
    indexes = {
        @Index(name = "idx_payments_bill", columnList = "billId"),
        @Index(name = "idx_payments_date", columnList = "tenantId, paymentDate"),
        @Index(name = "idx_payments_method", columnList = "tenantId, paymentMethod"),
        @Index(name = "idx_payments_reference", columnList = "referenceNumber")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long billId;

    @Column(nullable = false)
    private Long tenantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Column(nullable = false)
    private LocalDateTime paymentDate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(length = 100)
    private String referenceNumber;  // Transaction ID for digital payments

    @Column(length = 4)
    private String cardLast4;  // Last 4 digits of card

    @Column(length = 100)
    private String upiId;  // UPI ID

    @Column(length = 100)
    private String bankName;  // For cards/cheques

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column
    private Long createdBy;

    /**
     * Payment method enumeration
     */
    public enum PaymentMethod {
        CASH,      // Cash payment
        CARD,      // Debit/Credit card
        UPI,       // UPI payment (GPay, PhonePe, etc.)
        WALLET,    // Digital wallets (Paytm, etc.)
        CHEQUE     // Cheque payment
    }
}

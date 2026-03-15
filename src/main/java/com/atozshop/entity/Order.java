package com.atozshop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_orders_tenant_store", columnList = "tenant_id, store_id"),
    @Index(name = "idx_orders_customer", columnList = "customer_id"),
    @Index(name = "idx_orders_status", columnList = "tenant_id, status"),
    @Index(name = "idx_orders_date", columnList = "tenant_id, order_date"),
    @Index(name = "idx_orders_number", columnList = "tenant_id, order_number")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "order_number", nullable = false, length = 50)
    private String orderNumber;  // ORD-YYYYMMDD-XXX

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    // Delivery Information
    @Column(name = "delivery_address_id")
    private Long deliveryAddressId;

    @Column(name = "delivery_slot", length = 50)
    private String deliverySlot;  // "9 AM - 12 PM"

    @Column(name = "delivery_fee", precision = 10, scale = 2)
    private BigDecimal deliveryFee;

    @Column(name = "customer_notes", columnDefinition = "TEXT")
    private String customerNotes;

    // Amounts
    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "discount_amount", precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "tax_amount", precision = 12, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 20)
    private PaymentStatus paymentStatus;

    // Tracking Timestamps
    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "packed_at")
    private LocalDateTime packedAt;

    @Column(name = "dispatched_at")
    private LocalDateTime dispatchedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancel_reason", columnDefinition = "TEXT")
    private String cancelReason;

    // Who performed actions
    @Column(name = "accepted_by")
    private Long acceptedBy;

    @Column(name = "packed_by")
    private Long packedBy;

    @Column(name = "dispatched_by")
    private Long dispatchedBy;

    @Column(name = "delivered_by")
    private Long deliveredBy;

    @Column(name = "cancelled_by")
    private Long cancelledBy;

    // Enums
    public enum OrderStatus {
        NEW,              // Just placed by customer
        ACCEPTED,         // Admin accepted order, stock reserved
        PACKED,           // Items packed, ready for delivery
        OUT_FOR_DELIVERY, // Dispatched for delivery
        DELIVERED,        // Successfully delivered
        CANCELLED,        // Order cancelled
        RETURNED          // Order returned after delivery
    }

    public enum PaymentMethod {
        COD,              // Cash on Delivery
        ONLINE,           // Online payment (Razorpay, etc.)
        WALLET,           // Digital wallet
        UPI               // UPI payment
    }

    public enum PaymentStatus {
        PENDING,          // Payment not yet received
        PAID,             // Payment received
        REFUNDED,         // Payment refunded
        FAILED            // Payment failed
    }
}

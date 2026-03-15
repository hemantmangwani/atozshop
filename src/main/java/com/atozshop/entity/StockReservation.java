package com.atozshop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_reservations", indexes = {
    @Index(name = "idx_stock_reservations_variant", columnList = "tenant_id, variant_id, status"),
    @Index(name = "idx_stock_reservations_order", columnList = "order_id"),
    @Index(name = "idx_stock_reservations_expires", columnList = "status, expires_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockReservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "variant_id", nullable = false)
    private Long variantId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity;

    @Column(name = "reservation_date", nullable = false)
    private LocalDateTime reservationDate;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;  // Auto-release after X hours

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.ACTIVE;

    @Column(name = "reserved_by")
    private Long reservedBy;  // User ID who created the reservation

    @Column(name = "released_at")
    private LocalDateTime releasedAt;

    @Column(name = "release_reason", length = 200)
    private String releaseReason;

    // Enum
    public enum ReservationStatus {
        ACTIVE,      // Currently reserved
        FULFILLED,   // Order delivered, stock deducted
        CANCELLED,   // Order cancelled, stock released
        EXPIRED      // Reservation expired, stock released
    }

    @PrePersist
    protected void onCreate() {
        if (reservationDate == null) {
            reservationDate = LocalDateTime.now();
        }
        if (expiresAt == null) {
            // Default: expire after 24 hours
            expiresAt = reservationDate.plusHours(24);
        }
    }
}

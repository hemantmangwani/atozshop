package com.atozshop.service;

import com.atozshop.entity.StockReservation;
import com.atozshop.repository.StockLedgerRepository;
import com.atozshop.repository.StockReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockReservationService {

    private final StockReservationRepository reservationRepository;
    private final StockLedgerRepository stockLedgerRepository;

    /**
     * Get available stock for a variant (excluding reserved stock)
     */
    public Integer getAvailableStock(Long variantId, Long storeId, Long tenantId) {
        // Get total current stock from ledger
        Integer currentStock = stockLedgerRepository.getCurrentStock(variantId, storeId, tenantId);
        if (currentStock == null) {
            currentStock = 0;
        }

        // Get reserved stock
        Integer reservedStock = reservationRepository.getTotalReservedStock(variantId, storeId, tenantId);
        if (reservedStock == null) {
            reservedStock = 0;
        }

        // Available = Current - Reserved
        int available = currentStock - reservedStock;
        return Math.max(0, available);  // Never return negative
    }

    /**
     * Check if variant has enough available stock
     */
    public boolean hasAvailableStock(Long variantId, Long storeId, Long tenantId, Integer requiredQuantity) {
        Integer available = getAvailableStock(variantId, storeId, tenantId);
        return available >= requiredQuantity;
    }

    /**
     * Reserve stock for an order
     */
    @Transactional
    public StockReservation reserveStock(Long tenantId, Long storeId, Long variantId, Long orderId,
                                        Integer quantity, Long reservedBy) {
        log.info("Reserving {} units of variant {} for order {}", quantity, variantId, orderId);

        // Check availability
        if (!hasAvailableStock(variantId, storeId, tenantId, quantity)) {
            Integer available = getAvailableStock(variantId, storeId, tenantId);
            throw new RuntimeException("Insufficient stock. Available: " + available + ", Requested: " + quantity);
        }

        // Create reservation
        StockReservation reservation = StockReservation.builder()
                .tenantId(tenantId)
                .storeId(storeId)
                .variantId(variantId)
                .orderId(orderId)
                .reservedQuantity(quantity)
                .reservationDate(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(24))  // 24 hour expiry
                .status(StockReservation.ReservationStatus.ACTIVE)
                .reservedBy(reservedBy)
                .build();

        reservation = reservationRepository.save(reservation);

        log.info("Stock reserved: {} units of variant {} (Reservation ID: {})",
                quantity, variantId, reservation.getId());

        return reservation;
    }

    /**
     * Reserve stock for multiple items (order with multiple products)
     */
    @Transactional
    public void reserveStockBatch(Long tenantId, Long storeId, Long orderId,
                                 List<ReservationItem> items, Long reservedBy) {
        log.info("Reserving stock for {} items in order {}", items.size(), orderId);

        // First, validate all items have sufficient stock
        for (ReservationItem item : items) {
            if (!hasAvailableStock(item.getVariantId(), storeId, tenantId, item.getQuantity())) {
                Integer available = getAvailableStock(item.getVariantId(), storeId, tenantId);
                throw new RuntimeException("Insufficient stock for variant " + item.getVariantId() +
                        ". Available: " + available + ", Requested: " + item.getQuantity());
            }
        }

        // If all validations pass, reserve all items
        for (ReservationItem item : items) {
            reserveStock(tenantId, storeId, item.getVariantId(), orderId, item.getQuantity(), reservedBy);
        }

        log.info("Successfully reserved stock for {} items", items.size());
    }

    /**
     * Release reserved stock (when order is cancelled)
     */
    @Transactional
    public void releaseReservation(Long orderId, String releaseReason) {
        log.info("Releasing stock reservation for order {}: {}", orderId, releaseReason);

        List<StockReservation> reservations = reservationRepository.findByOrderId(orderId);

        for (StockReservation reservation : reservations) {
            reservation.setStatus(StockReservation.ReservationStatus.CANCELLED);
            reservation.setReleasedAt(LocalDateTime.now());
            reservation.setReleaseReason(releaseReason);
            reservationRepository.save(reservation);

            log.info("Released {} units of variant {} (Reservation ID: {})",
                    reservation.getReservedQuantity(), reservation.getVariantId(), reservation.getId());
        }
    }

    /**
     * Mark reservation as fulfilled (when order is delivered)
     */
    @Transactional
    public void fulfillReservation(Long orderId) {
        log.info("Marking stock reservation as fulfilled for order {}", orderId);

        List<StockReservation> reservations = reservationRepository.findByOrderId(orderId);

        for (StockReservation reservation : reservations) {
            reservation.setStatus(StockReservation.ReservationStatus.FULFILLED);
            reservation.setReleasedAt(LocalDateTime.now());
            reservation.setReleaseReason("Order delivered");
            reservationRepository.save(reservation);
        }
    }

    /**
     * Clean up expired reservations (scheduled job)
     */
    @Transactional
    public int cleanupExpiredReservations() {
        log.info("Cleaning up expired stock reservations");

        List<StockReservation> expired = reservationRepository.findExpiredReservations(LocalDateTime.now());

        for (StockReservation reservation : expired) {
            reservation.setStatus(StockReservation.ReservationStatus.EXPIRED);
            reservation.setReleasedAt(LocalDateTime.now());
            reservation.setReleaseReason("Reservation expired");
            reservationRepository.save(reservation);

            log.info("Expired reservation ID {}: {} units of variant {}",
                    reservation.getId(), reservation.getReservedQuantity(), reservation.getVariantId());
        }

        log.info("Cleaned up {} expired reservations", expired.size());
        return expired.size();
    }

    /**
     * Get reserved stock for a specific order
     */
    public List<StockReservation> getOrderReservations(Long orderId) {
        return reservationRepository.findByOrderId(orderId);
    }

    /**
     * Helper class for batch reservation
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReservationItem {
        private Long variantId;
        private Integer quantity;
    }
}

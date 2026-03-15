package com.atozshop.repository;

import com.atozshop.entity.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockReservationRepository extends JpaRepository<StockReservation, Long> {

    List<StockReservation> findByOrderId(Long orderId);

    Optional<StockReservation> findByOrderIdAndVariantId(Long orderId, Long variantId);

    List<StockReservation> findByTenantIdAndStoreIdAndVariantIdAndStatus(
        Long tenantId, Long storeId, Long variantId, StockReservation.ReservationStatus status
    );

    @Query("SELECT COALESCE(SUM(sr.reservedQuantity), 0) FROM StockReservation sr WHERE sr.variantId = ?1 AND sr.storeId = ?2 AND sr.tenantId = ?3 AND sr.status = 'ACTIVE'")
    Integer getTotalReservedStock(Long variantId, Long storeId, Long tenantId);

    @Query("SELECT sr FROM StockReservation sr WHERE sr.status = 'ACTIVE' AND sr.expiresAt < ?1")
    List<StockReservation> findExpiredReservations(LocalDateTime currentTime);

    @Query("SELECT sr.variantId, SUM(sr.reservedQuantity) FROM StockReservation sr WHERE sr.tenantId = ?1 AND sr.storeId = ?2 AND sr.status = 'ACTIVE' GROUP BY sr.variantId")
    List<Object[]> getReservedStockByVariant(Long tenantId, Long storeId);

    void deleteByOrderId(Long orderId);
}

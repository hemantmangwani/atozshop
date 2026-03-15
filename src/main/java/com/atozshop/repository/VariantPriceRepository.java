package com.atozshop.repository;

import com.atozshop.entity.VariantPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VariantPriceRepository extends JpaRepository<VariantPrice, Long> {

    Optional<VariantPrice> findByIdAndTenantId(Long id, Long tenantId);

    List<VariantPrice> findByVariantIdAndTenantId(Long variantId, Long tenantId);

    @Query("SELECT vp FROM VariantPrice vp WHERE vp.tenantId = :tenantId AND vp.variantId = :variantId " +
           "AND (:storeId IS NULL OR vp.storeId = :storeId) " +
           "AND vp.effectiveFrom <= :date AND (vp.effectiveTo IS NULL OR vp.effectiveTo >= :date)")
    Optional<VariantPrice> findCurrentPrice(@Param("tenantId") Long tenantId,
                                             @Param("variantId") Long variantId,
                                             @Param("storeId") Long storeId,
                                             @Param("date") LocalDate date);

    @Query("UPDATE VariantPrice vp SET vp.effectiveTo = :endDate WHERE vp.tenantId = :tenantId " +
           "AND vp.variantId = :variantId AND (:storeId IS NULL OR vp.storeId = :storeId) " +
           "AND vp.effectiveTo IS NULL")
    void closeCurrentPrices(@Param("tenantId") Long tenantId,
                            @Param("variantId") Long variantId,
                            @Param("storeId") Long storeId,
                            @Param("endDate") LocalDate endDate);
}

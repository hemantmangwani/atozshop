package com.atozshop.repository;

import com.atozshop.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    Optional<ProductVariant> findByIdAndTenantId(Long id, Long tenantId);

    List<ProductVariant> findByTenantId(Long tenantId);

    Optional<ProductVariant> findBySkuAndTenantId(String sku, Long tenantId);

    Optional<ProductVariant> findByBarcodeValueAndTenantId(String barcodeValue, Long tenantId);

    boolean existsBySkuAndTenantId(String sku, Long tenantId);

    boolean existsByBarcodeValueAndTenantId(String barcodeValue, Long tenantId);

    List<ProductVariant> findByProductIdAndTenantId(Long productId, Long tenantId);

    List<ProductVariant> findByTenantIdAndIsActive(Long tenantId, Boolean isActive);

    @Query("SELECT v FROM ProductVariant v WHERE v.tenantId = :tenantId AND v.id IN " +
           "(SELECT sl.variantId FROM StockLedger sl WHERE sl.tenantId = :tenantId AND sl.storeId = :storeId " +
           "GROUP BY sl.variantId HAVING SUM(sl.quantityChange) <= v.minStockThreshold)")
    List<ProductVariant> findLowStockVariants(@Param("tenantId") Long tenantId,
                                               @Param("storeId") Long storeId);
}

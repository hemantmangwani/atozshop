package com.atozshop.repository;

import com.atozshop.entity.StockLedger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface StockLedgerRepository extends JpaRepository<StockLedger, Long> {

    List<StockLedger> findByVariantIdAndStoreIdAndTenantIdOrderByTransactionDateDesc(
            Long variantId, Long storeId, Long tenantId);

    Page<StockLedger> findByTenantIdAndStoreIdOrderByTransactionDateDesc(
            Long tenantId, Long storeId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(sl.quantityChange), 0) FROM StockLedger sl " +
           "WHERE sl.variantId = :variantId AND sl.storeId = :storeId AND sl.tenantId = :tenantId")
    Integer getCurrentStock(@Param("variantId") Long variantId,
                            @Param("storeId") Long storeId,
                            @Param("tenantId") Long tenantId);

    @Query("SELECT sl.variantId as variantId, SUM(sl.quantityChange) as currentStock " +
           "FROM StockLedger sl WHERE sl.tenantId = :tenantId AND sl.storeId = :storeId " +
           "GROUP BY sl.variantId")
    List<Map<String, Object>> getAllStockBalances(@Param("tenantId") Long tenantId,
                                                    @Param("storeId") Long storeId);

    List<StockLedger> findByTransactionIdAndTenantId(Long transactionId, Long tenantId);
}

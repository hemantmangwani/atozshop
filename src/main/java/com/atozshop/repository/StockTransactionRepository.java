package com.atozshop.repository;

import com.atozshop.entity.StockTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {

    Optional<StockTransaction> findByIdAndTenantId(Long id, Long tenantId);

    Optional<StockTransaction> findByTransactionNumberAndTenantId(String transactionNumber, Long tenantId);

    Page<StockTransaction> findByTenantIdOrderByTransactionDateDesc(Long tenantId, Pageable pageable);

    Page<StockTransaction> findByTenantIdAndStatusOrderByTransactionDateDesc(
            Long tenantId, StockTransaction.Status status, Pageable pageable);

    @Query("SELECT MAX(CAST(SUBSTRING(st.transactionNumber, LENGTH(st.transactionNumber) - 2, 3) AS int)) " +
           "FROM StockTransaction st WHERE st.tenantId = :tenantId " +
           "AND st.transactionDate >= :startOfDay AND st.transactionDate < :endOfDay")
    Integer findMaxSequenceForDate(@Param("tenantId") Long tenantId,
                                    @Param("startOfDay") LocalDateTime startOfDay,
                                    @Param("endOfDay") LocalDateTime endOfDay);
}

package com.atozshop.repository;

import com.atozshop.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    Optional<Bill> findByBillNumberAndTenantId(String billNumber, Long tenantId);

    Optional<Bill> findByIdAndTenantId(Long id, Long tenantId);

    List<Bill> findByTenantIdAndStoreId(Long tenantId, Long storeId);

    List<Bill> findByTenantIdAndStoreIdAndBillDateBetween(
        Long tenantId,
        Long storeId,
        LocalDateTime from,
        LocalDateTime to
    );

    List<Bill> findByCustomerIdAndTenantId(Long customerId, Long tenantId);

    List<Bill> findByTenantIdAndStatus(Long tenantId, Bill.BillStatus status);

    @Query("SELECT b.billNumber FROM Bill b WHERE b.tenantId = ?1 AND b.billNumber LIKE ?2% ORDER BY b.billNumber DESC LIMIT 1")
    Optional<String> findLastBillNumberForDate(Long tenantId, String datePrefix);

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Bill b WHERE b.tenantId = ?1 AND b.storeId = ?2 AND b.status = 'CONFIRMED' AND DATE(b.billDate) = ?3")
    BigDecimal getTotalSalesForDate(Long tenantId, Long storeId, LocalDate date);

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Bill b WHERE b.tenantId = ?1 AND b.storeId = ?2 AND b.status = 'CONFIRMED' AND b.billDate BETWEEN ?3 AND ?4")
    BigDecimal getTotalSalesBetweenDates(Long tenantId, Long storeId, LocalDateTime from, LocalDateTime to);

    @Query("SELECT COUNT(b) FROM Bill b WHERE b.tenantId = ?1 AND b.storeId = ?2 AND b.status = 'CONFIRMED' AND DATE(b.billDate) = ?3")
    Long getTransactionCountForDate(Long tenantId, Long storeId, LocalDate date);

    @Query("SELECT COALESCE(SUM(b.totalQuantity), 0) FROM Bill b WHERE b.tenantId = ?1 AND b.storeId = ?2 AND b.status = 'CONFIRMED' AND DATE(b.billDate) = ?3")
    Long getTotalItemsSoldForDate(Long tenantId, Long storeId, LocalDate date);

    boolean existsByBillNumberAndTenantId(String billNumber, Long tenantId);

    @Query("SELECT b.billNumber FROM Bill b WHERE b.tenantId = ?1 AND b.billNumber LIKE ?2 ORDER BY b.billNumber DESC LIMIT 1")
    String findLastBillNumberLike(Long tenantId, String pattern);
}

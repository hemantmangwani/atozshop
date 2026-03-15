package com.atozshop.repository;

import com.atozshop.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByBillId(Long billId);

    List<Payment> findByTenantId(Long tenantId);

    List<Payment> findByTenantIdAndPaymentDateBetween(Long tenantId, LocalDateTime from, LocalDateTime to);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.billId = ?1")
    BigDecimal getTotalPaidForBill(Long billId);

    @Query("SELECT p.paymentMethod, SUM(p.amount) FROM Payment p WHERE p.tenantId = ?1 AND DATE(p.paymentDate) = ?2 GROUP BY p.paymentMethod")
    List<Object[]> getPaymentMethodBreakdownForDate(Long tenantId, LocalDate date);

    @Query("SELECT p.paymentMethod, SUM(p.amount) FROM Payment p WHERE p.tenantId = ?1 AND p.paymentDate BETWEEN ?2 AND ?3 GROUP BY p.paymentMethod")
    List<Object[]> getPaymentMethodBreakdownBetweenDates(Long tenantId, LocalDateTime from, LocalDateTime to);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.tenantId = ?1 AND DATE(p.paymentDate) = ?2")
    BigDecimal getTotalPaymentsForDate(Long tenantId, LocalDate date);

    List<Payment> findByTenantIdAndPaymentMethod(Long tenantId, Payment.PaymentMethod paymentMethod);
}

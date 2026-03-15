package com.atozshop.repository;

import com.atozshop.entity.BillDiscount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface BillDiscountRepository extends JpaRepository<BillDiscount, Long> {

    List<BillDiscount> findByBillId(Long billId);

    void deleteByBillId(Long billId);

    @Query("SELECT COALESCE(SUM(bd.discountAmount), 0) FROM BillDiscount bd WHERE bd.billId = ?1")
    BigDecimal getTotalDiscountForBill(Long billId);
}

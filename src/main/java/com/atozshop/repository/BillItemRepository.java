package com.atozshop.repository;

import com.atozshop.entity.BillItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillItemRepository extends JpaRepository<BillItem, Long> {

    List<BillItem> findByBillId(Long billId);

    Optional<BillItem> findByIdAndBillId(Long id, Long billId);

    void deleteByBillId(Long billId);

    @Query("SELECT COUNT(bi) FROM BillItem bi WHERE bi.billId = ?1")
    Integer countByBillId(Long billId);

    @Query("SELECT COALESCE(SUM(bi.quantity), 0) FROM BillItem bi WHERE bi.billId = ?1")
    Integer sumQuantityByBillId(Long billId);

    @Query("SELECT bi FROM BillItem bi WHERE bi.billId = ?1 AND bi.variantId = ?2")
    Optional<BillItem> findByBillIdAndVariantId(Long billId, Long variantId);
}

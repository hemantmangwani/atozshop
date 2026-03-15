package com.atozshop.repository;

import com.atozshop.entity.StockTransactionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockTransactionItemRepository extends JpaRepository<StockTransactionItem, Long> {

    List<StockTransactionItem> findByTransactionId(Long transactionId);

    void deleteByTransactionId(Long transactionId);
}

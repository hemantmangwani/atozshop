package com.atozshop.repository;

import com.atozshop.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    List<OrderItem> findByVariantId(Long variantId);

    @Query("SELECT oi FROM OrderItem oi WHERE oi.orderId IN ?1")
    List<OrderItem> findByOrderIdIn(List<Long> orderIds);

    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.variantId = ?1")
    Integer getTotalQuantityOrderedForVariant(Long variantId);

    @Query("SELECT oi.variantId, SUM(oi.quantity) FROM OrderItem oi WHERE oi.orderId IN (SELECT o.id FROM Order o WHERE o.tenantId = ?1 AND o.status = 'DELIVERED') GROUP BY oi.variantId ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> getTopOrderedVariants(Long tenantId);
}

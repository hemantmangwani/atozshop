package com.atozshop.repository;

import com.atozshop.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumberAndTenantId(String orderNumber, Long tenantId);

    Optional<Order> findByIdAndTenantId(Long id, Long tenantId);

    List<Order> findByTenantIdAndStoreId(Long tenantId, Long storeId);

    List<Order> findByCustomerIdAndTenantIdOrderByOrderDateDesc(Long customerId, Long tenantId);

    List<Order> findByTenantIdAndStatus(Long tenantId, Order.OrderStatus status);

    List<Order> findByTenantIdAndStoreIdAndStatus(Long tenantId, Long storeId, Order.OrderStatus status);

    List<Order> findByTenantIdAndOrderDateBetween(Long tenantId, LocalDateTime from, LocalDateTime to);

    @Query("SELECT o.orderNumber FROM Order o WHERE o.tenantId = ?1 AND o.orderNumber LIKE ?2 ORDER BY o.orderNumber DESC LIMIT 1")
    String findLastOrderNumberLike(Long tenantId, String pattern);

    @Query("SELECT o FROM Order o WHERE o.tenantId = ?1 AND o.storeId = ?2 AND o.status IN ?3 ORDER BY o.orderDate DESC")
    List<Order> findByTenantAndStoreAndStatusIn(Long tenantId, Long storeId, List<Order.OrderStatus> statuses);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.tenantId = ?1 AND o.storeId = ?2 AND o.status = ?3")
    Long countByTenantAndStoreAndStatus(Long tenantId, Long storeId, Order.OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.tenantId = ?1 AND o.customerId = ?2 AND o.status NOT IN ('CANCELLED', 'DELIVERED') ORDER BY o.orderDate DESC")
    List<Order> findActiveOrdersByCustomer(Long tenantId, Long customerId);

    boolean existsByOrderNumberAndTenantId(String orderNumber, Long tenantId);
}

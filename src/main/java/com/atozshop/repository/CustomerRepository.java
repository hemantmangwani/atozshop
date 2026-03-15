package com.atozshop.repository;

import com.atozshop.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByPhoneAndTenantId(String phone, Long tenantId);

    Optional<Customer> findByEmailAndTenantId(String email, Long tenantId);

    Optional<Customer> findByCustomerCodeAndTenantId(String customerCode, Long tenantId);

    List<Customer> findByTenantIdAndIsActive(Long tenantId, Boolean isActive);

    @Query("SELECT c FROM Customer c WHERE c.tenantId = ?1 AND (c.name LIKE %?2% OR c.phone LIKE %?2%)")
    List<Customer> searchByNameOrPhone(Long tenantId, String keyword);

    @Query("SELECT c.customerCode FROM Customer c WHERE c.tenantId = ?1 AND c.customerCode LIKE ?2% ORDER BY c.customerCode DESC LIMIT 1")
    Optional<String> findLastCustomerCodeForDate(Long tenantId, String datePrefix);

    boolean existsByPhoneAndTenantId(String phone, Long tenantId);

    boolean existsByCustomerCodeAndTenantId(String customerCode, Long tenantId);
}

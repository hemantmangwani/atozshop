package com.atozshop.repository;

import com.atozshop.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Optional<Supplier> findByIdAndTenantId(Long id, Long tenantId);

    Optional<Supplier> findByCodeAndTenantId(String code, Long tenantId);

    List<Supplier> findByTenantIdAndIsActive(Long tenantId, Boolean isActive);

    boolean existsByCodeAndTenantId(String code, Long tenantId);

    @Query("SELECT s FROM Supplier s WHERE s.tenantId = ?1 AND " +
           "(s.name LIKE %?2% OR s.phone LIKE %?2% OR s.email LIKE %?2%)")
    List<Supplier> searchByNameOrPhoneOrEmail(Long tenantId, String keyword);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(s.code, 15) AS int)), 0) FROM Supplier s " +
           "WHERE s.tenantId = ?1 AND s.code LIKE ?2")
    Integer findLastSequenceForDate(Long tenantId, String datePrefix);
}

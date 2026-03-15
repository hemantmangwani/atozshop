package com.atozshop.repository;

import com.atozshop.entity.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {

    Optional<Discount> findByDiscountCodeAndTenantId(String discountCode, Long tenantId);

    Optional<Discount> findByIdAndTenantId(Long id, Long tenantId);

    List<Discount> findByTenantIdAndIsActive(Long tenantId, Boolean isActive);

    @Query("SELECT d FROM Discount d WHERE d.tenantId = ?1 AND d.isActive = true AND d.discountCode = ?2 AND (?3 BETWEEN d.validFrom AND d.validTo OR (d.validFrom IS NULL AND d.validTo IS NULL))")
    Optional<Discount> findActiveDiscountByCode(Long tenantId, String discountCode, LocalDate currentDate);

    @Query("SELECT d FROM Discount d WHERE d.tenantId = ?1 AND d.isActive = true AND (?2 BETWEEN d.validFrom AND d.validTo OR (d.validFrom IS NULL AND d.validTo IS NULL))")
    List<Discount> findAllActiveDiscounts(Long tenantId, LocalDate currentDate);

    boolean existsByDiscountCodeAndTenantId(String discountCode, Long tenantId);
}

package com.atozshop.repository;

import com.atozshop.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    List<Store> findByTenantIdAndIsActive(Long tenantId, Boolean isActive);

    Optional<Store> findByCodeAndTenantId(String code, Long tenantId);

    Optional<Store> findByIdAndTenantId(Long id, Long tenantId);

    boolean existsByCodeAndTenantId(String code, Long tenantId);
}
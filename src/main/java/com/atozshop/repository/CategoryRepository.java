package com.atozshop.repository;

import com.atozshop.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByIdAndTenantId(Long id, Long tenantId);

    Optional<Category> findBySlugAndTenantId(String slug, Long tenantId);

    boolean existsByNameAndTenantId(String name, Long tenantId);

    boolean existsBySlugAndTenantId(String slug, Long tenantId);

    List<Category> findByTenantIdAndParentIdOrderBySortOrder(Long tenantId, Long parentId);

    List<Category> findByTenantIdAndIsActiveOrderBySortOrder(Long tenantId, Boolean isActive);

    List<Category> findByTenantIdOrderBySortOrder(Long tenantId);
}

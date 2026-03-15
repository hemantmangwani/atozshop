package com.atozshop.repository;

import com.atozshop.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByIdAndTenantId(Long id, Long tenantId);

    Optional<Product> findBySlugAndTenantId(String slug, Long tenantId);

    boolean existsByNameAndTenantId(String name, Long tenantId);

    boolean existsBySlugAndTenantId(String slug, Long tenantId);

    List<Product> findByCategoryIdAndTenantId(Long categoryId, Long tenantId);

    Page<Product> findByTenantId(Long tenantId, Pageable pageable);

    Page<Product> findByTenantIdAndIsActive(Long tenantId, Boolean isActive, Pageable pageable);

    List<Product> findByTenantIdAndIsActive(Long tenantId, Boolean isActive);

    @Query("SELECT p FROM Product p WHERE p.tenantId = :tenantId AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchProducts(@Param("tenantId") Long tenantId,
                                  @Param("keyword") String keyword,
                                  Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.tenantId = :tenantId AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Product> searchProductsList(@Param("tenantId") Long tenantId,
                                     @Param("keyword") String keyword);
}

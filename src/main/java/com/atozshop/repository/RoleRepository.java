package com.atozshop.repository;

import com.atozshop.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByNameAndTenantId(String name, Long tenantId);

    boolean existsByNameAndTenantId(String name, Long tenantId);
}
package com.atozshop.repository;

import com.atozshop.entity.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Long> {

    List<CustomerAddress> findByCustomerId(Long customerId);

    Optional<CustomerAddress> findByIdAndCustomerId(Long id, Long customerId);

    Optional<CustomerAddress> findByCustomerIdAndIsDefault(Long customerId, Boolean isDefault);

    @Modifying
    @Query("UPDATE CustomerAddress ca SET ca.isDefault = false WHERE ca.customerId = ?1")
    void clearDefaultForCustomer(Long customerId);

    @Query("SELECT COUNT(ca) FROM CustomerAddress ca WHERE ca.customerId = ?1")
    Long countByCustomerId(Long customerId);

    boolean existsByIdAndCustomerId(Long id, Long customerId);
}

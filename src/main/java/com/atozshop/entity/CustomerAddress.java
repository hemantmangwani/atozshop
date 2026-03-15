package com.atozshop.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "customer_addresses", indexes = {
    @Index(name = "idx_customer_addresses_customer", columnList = "customer_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerAddress extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", length = 20)
    private AddressType addressType;

    @Column(name = "address_line1", nullable = false)
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    @Column(name = "landmark", length = 100)
    private String landmark;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "state", nullable = false, length = 100)
    private String state;

    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;

    @Column(name = "country", length = 100)
    @Builder.Default
    private String country = "India";

    @Column(name = "phone", length = 20)
    private String phone;  // Contact phone for this address

    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;

    // Enum
    public enum AddressType {
        HOME,
        WORK,
        OTHER
    }
}

package com.atozshop.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Supplier entity - Represents vendors who supply products
 */
@Entity
@Table(name = "suppliers", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenantId", "code"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supplier extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 50)
    private String code;  // Auto: SUP-YYYYMMDD-XXX

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 200)
    private String contactPerson;

    @Column(length = 20)
    private String phone;

    @Column(length = 255)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 20)
    private String postalCode;

    @Column(length = 100)
    private String country;

    @Column(length = 50)
    private String gstNumber;

    @Column(length = 50)
    private String panNumber;

    @Column(length = 100)
    private String bankName;

    @Column(length = 50)
    private String bankAccountNumber;

    @Column(length = 20)
    private String bankIfscCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SupplierType supplierType = SupplierType.LOCAL;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public enum SupplierType {
        LOCAL,
        NATIONAL,
        INTERNATIONAL
    }
}

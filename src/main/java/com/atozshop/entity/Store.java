package com.atozshop.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Store entity - Represents physical shop locations
 * Supports multi-branch operations
 */
@Entity
@Table(name = "stores", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenantId", "code"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Store extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 50)
    private String code;

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

    @Column(length = 20)
    private String phone;

    @Column(length = 255)
    private String email;

    @Column(length = 50)
    private String gstNumber;

    @Column(length = 500)
    private String logoUrl;

    @Column(nullable = false)
    private Boolean isActive = true;
}
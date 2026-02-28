package com.atozshop.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Tenant entity - Supports multi-tenancy (multiple shops)
 * Each tenant represents a separate shop/business
 */
@Entity
@Table(name = "tenants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(length = 50)
    private String timezone = "UTC";

    @Column(nullable = false)
    private Boolean isActive = true;
}
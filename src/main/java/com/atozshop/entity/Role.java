package com.atozshop.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Role entity - Defines user roles for access control
 * System roles: ADMIN, MANAGER, CASHIER, STOCK_KEEPER, DELIVERY_AGENT
 */
@Entity
@Table(name = "roles", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenantId", "name"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Boolean isSystem = false;
}

package com.atozshop.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "products", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "slug"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 250)
    private String slug;

    @Column(length = 1000)
    private String description;

    @Column(length = 100)
    private String brand;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}

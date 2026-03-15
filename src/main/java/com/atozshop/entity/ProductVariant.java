package com.atozshop.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_variants", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "sku"}),
    @UniqueConstraint(columnNames = {"tenant_id", "barcode_value"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false, length = 50)
    private String sku;

    @Column(name = "variant_name", length = 200)
    private String variantName;

    @Column(length = 50)
    private String unit;

    @Column(name = "barcode_value", length = 100)
    private String barcodeValue;

    @Column(name = "qr_value", length = 255)
    private String qrValue;

    @Column(name = "min_stock_threshold")
    @Builder.Default
    private Integer minStockThreshold = 0;

    @Column(name = "max_stock_threshold")
    private Integer maxStockThreshold;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}

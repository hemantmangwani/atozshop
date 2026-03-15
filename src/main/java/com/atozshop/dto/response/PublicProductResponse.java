package com.atozshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicProductResponse {

    private Long id;
    private String sku;
    private String name;
    private String description;
    private String categoryName;
    private String brandName;

    // Primary variant (default display)
    private Long defaultVariantId;
    private String defaultVariantName;
    private BigDecimal sellingPrice;
    private BigDecimal mrp;
    private BigDecimal discountPercent;

    // Stock availability
    private Integer availableStock;
    private String stockStatus;  // "In Stock", "Low Stock", "Out of Stock"
    private Boolean isAvailable;

    // Images
    private String primaryImageUrl;
    private List<String> imageUrls;

    // All variants
    private List<VariantInfo> variants;

    // Product metadata
    private Double averageRating;
    private Integer reviewCount;
    private Boolean isFeatured;
    private Boolean isNew;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariantInfo {
        private Long id;
        private String name;
        private String sku;
        private BigDecimal sellingPrice;
        private BigDecimal mrp;
        private Integer availableStock;
        private String stockStatus;
    }
}

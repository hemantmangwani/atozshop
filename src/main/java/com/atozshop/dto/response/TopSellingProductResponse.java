package com.atozshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopSellingProductResponse {

    private Long variantId;
    private String sku;
    private String productName;
    private String variantName;

    // Metrics
    private Integer totalQuantitySold;
    private BigDecimal totalRevenue;
    private BigDecimal totalProfit;

    // Rankings
    private Integer rankByQuantity;
    private Integer rankByRevenue;
    private Integer rankByProfit;
}

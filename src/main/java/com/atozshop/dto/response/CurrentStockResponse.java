package com.atozshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentStockResponse {

    private Long variantId;
    private String sku;
    private String variantName;
    private String productName;
    private Integer currentStock;
    private Integer minStockThreshold;
    private Integer maxStockThreshold;
    private BigDecimal costPrice;
    private BigDecimal sellingPrice;
    private String stockStatus;
}

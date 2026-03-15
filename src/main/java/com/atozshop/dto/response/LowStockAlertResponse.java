package com.atozshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LowStockAlertResponse {

    private Long variantId;
    private String sku;
    private String variantName;
    private String productName;
    private Integer currentStock;
    private Integer minStockThreshold;
    private Integer shortfall;
    private String alertLevel;
}

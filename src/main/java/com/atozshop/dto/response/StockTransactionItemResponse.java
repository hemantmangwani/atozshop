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
public class StockTransactionItemResponse {

    private Long id;
    private Long variantId;
    private String variantName;
    private String sku;
    private Integer quantity;
    private BigDecimal costPrice;
    private BigDecimal sellingPrice;
    private BigDecimal totalCost;
    private BigDecimal expectedRevenue;
    private BigDecimal expectedProfit;
    private String remarks;
}

package com.atozshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockTransactionResponse {

    private Long id;
    private Long tenantId;
    private Long storeId;
    private String storeName;
    private String transactionNumber;
    private LocalDateTime transactionDate;
    private String supplierName;
    private Integer totalQuantity;
    private BigDecimal totalCost;
    private BigDecimal expectedRevenue;
    private BigDecimal expectedProfit;
    private String status;
    private String notes;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<StockTransactionItemResponse> items;
}

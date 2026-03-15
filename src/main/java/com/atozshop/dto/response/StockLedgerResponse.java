package com.atozshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockLedgerResponse {

    private Long id;
    private Long tenantId;
    private Long storeId;
    private Long variantId;
    private String variantName;
    private String transactionType;
    private Long transactionId;
    private String transactionNumber;
    private Integer quantityChange;
    private Integer balanceAfter;
    private BigDecimal costPriceSnapshot;
    private BigDecimal sellingPriceSnapshot;
    private String remarks;
    private LocalDateTime transactionDate;
    private Long createdBy;
    private LocalDateTime createdAt;
}

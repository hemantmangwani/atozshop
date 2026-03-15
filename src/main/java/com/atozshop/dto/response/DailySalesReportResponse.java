package com.atozshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailySalesReportResponse {

    private LocalDate reportDate;
    private Long tenantId;
    private Long storeId;
    private String storeName;

    // Transaction summary
    private Long totalTransactions;
    private Long totalItemsSold;
    private BigDecimal totalSales;
    private BigDecimal totalDiscount;
    private BigDecimal totalTax;
    private BigDecimal netSales;

    // Payment breakdown
    private Map<String, BigDecimal> paymentMethodBreakdown;

    // Average metrics
    private BigDecimal averageTransactionValue;
    private Double averageItemsPerTransaction;
}

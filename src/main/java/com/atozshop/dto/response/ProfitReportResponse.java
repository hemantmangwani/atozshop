package com.atozshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfitReportResponse {

    private LocalDate fromDate;
    private LocalDate toDate;
    private String period;  // DAY, WEEK, MONTH

    // Revenue
    private BigDecimal totalSales;
    private BigDecimal totalDiscounts;
    private BigDecimal netSales;

    // Cost
    private BigDecimal totalCost;

    // Profit
    private BigDecimal grossProfit;         // Net Sales - COGS
    private BigDecimal profitMargin;        // (Profit / Net Sales) * 100

    // Additional Metrics
    private Integer totalTransactions;
    private BigDecimal averageOrderValue;   // Net Sales / Transactions
}

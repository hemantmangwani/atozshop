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
public class DailyClosingReportResponse {

    private LocalDate reportDate;
    private Long storeId;
    private String storeName;

    // Sales Summary
    private Integer totalBills;
    private Integer totalItems;
    private Integer totalQuantity;

    // Amount Summary
    private BigDecimal grossSales;          // Total before discounts
    private BigDecimal totalDiscounts;      // Total discounts given
    private BigDecimal netSales;            // Gross - Discounts
    private BigDecimal totalTax;            // Total tax collected
    private BigDecimal finalTotal;          // Net + Tax

    // Payment Summary
    private Map<String, BigDecimal> paymentBreakdown;  // CASH: 100000, UPI: 50000, etc.
    private BigDecimal totalPaid;
    private BigDecimal totalPending;

    // Profit Summary
    private BigDecimal totalCost;           // Cost of goods sold
    private BigDecimal grossProfit;         // Net Sales - COGS
    private BigDecimal profitPercentage;    // (Profit / Net Sales) * 100

    // Returns (if applicable)
    private Integer totalReturns;
    private BigDecimal totalRefunds;

    // Cash Drawer Reconciliation
    private BigDecimal expectedCash;        // Cash payments received
    private BigDecimal declaredCash;        // Actual cash in drawer (manual entry)
    private BigDecimal cashVariance;        // Expected - Declared
}

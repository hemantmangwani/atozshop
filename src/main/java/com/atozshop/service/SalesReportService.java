package com.atozshop.service;

import com.atozshop.dto.response.DailyClosingReportResponse;
import com.atozshop.dto.response.ProfitReportResponse;
import com.atozshop.dto.response.TopSellingProductResponse;
import com.atozshop.repository.BillItemRepository;
import com.atozshop.repository.BillRepository;
import com.atozshop.repository.PaymentRepository;
import com.atozshop.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalesReportService {

    private final BillRepository billRepository;
    private final BillItemRepository billItemRepository;
    private final PaymentRepository paymentRepository;
    private final StoreRepository storeRepository;
    private final JdbcTemplate jdbcTemplate;

    @Transactional(readOnly = true)
    public DailyClosingReportResponse getDailyClosingReport(Long tenantId, Long storeId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        // Get all confirmed bills for the day
        String billQuery = """
            SELECT
                COUNT(*) as total_bills,
                COALESCE(SUM(total_items), 0) as total_items,
                COALESCE(SUM(total_quantity), 0) as total_quantity,
                COALESCE(SUM(subtotal), 0) as gross_sales,
                COALESCE(SUM(discount_amount), 0) as total_discounts,
                COALESCE(SUM(subtotal - discount_amount), 0) as net_sales,
                COALESCE(SUM(tax_amount), 0) as total_tax,
                COALESCE(SUM(total_amount), 0) as final_total,
                COALESCE(SUM(paid_amount), 0) as total_paid,
                COALESCE(SUM(balance_amount), 0) as total_pending
            FROM bills
            WHERE tenant_id = ? AND store_id = ?
              AND bill_date >= ? AND bill_date <= ?
              AND status = 'CONFIRMED'
              AND bill_type = 'SALES'
            """;

        Map<String, Object> billStats = jdbcTemplate.queryForMap(billQuery,
                tenantId, storeId, startOfDay, endOfDay);

        // Get payment breakdown
        String paymentQuery = """
            SELECT payment_method, COALESCE(SUM(amount), 0) as total
            FROM payments
            WHERE tenant_id = ?
              AND payment_date >= ? AND payment_date <= ?
            GROUP BY payment_method
            """;

        List<Map<String, Object>> paymentResults = jdbcTemplate.queryForList(paymentQuery,
                tenantId, startOfDay, endOfDay);

        Map<String, BigDecimal> paymentBreakdown = paymentResults.stream()
                .collect(Collectors.toMap(
                        row -> (String) row.get("payment_method"),
                        row -> (BigDecimal) row.get("total")
                ));

        // Calculate profit (simplified - using current cost price)
        String profitQuery = """
            SELECT
                COALESCE(SUM(bi.total_amount), 0) as total_revenue,
                COALESCE(SUM(bi.quantity * vp.cost_price), 0) as total_cost
            FROM bill_items bi
            JOIN bills b ON bi.bill_id = b.id
            JOIN variant_prices vp ON bi.variant_id = vp.variant_id AND vp.store_id = b.store_id
            WHERE b.tenant_id = ? AND b.store_id = ?
              AND b.bill_date >= ? AND b.bill_date <= ?
              AND b.status = 'CONFIRMED'
              AND b.bill_type = 'SALES'
              AND vp.effective_to IS NULL
            """;

        Map<String, Object> profitStats = jdbcTemplate.queryForMap(profitQuery,
                tenantId, storeId, startOfDay, endOfDay);

        BigDecimal totalRevenue = (BigDecimal) profitStats.get("total_revenue");
        BigDecimal totalCost = (BigDecimal) profitStats.get("total_cost");
        BigDecimal grossProfit = totalRevenue.subtract(totalCost);
        BigDecimal profitPercentage = totalRevenue.compareTo(BigDecimal.ZERO) > 0 ?
                grossProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")) :
                BigDecimal.ZERO;

        // Get store name
        String storeName = storeRepository.findByIdAndTenantId(storeId, tenantId)
                .map(s -> s.getName())
                .orElse("Unknown Store");

        // Calculate expected cash (CASH payments only)
        BigDecimal expectedCash = paymentBreakdown.getOrDefault("CASH", BigDecimal.ZERO);

        return DailyClosingReportResponse.builder()
                .reportDate(date)
                .storeId(storeId)
                .storeName(storeName)
                .totalBills(((Number) billStats.get("total_bills")).intValue())
                .totalItems(((Number) billStats.get("total_items")).intValue())
                .totalQuantity(((Number) billStats.get("total_quantity")).intValue())
                .grossSales((BigDecimal) billStats.get("gross_sales"))
                .totalDiscounts((BigDecimal) billStats.get("total_discounts"))
                .netSales((BigDecimal) billStats.get("net_sales"))
                .totalTax((BigDecimal) billStats.get("total_tax"))
                .finalTotal((BigDecimal) billStats.get("final_total"))
                .paymentBreakdown(paymentBreakdown)
                .totalPaid((BigDecimal) billStats.get("total_paid"))
                .totalPending((BigDecimal) billStats.get("total_pending"))
                .totalCost(totalCost)
                .grossProfit(grossProfit)
                .profitPercentage(profitPercentage)
                .totalReturns(0)
                .totalRefunds(BigDecimal.ZERO)
                .expectedCash(expectedCash)
                .declaredCash(null)
                .cashVariance(null)
                .build();
    }

    @Transactional(readOnly = true)
    public List<TopSellingProductResponse> getTopSellingProducts(Long tenantId, Long storeId,
                                                                   LocalDate fromDate, LocalDate toDate,
                                                                   Integer limit) {
        LocalDateTime startDate = fromDate.atStartOfDay();
        LocalDateTime endDate = toDate.atTime(LocalTime.MAX);

        String query = """
            SELECT
                bi.variant_id,
                bi.sku,
                bi.product_name,
                bi.variant_name,
                SUM(bi.quantity) as total_quantity,
                SUM(bi.total_amount) as total_revenue,
                SUM(bi.total_amount - (bi.quantity * COALESCE(vp.cost_price, 0))) as total_profit
            FROM bill_items bi
            JOIN bills b ON bi.bill_id = b.id
            LEFT JOIN variant_prices vp ON bi.variant_id = vp.variant_id AND vp.store_id = b.store_id AND vp.effective_to IS NULL
            WHERE b.tenant_id = ? AND b.store_id = ?
              AND b.bill_date >= ? AND b.bill_date <= ?
              AND b.status = 'CONFIRMED'
              AND b.bill_type = 'SALES'
            GROUP BY bi.variant_id, bi.sku, bi.product_name, bi.variant_name
            ORDER BY total_quantity DESC
            LIMIT ?
            """;

        List<Map<String, Object>> results = jdbcTemplate.queryForList(query,
                tenantId, storeId, startDate, endDate, limit);

        List<TopSellingProductResponse> topProducts = new ArrayList<>();
        int rank = 1;

        for (Map<String, Object> row : results) {
            topProducts.add(TopSellingProductResponse.builder()
                    .variantId(((Number) row.get("variant_id")).longValue())
                    .sku((String) row.get("sku"))
                    .productName((String) row.get("product_name"))
                    .variantName((String) row.get("variant_name"))
                    .totalQuantitySold(((Number) row.get("total_quantity")).intValue())
                    .totalRevenue((BigDecimal) row.get("total_revenue"))
                    .totalProfit((BigDecimal) row.get("total_profit"))
                    .rankByQuantity(rank++)
                    .build());
        }

        return topProducts;
    }

    @Transactional(readOnly = true)
    public ProfitReportResponse getProfitReport(Long tenantId, Long storeId,
                                                  LocalDate fromDate, LocalDate toDate,
                                                  String period) {
        LocalDateTime startDate = fromDate.atStartOfDay();
        LocalDateTime endDate = toDate.atTime(LocalTime.MAX);

        String query = """
            SELECT
                COUNT(DISTINCT b.id) as total_transactions,
                COALESCE(SUM(b.subtotal), 0) as total_sales,
                COALESCE(SUM(b.discount_amount), 0) as total_discounts,
                COALESCE(SUM(b.subtotal - b.discount_amount), 0) as net_sales,
                COALESCE(SUM(bi.quantity * COALESCE(vp.cost_price, 0)), 0) as total_cost
            FROM bills b
            LEFT JOIN bill_items bi ON b.id = bi.bill_id
            LEFT JOIN variant_prices vp ON bi.variant_id = vp.variant_id AND vp.store_id = b.store_id AND vp.effective_to IS NULL
            WHERE b.tenant_id = ? AND b.store_id = ?
              AND b.bill_date >= ? AND b.bill_date <= ?
              AND b.status = 'CONFIRMED'
              AND b.bill_type = 'SALES'
            """;

        Map<String, Object> result = jdbcTemplate.queryForMap(query,
                tenantId, storeId, startDate, endDate);

        BigDecimal totalSales = (BigDecimal) result.get("total_sales");
        BigDecimal totalDiscounts = (BigDecimal) result.get("total_discounts");
        BigDecimal netSales = (BigDecimal) result.get("net_sales");
        BigDecimal totalCost = (BigDecimal) result.get("total_cost");
        Integer totalTransactions = ((Number) result.get("total_transactions")).intValue();

        BigDecimal grossProfit = netSales.subtract(totalCost);
        BigDecimal profitMargin = netSales.compareTo(BigDecimal.ZERO) > 0 ?
                grossProfit.divide(netSales, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")) :
                BigDecimal.ZERO;

        BigDecimal averageOrderValue = totalTransactions > 0 ?
                netSales.divide(new BigDecimal(totalTransactions), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        return ProfitReportResponse.builder()
                .fromDate(fromDate)
                .toDate(toDate)
                .period(period)
                .totalSales(totalSales)
                .totalDiscounts(totalDiscounts)
                .netSales(netSales)
                .totalCost(totalCost)
                .grossProfit(grossProfit)
                .profitMargin(profitMargin)
                .totalTransactions(totalTransactions)
                .averageOrderValue(averageOrderValue)
                .build();
    }
}

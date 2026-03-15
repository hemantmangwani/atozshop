package com.atozshop.controller;

import com.atozshop.dto.request.SalesReportRequest;
import com.atozshop.dto.response.DailyClosingReportResponse;
import com.atozshop.dto.response.DailySalesReportResponse;
import com.atozshop.dto.response.TopSellingProductResponse;
import com.atozshop.dto.response.ProfitReportResponse;
import com.atozshop.security.CurrentUser;
import com.atozshop.security.UserPrincipal;
import com.atozshop.service.SalesReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
@Tag(name = "Sales Reports", description = "APIs for sales reporting and analytics")
public class SalesReportController {

    private final SalesReportService salesReportService;

    @PostMapping("/daily-report")
    @Operation(summary = "Get daily sales report", description = "Daily sales report for a specific date")
    public ResponseEntity<DailyClosingReportResponse> getDailyReport(
        @Valid @RequestBody Map<String, Object> request
    ) {
        Long storeId = request.get("storeId") != null ?
            Long.valueOf(request.get("storeId").toString()) : 1L;
        Long tenantId = request.get("tenantId") != null ?
            Long.valueOf(request.get("tenantId").toString()) : 1L;
        LocalDate reportDate = LocalDate.parse(request.get("reportDate").toString());

        DailyClosingReportResponse report = salesReportService.getDailyClosingReport(
            tenantId, storeId, reportDate);
        return ResponseEntity.ok(report);
    }

    @PostMapping("/period-report")
    @Operation(summary = "Get period sales report", description = "Sales report for a date range")
    public ResponseEntity<ProfitReportResponse> getPeriodReport(
        @Valid @RequestBody Map<String, Object> request
    ) {
        Long storeId = request.get("storeId") != null ?
            Long.valueOf(request.get("storeId").toString()) : 1L;
        Long tenantId = request.get("tenantId") != null ?
            Long.valueOf(request.get("tenantId").toString()) : 1L;
        LocalDate startDate = LocalDate.parse(request.get("startDate").toString());
        LocalDate endDate = LocalDate.parse(request.get("endDate").toString());

        ProfitReportResponse report = salesReportService.getProfitReport(
            tenantId, storeId, startDate, endDate, "DAY");
        return ResponseEntity.ok(report);
    }

    @PostMapping("/top-products")
    @Operation(summary = "Get top selling products", description = "Returns top N selling products by quantity")
    public ResponseEntity<List<TopSellingProductResponse>> getTopProducts(
        @Valid @RequestBody Map<String, Object> request
    ) {
        Long storeId = request.get("storeId") != null ?
            Long.valueOf(request.get("storeId").toString()) : 1L;
        Long tenantId = request.get("tenantId") != null ?
            Long.valueOf(request.get("tenantId").toString()) : 1L;
        LocalDate startDate = LocalDate.parse(request.get("startDate").toString());
        LocalDate endDate = LocalDate.parse(request.get("endDate").toString());
        Integer limit = request.get("limit") != null ?
            Integer.valueOf(request.get("limit").toString()) : 10;

        List<TopSellingProductResponse> topProducts = salesReportService.getTopSellingProducts(
            tenantId, storeId, startDate, endDate, limit);
        return ResponseEntity.ok(topProducts);
    }

    @GetMapping("/daily-closing")
    @Operation(summary = "Get daily closing report", description = "Comprehensive daily closing report with sales, payments, profit, and cash reconciliation")
    public ResponseEntity<DailyClosingReportResponse> getDailyClosingReport(
        @CurrentUser UserPrincipal user,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        DailyClosingReportResponse report = salesReportService.getDailyClosingReport(
            user.getTenantId(), user.getStoreIdOrDefault(), date);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/top-selling-products")
    @Operation(summary = "Get top selling products", description = "Returns top N selling products by quantity, revenue, or profit")
    public ResponseEntity<List<TopSellingProductResponse>> getTopSellingProducts(
        @CurrentUser UserPrincipal user,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        @RequestParam(defaultValue = "10") Integer limit
    ) {
        List<TopSellingProductResponse> topProducts = salesReportService.getTopSellingProducts(
            user.getTenantId(), user.getStoreIdOrDefault(), fromDate, toDate, limit);
        return ResponseEntity.ok(topProducts);
    }

    @GetMapping("/profit")
    @Operation(summary = "Get profit report", description = "Returns profit analysis for a date range")
    public ResponseEntity<ProfitReportResponse> getProfitReport(
        @CurrentUser UserPrincipal user,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        @RequestParam(defaultValue = "DAY") String period
    ) {
        ProfitReportResponse report = salesReportService.getProfitReport(
            user.getTenantId(), user.getStoreIdOrDefault(), fromDate, toDate, period);
        return ResponseEntity.ok(report);
    }
}

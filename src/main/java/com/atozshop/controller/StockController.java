package com.atozshop.controller;

import com.atozshop.dto.request.IncomingStockRequest;
import com.atozshop.dto.response.CurrentStockResponse;
import com.atozshop.dto.response.LowStockAlertResponse;
import com.atozshop.dto.response.StockLedgerResponse;
import com.atozshop.dto.response.StockTransactionResponse;
import com.atozshop.security.CurrentUser;
import com.atozshop.security.UserPrincipal;
import com.atozshop.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stock")
@RequiredArgsConstructor
@Tag(name = "Stock Management", description = "APIs for managing inventory stock, transactions, and ledger")
public class StockController {

    private final StockService stockService;

    @PostMapping("/incoming")
    @Operation(summary = "Create incoming stock transaction",
            description = "Creates a draft incoming stock transaction with profit calculation")
    public ResponseEntity<StockTransactionResponse> createIncomingStock(
            @Valid @RequestBody IncomingStockRequest request) {
        StockTransactionResponse response = stockService.createIncomingStock(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/incoming/{id}")
    @Operation(summary = "Get transaction by ID", description = "Retrieves stock transaction details")
    public ResponseEntity<StockTransactionResponse> getTransactionById(
            @CurrentUser UserPrincipal user,
            @PathVariable Long id) {
        StockTransactionResponse response = stockService.getTransactionById(id, user.getTenantId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/incoming")
    @Operation(summary = "Get all transactions", description = "Retrieves all stock transactions with pagination")
    public ResponseEntity<Page<StockTransactionResponse>> getAllTransactions(
            @CurrentUser UserPrincipal user,
            Pageable pageable) {
        Page<StockTransactionResponse> response = stockService.getAllTransactions(user.getTenantId(), pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/incoming/{id}/confirm")
    @Operation(summary = "Confirm incoming stock transaction",
            description = "Confirms the transaction and creates stock ledger entries to update inventory")
    public ResponseEntity<StockTransactionResponse> confirmIncomingStock(
            @CurrentUser UserPrincipal user,
            @PathVariable Long id) {
        StockTransactionResponse response = stockService.confirmIncomingStock(id, user.getTenantId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/incoming/{id}/cancel")
    @Operation(summary = "Cancel transaction", description = "Cancels a draft transaction")
    public ResponseEntity<StockTransactionResponse> cancelTransaction(
            @CurrentUser UserPrincipal user,
            @PathVariable Long id) {
        StockTransactionResponse response = stockService.cancelTransaction(id, user.getTenantId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ledger/variant/{variantId}")
    @Operation(summary = "Get stock ledger for variant",
            description = "Retrieves complete stock movement history for a variant")
    public ResponseEntity<List<StockLedgerResponse>> getVariantLedger(
            @CurrentUser UserPrincipal user,
            @PathVariable Long variantId) {
        List<StockLedgerResponse> response = stockService.getVariantLedger(variantId, user.getStoreIdOrDefault(), user.getTenantId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/levels")
    @Operation(summary = "Get current stock levels",
            description = "Retrieves current stock levels for all variants in a store")
    public ResponseEntity<List<CurrentStockResponse>> getCurrentStockLevels(@CurrentUser UserPrincipal user) {
        List<CurrentStockResponse> response = stockService.getCurrentStockLevels(user.getTenantId(), user.getStoreIdOrDefault());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/current")
    @Operation(summary = "Get current stock", description = "Returns current stock levels for all variants")
    public ResponseEntity<List<CurrentStockResponse>> getCurrentStock(@CurrentUser UserPrincipal user) {
        List<CurrentStockResponse> response = stockService.getCurrentStockLevels(user.getTenantId(), user.getStoreIdOrDefault());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ledger")
    @Operation(summary = "Get stock ledger", description = "Returns stock movement history with pagination")
    public ResponseEntity<Page<StockLedgerResponse>> getStockLedger(
            @CurrentUser UserPrincipal user,
            Pageable pageable) {
        Page<StockLedgerResponse> response = stockService.getStockLedger(user.getTenantId(), user.getStoreIdOrDefault(), pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get low stock alerts", description = "Returns products below reorder level")
    public ResponseEntity<List<LowStockAlertResponse>> getLowStockAlerts(@CurrentUser UserPrincipal user) {
        List<LowStockAlertResponse> response = stockService.getLowStockAlerts(user.getTenantId(), user.getStoreIdOrDefault());
        return ResponseEntity.ok(response);
    }
}

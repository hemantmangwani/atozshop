package com.atozshop.controller;

import com.atozshop.dto.request.CreateVariantRequest;
import com.atozshop.dto.request.UpdateVariantRequest;
import com.atozshop.dto.response.LowStockAlertResponse;
import com.atozshop.dto.response.VariantResponse;
import com.atozshop.security.CurrentUser;
import com.atozshop.security.UserPrincipal;
import com.atozshop.service.ProductVariantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/variants")
@RequiredArgsConstructor
@Tag(name = "Product Variant Management", description = "APIs for managing product variants and stock keeping units (SKUs)")
public class ProductVariantController {

    private final ProductVariantService variantService;

    @PostMapping
    @Operation(summary = "Create a new product variant", description = "Creates a new variant with SKU, barcode, and initial pricing")
    public ResponseEntity<VariantResponse> createVariant(@Valid @RequestBody CreateVariantRequest request) {
        VariantResponse response = variantService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all variants", description = "Retrieves all product variants for the tenant and store")
    public ResponseEntity<List<VariantResponse>> getAllVariants(@CurrentUser UserPrincipal user) {
        List<VariantResponse> variants = variantService.getAll(user.getTenantId(), user.getStoreIdOrDefault());
        return ResponseEntity.ok(variants);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get variant by ID", description = "Retrieves variant details including current stock")
    public ResponseEntity<VariantResponse> getVariantById(
            @CurrentUser UserPrincipal user,
            @PathVariable Long id) {
        VariantResponse response = variantService.getById(id, user.getTenantId(), user.getStoreIdOrDefault());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sku/{sku}")
    @Operation(summary = "Get variant by SKU", description = "Looks up a variant by SKU code")
    public ResponseEntity<VariantResponse> getVariantBySku(
            @CurrentUser UserPrincipal user,
            @PathVariable String sku) {
        VariantResponse response = variantService.getBySku(sku, user.getTenantId(), user.getStoreIdOrDefault());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/barcode/{barcode}")
    @Operation(summary = "Get variant by barcode", description = "Barcode scan lookup - retrieves variant details by barcode")
    public ResponseEntity<VariantResponse> getVariantByBarcode(
            @CurrentUser UserPrincipal user,
            @PathVariable String barcode) {
        VariantResponse response = variantService.getByBarcode(barcode, user.getTenantId(), user.getStoreIdOrDefault());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update variant", description = "Updates variant details and pricing")
    public ResponseEntity<VariantResponse> updateVariant(
            @CurrentUser UserPrincipal user,
            @PathVariable Long id,
            @Valid @RequestBody UpdateVariantRequest request) {
        VariantResponse response = variantService.update(id, user.getTenantId(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get low stock alerts", description = "Retrieves variants with stock below minimum threshold")
    public ResponseEntity<List<LowStockAlertResponse>> getLowStockAlerts(@CurrentUser UserPrincipal user) {
        List<LowStockAlertResponse> response = variantService.getLowStockAlerts(user.getTenantId(), user.getStoreIdOrDefault());
        return ResponseEntity.ok(response);
    }
}

package com.atozshop.controller;

import com.atozshop.dto.request.CreateSupplierRequest;
import com.atozshop.dto.request.UpdateSupplierRequest;
import com.atozshop.dto.response.SupplierResponse;
import com.atozshop.security.CurrentUser;
import com.atozshop.security.UserPrincipal;
import com.atozshop.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
@Tag(name = "Supplier Management", description = "APIs for managing product suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    @Operation(summary = "Create a new supplier")
    public ResponseEntity<SupplierResponse> createSupplier(@Valid @RequestBody CreateSupplierRequest request) {
        SupplierResponse response = supplierService.createSupplier(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get supplier by ID")
    public ResponseEntity<SupplierResponse> getSupplierById(
            @CurrentUser UserPrincipal user,
            @PathVariable Long id) {
        SupplierResponse response = supplierService.getSupplierById(id, user.getTenantId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get supplier by code")
    public ResponseEntity<SupplierResponse> getSupplierByCode(
            @CurrentUser UserPrincipal user,
            @PathVariable String code) {
        SupplierResponse response = supplierService.getSupplierByCode(code, user.getTenantId());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all active suppliers")
    public ResponseEntity<List<SupplierResponse>> getAllSuppliers(@CurrentUser UserPrincipal user) {
        List<SupplierResponse> response = supplierService.getAllSuppliers(user.getTenantId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Search suppliers by name, phone, or email")
    public ResponseEntity<List<SupplierResponse>> searchSuppliers(
            @CurrentUser UserPrincipal user,
            @RequestParam String keyword) {
        List<SupplierResponse> response = supplierService.searchSuppliers(user.getTenantId(), keyword);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update supplier")
    public ResponseEntity<SupplierResponse> updateSupplier(
            @CurrentUser UserPrincipal user,
            @PathVariable Long id,
            @Valid @RequestBody UpdateSupplierRequest request) {
        SupplierResponse response = supplierService.updateSupplier(id, user.getTenantId(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete supplier (soft delete)")
    public ResponseEntity<Void> deleteSupplier(
            @CurrentUser UserPrincipal user,
            @PathVariable Long id) {
        supplierService.deleteSupplier(id, user.getTenantId());
        return ResponseEntity.noContent().build();
    }
}

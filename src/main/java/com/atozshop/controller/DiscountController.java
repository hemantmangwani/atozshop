package com.atozshop.controller;

import com.atozshop.dto.request.CreateDiscountRequest;
import com.atozshop.dto.request.UpdateDiscountRequest;
import com.atozshop.dto.response.DiscountResponse;
import com.atozshop.security.CurrentUser;
import com.atozshop.security.UserPrincipal;
import com.atozshop.service.DiscountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/discounts")
@RequiredArgsConstructor
@Tag(name = "Discount Management", description = "APIs for managing discounts and offers")
public class DiscountController {

    private final DiscountService discountService;

    @PostMapping
    @Operation(summary = "Create a new discount", description = "Creates a new discount/offer")
    public ResponseEntity<DiscountResponse> createDiscount(@Valid @RequestBody CreateDiscountRequest request) {
        DiscountResponse response = discountService.createDiscount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all discounts", description = "Retrieves all active discounts for a tenant")
    public ResponseEntity<List<DiscountResponse>> getAllDiscounts(@CurrentUser UserPrincipal user) {
        List<DiscountResponse> discounts = discountService.getAllDiscounts(user.getTenantId());
        return ResponseEntity.ok(discounts);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active discounts", description = "Retrieves all currently valid and active discounts")
    public ResponseEntity<List<DiscountResponse>> getAllActiveDiscounts(@CurrentUser UserPrincipal user) {
        List<DiscountResponse> discounts = discountService.getAllActiveDiscounts(user.getTenantId());
        return ResponseEntity.ok(discounts);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get discount by ID", description = "Retrieves a specific discount by ID")
    public ResponseEntity<DiscountResponse> getDiscountById(
        @CurrentUser UserPrincipal user,
        @PathVariable Long id
    ) {
        DiscountResponse discount = discountService.getDiscountById(id, user.getTenantId());
        return ResponseEntity.ok(discount);
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get discount by code", description = "Retrieves a discount by its code")
    public ResponseEntity<DiscountResponse> getDiscountByCode(
        @CurrentUser UserPrincipal user,
        @PathVariable String code
    ) {
        DiscountResponse discount = discountService.getDiscountByCode(code, user.getTenantId());
        return ResponseEntity.ok(discount);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update discount", description = "Updates discount information")
    public ResponseEntity<DiscountResponse> updateDiscount(
        @CurrentUser UserPrincipal user,
        @PathVariable Long id,
        @Valid @RequestBody UpdateDiscountRequest request
    ) {
        DiscountResponse discount = discountService.updateDiscount(id, request, user.getTenantId());
        return ResponseEntity.ok(discount);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete discount", description = "Soft deletes a discount (sets isActive to false)")
    public ResponseEntity<Void> deleteDiscount(
        @CurrentUser UserPrincipal user,
        @PathVariable Long id
    ) {
        discountService.deleteDiscount(id, user.getTenantId());
        return ResponseEntity.noContent().build();
    }
}

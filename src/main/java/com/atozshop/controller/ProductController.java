package com.atozshop.controller;

import com.atozshop.dto.request.CreateProductRequest;
import com.atozshop.dto.request.UpdateProductRequest;
import com.atozshop.dto.response.ProductResponse;
import com.atozshop.security.CurrentUser;
import com.atozshop.security.UserPrincipal;
import com.atozshop.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Product Management", description = "APIs for managing products")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @Operation(summary = "Create a new product", description = "Creates a new product with auto-generated slug")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        ProductResponse response = productService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieves product details by ID")
    public ResponseEntity<ProductResponse> getProductById(
            @CurrentUser UserPrincipal user,
            @PathVariable Long id) {
        ProductResponse response = productService.getById(id, user.getTenantId());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all products", description = "Retrieves all products for a tenant with pagination")
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @CurrentUser UserPrincipal user,
            Pageable pageable) {
        Page<ProductResponse> response = productService.getAllByTenant(user.getTenantId(), pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Search products", description = "Searches products by keyword in name, brand, or description")
    public ResponseEntity<Page<ProductResponse>> searchProducts(
            @CurrentUser UserPrincipal user,
            @RequestParam String keyword,
            Pageable pageable) {
        Page<ProductResponse> response = productService.searchProducts(user.getTenantId(), keyword, pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product", description = "Updates an existing product")
    public ResponseEntity<ProductResponse> updateProduct(
            @CurrentUser UserPrincipal user,
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {
        ProductResponse response = productService.update(id, user.getTenantId(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product", description = "Soft deletes a product by setting isActive to false")
    public ResponseEntity<Void> deleteProduct(
            @CurrentUser UserPrincipal user,
            @PathVariable Long id) {
        productService.delete(id, user.getTenantId());
        return ResponseEntity.noContent().build();
    }
}

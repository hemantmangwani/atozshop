package com.atozshop.controller;

import com.atozshop.dto.request.CreateCategoryRequest;
import com.atozshop.dto.request.UpdateCategoryRequest;
import com.atozshop.dto.response.CategoryResponse;
import com.atozshop.security.CurrentUser;
import com.atozshop.security.UserPrincipal;
import com.atozshop.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Category Management", description = "APIs for managing product categories")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @Operation(summary = "Create a new category", description = "Creates a new product category with auto-generated slug")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        CategoryResponse response = categoryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Retrieves category details by ID")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @CurrentUser UserPrincipal user,
            @PathVariable Long id) {
        CategoryResponse response = categoryService.getById(id, user.getTenantId());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all categories", description = "Retrieves all categories for a tenant")
    public ResponseEntity<List<CategoryResponse>> getAllCategories(@CurrentUser UserPrincipal user) {
        List<CategoryResponse> response = categoryService.getAllByTenant(user.getTenantId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/subcategories")
    @Operation(summary = "Get subcategories", description = "Retrieves all subcategories of a parent category")
    public ResponseEntity<List<CategoryResponse>> getSubcategories(
            @CurrentUser UserPrincipal user,
            @PathVariable Long id) {
        List<CategoryResponse> response = categoryService.getSubcategories(id, user.getTenantId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update category", description = "Updates an existing category")
    public ResponseEntity<CategoryResponse> updateCategory(
            @CurrentUser UserPrincipal user,
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        CategoryResponse response = categoryService.update(id, user.getTenantId(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category", description = "Soft deletes a category by setting isActive to false")
    public ResponseEntity<Void> deleteCategory(
            @CurrentUser UserPrincipal user,
            @PathVariable Long id) {
        categoryService.delete(id, user.getTenantId());
        return ResponseEntity.noContent().build();
    }
}

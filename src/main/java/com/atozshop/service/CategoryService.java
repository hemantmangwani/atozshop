package com.atozshop.service;

import com.atozshop.dto.request.CreateCategoryRequest;
import com.atozshop.dto.request.UpdateCategoryRequest;
import com.atozshop.dto.response.CategoryResponse;
import com.atozshop.entity.Category;
import com.atozshop.repository.CategoryRepository;
import com.atozshop.util.SlugGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryResponse create(CreateCategoryRequest request) {
        // Validate parent category if provided
        if (request.getParentId() != null) {
            categoryRepository.findByIdAndTenantId(request.getParentId(), request.getTenantId())
                    .orElseThrow(() -> new RuntimeException("Parent category not found"));
        }

        // Generate slug
        String slug = SlugGenerator.generateSlug(request.getName());

        // Check if slug already exists
        if (categoryRepository.existsBySlugAndTenantId(slug, request.getTenantId())) {
            slug = slug + "-" + System.currentTimeMillis();
        }

        Category category = Category.builder()
                .tenantId(request.getTenantId())
                .parentId(request.getParentId())
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        Category saved = categoryRepository.save(category);
        return mapToResponse(saved);
    }

    public CategoryResponse getById(Long id, Long tenantId) {
        Category category = categoryRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return mapToResponse(category);
    }

    public List<CategoryResponse> getAllByTenant(Long tenantId) {
        return categoryRepository.findByTenantIdOrderBySortOrder(tenantId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<CategoryResponse> getSubcategories(Long parentId, Long tenantId) {
        return categoryRepository.findByTenantIdAndParentIdOrderBySortOrder(tenantId, parentId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public CategoryResponse update(Long id, Long tenantId, UpdateCategoryRequest request) {
        Category category = categoryRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // Validate parent category if being updated
        if (request.getParentId() != null && !request.getParentId().equals(category.getParentId())) {
            // Check for circular reference
            if (request.getParentId().equals(id)) {
                throw new RuntimeException("Category cannot be its own parent");
            }
            categoryRepository.findByIdAndTenantId(request.getParentId(), tenantId)
                    .orElseThrow(() -> new RuntimeException("Parent category not found"));
        }

        if (request.getName() != null && !request.getName().equals(category.getName())) {
            category.setName(request.getName());
            String newSlug = SlugGenerator.generateSlug(request.getName());
            if (!newSlug.equals(category.getSlug()) && categoryRepository.existsBySlugAndTenantId(newSlug, tenantId)) {
                newSlug = newSlug + "-" + System.currentTimeMillis();
            }
            category.setSlug(newSlug);
        }

        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }
        if (request.getImageUrl() != null) {
            category.setImageUrl(request.getImageUrl());
        }
        if (request.getSortOrder() != null) {
            category.setSortOrder(request.getSortOrder());
        }
        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }
        if (request.getParentId() != null) {
            category.setParentId(request.getParentId());
        }

        Category updated = categoryRepository.save(category);
        return mapToResponse(updated);
    }

    public void delete(Long id, Long tenantId) {
        Category category = categoryRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        category.setIsActive(false);
        categoryRepository.save(category);
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .tenantId(category.getTenantId())
                .parentId(category.getParentId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .sortOrder(category.getSortOrder())
                .isActive(category.getIsActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}

package com.atozshop.service;

import com.atozshop.dto.request.CreateProductRequest;
import com.atozshop.dto.request.UpdateProductRequest;
import com.atozshop.dto.response.ProductResponse;
import com.atozshop.entity.Category;
import com.atozshop.entity.Product;
import com.atozshop.repository.CategoryRepository;
import com.atozshop.repository.ProductRepository;
import com.atozshop.util.SlugGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductResponse create(CreateProductRequest request) {
        // Validate category exists
        Category category = categoryRepository.findByIdAndTenantId(request.getCategoryId(), request.getTenantId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // Generate slug
        String slug = SlugGenerator.generateSlug(request.getName());

        // Check if slug already exists
        if (productRepository.existsBySlugAndTenantId(slug, request.getTenantId())) {
            slug = slug + "-" + System.currentTimeMillis();
        }

        Product product = Product.builder()
                .tenantId(request.getTenantId())
                .categoryId(request.getCategoryId())
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .brand(request.getBrand())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        Product saved = productRepository.save(product);
        return mapToResponse(saved, category.getName());
    }

    public ProductResponse getById(Long id, Long tenantId) {
        Product product = productRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        String categoryName = categoryRepository.findById(product.getCategoryId())
                .map(Category::getName)
                .orElse(null);

        return mapToResponse(product, categoryName);
    }

    public Page<ProductResponse> getAllByTenant(Long tenantId, Pageable pageable) {
        return productRepository.findByTenantId(tenantId, pageable)
                .map(product -> {
                    String categoryName = categoryRepository.findById(product.getCategoryId())
                            .map(Category::getName)
                            .orElse(null);
                    return mapToResponse(product, categoryName);
                });
    }

    public Page<ProductResponse> searchProducts(Long tenantId, String keyword, Pageable pageable) {
        return productRepository.searchProducts(tenantId, keyword, pageable)
                .map(product -> {
                    String categoryName = categoryRepository.findById(product.getCategoryId())
                            .map(Category::getName)
                            .orElse(null);
                    return mapToResponse(product, categoryName);
                });
    }

    public ProductResponse update(Long id, Long tenantId, UpdateProductRequest request) {
        Product product = productRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Validate category if being updated
        if (request.getCategoryId() != null && !request.getCategoryId().equals(product.getCategoryId())) {
            categoryRepository.findByIdAndTenantId(request.getCategoryId(), tenantId)
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategoryId(request.getCategoryId());
        }

        if (request.getName() != null && !request.getName().equals(product.getName())) {
            product.setName(request.getName());
            String newSlug = SlugGenerator.generateSlug(request.getName());
            if (!newSlug.equals(product.getSlug()) && productRepository.existsBySlugAndTenantId(newSlug, tenantId)) {
                newSlug = newSlug + "-" + System.currentTimeMillis();
            }
            product.setSlug(newSlug);
        }

        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getBrand() != null) {
            product.setBrand(request.getBrand());
        }
        if (request.getIsActive() != null) {
            product.setIsActive(request.getIsActive());
        }

        Product updated = productRepository.save(product);

        String categoryName = categoryRepository.findById(updated.getCategoryId())
                .map(Category::getName)
                .orElse(null);

        return mapToResponse(updated, categoryName);
    }

    public void delete(Long id, Long tenantId) {
        Product product = productRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setIsActive(false);
        productRepository.save(product);
    }

    private ProductResponse mapToResponse(Product product, String categoryName) {
        return ProductResponse.builder()
                .id(product.getId())
                .tenantId(product.getTenantId())
                .categoryId(product.getCategoryId())
                .categoryName(categoryName)
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .brand(product.getBrand())
                .isActive(product.getIsActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}

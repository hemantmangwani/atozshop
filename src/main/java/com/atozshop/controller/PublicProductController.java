package com.atozshop.controller;

import com.atozshop.dto.response.PublicProductResponse;
import com.atozshop.dto.response.StockAvailabilityResponse;
import com.atozshop.entity.Category;
import com.atozshop.entity.Product;
import com.atozshop.entity.ProductVariant;
import com.atozshop.entity.VariantPrice;
import com.atozshop.repository.*;
import com.atozshop.service.StockReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/public/products")
@RequiredArgsConstructor
@Tag(name = "Public Product Catalog", description = "Customer-facing product catalog APIs")
public class PublicProductController {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final CategoryRepository categoryRepository;
    private final VariantPriceRepository priceRepository;
    private final StockLedgerRepository stockLedgerRepository;
    private final StockReservationRepository reservationRepository;
    private final StockReservationService reservationService;

    @GetMapping
    @Operation(summary = "Get all products", description = "List all products with availability for customer browsing")
    public ResponseEntity<List<PublicProductResponse>> getAllProducts(
            @RequestParam(required = false, defaultValue = "1") Long tenantId,
            @RequestParam(required = false, defaultValue = "1") Long storeId,
            @RequestParam(required = false) Long categoryId
    ) {
        List<Product> products;

        if (categoryId != null) {
            products = productRepository.findByCategoryIdAndTenantId(categoryId, tenantId);
        } else {
            products = productRepository.findByTenantIdAndIsActive(tenantId, true);
        }

        List<PublicProductResponse> responses = products.stream()
                .map(product -> buildPublicProductResponse(product, storeId, tenantId))
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product details", description = "Get detailed product information with all variants and availability")
    public ResponseEntity<PublicProductResponse> getProductById(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "1") Long tenantId,
            @RequestParam(required = false, defaultValue = "1") Long storeId
    ) {
        Product product = productRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        PublicProductResponse response = buildPublicProductResponse(product, storeId, tenantId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Search products", description = "Search products by name or SKU")
    public ResponseEntity<List<PublicProductResponse>> searchProducts(
            @RequestParam(required = false, defaultValue = "1") Long tenantId,
            @RequestParam(required = false, defaultValue = "1") Long storeId,
            @RequestParam String keyword
    ) {
        List<Product> products = productRepository.searchProductsList(tenantId, keyword);

        List<PublicProductResponse> responses = products.stream()
                .map(product -> buildPublicProductResponse(product, storeId, tenantId))
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get products by category", description = "List all products in a specific category")
    public ResponseEntity<List<PublicProductResponse>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(required = false, defaultValue = "1") Long tenantId,
            @RequestParam(required = false, defaultValue = "1") Long storeId
    ) {
        List<Product> products = productRepository.findByCategoryIdAndTenantId(categoryId, tenantId);

        List<PublicProductResponse> responses = products.stream()
                .map(product -> buildPublicProductResponse(product, storeId, tenantId))
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/variant/{variantId}/availability")
    @Operation(summary = "Check stock availability", description = "Get real-time stock availability for a variant")
    public ResponseEntity<StockAvailabilityResponse> checkAvailability(
            @PathVariable Long variantId,
            @RequestParam(required = false, defaultValue = "1") Long tenantId,
            @RequestParam(required = false, defaultValue = "1") Long storeId
    ) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Variant not found"));

        Integer currentStock = stockLedgerRepository.getCurrentStock(variantId, storeId, tenantId);
        if (currentStock == null) currentStock = 0;

        Integer reservedStock = reservationRepository.getTotalReservedStock(variantId, storeId, tenantId);
        if (reservedStock == null) reservedStock = 0;

        Integer soldStock = 0;  // Can be calculated from sales transactions if needed

        StockAvailabilityResponse response = StockAvailabilityResponse.from(
                variantId, variant.getSku(), currentStock, soldStock, reservedStock
        );

        return ResponseEntity.ok(response);
    }

    // Helper methods

    private PublicProductResponse buildPublicProductResponse(Product product, Long storeId, Long tenantId) {
        // Get default variant (first active variant)
        List<ProductVariant> variants = variantRepository.findByProductIdAndTenantId(product.getId(), tenantId);

        if (variants.isEmpty()) {
            return buildEmptyProductResponse(product);
        }

        ProductVariant defaultVariant = variants.get(0);

        // Get price for default variant
        VariantPrice price = priceRepository.findCurrentPrice(
                tenantId, defaultVariant.getId(), storeId, LocalDate.now()
        ).orElse(null);

        // Get stock availability
        Integer availableStock = reservationService.getAvailableStock(defaultVariant.getId(), storeId, tenantId);
        String stockStatus = getStockStatus(availableStock);
        boolean isAvailable = availableStock > 0;

        // Build variant info list
        List<PublicProductResponse.VariantInfo> variantInfoList = variants.stream()
                .map(v -> buildVariantInfo(v, storeId, tenantId))
                .collect(Collectors.toList());

        // Get category name
        String categoryName = null;
        if (product.getCategoryId() != null) {
            Category category = categoryRepository.findById(product.getCategoryId()).orElse(null);
            if (category != null) {
                categoryName = category.getName();
            }
        }

        // Calculate discount percentage
        BigDecimal discountPercent = BigDecimal.ZERO;
        if (price != null && price.getMrp() != null && price.getSellingPrice() != null) {
            if (price.getMrp().compareTo(price.getSellingPrice()) > 0) {
                BigDecimal discount = price.getMrp().subtract(price.getSellingPrice());
                discountPercent = discount.divide(price.getMrp(), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100))
                        .setScale(2, RoundingMode.HALF_UP);
            }
        }

        return PublicProductResponse.builder()
                .id(product.getId())
                .sku(defaultVariant.getSku())  // Use variant SKU
                .name(product.getName())
                .description(product.getDescription())
                .categoryName(categoryName)
                .brandName(product.getBrand())
                .defaultVariantId(defaultVariant.getId())
                .defaultVariantName(defaultVariant.getVariantName())
                .sellingPrice(price != null ? price.getSellingPrice() : BigDecimal.ZERO)
                .mrp(price != null ? price.getMrp() : BigDecimal.ZERO)
                .discountPercent(discountPercent)
                .availableStock(availableStock)
                .stockStatus(stockStatus)
                .isAvailable(isAvailable)
                .variants(variantInfoList)
                .build();
    }

    private PublicProductResponse buildEmptyProductResponse(Product product) {
        return PublicProductResponse.builder()
                .id(product.getId())
                .sku("")  // No variant, no SKU
                .name(product.getName())
                .description(product.getDescription())
                .isAvailable(false)
                .stockStatus("Out of Stock")
                .availableStock(0)
                .build();
    }

    private PublicProductResponse.VariantInfo buildVariantInfo(ProductVariant variant, Long storeId, Long tenantId) {
        VariantPrice price = priceRepository.findCurrentPrice(
                tenantId, variant.getId(), storeId, LocalDate.now()
        ).orElse(null);

        Integer availableStock = reservationService.getAvailableStock(variant.getId(), storeId, tenantId);
        String stockStatus = getStockStatus(availableStock);

        return PublicProductResponse.VariantInfo.builder()
                .id(variant.getId())
                .name(variant.getVariantName())
                .sku(variant.getSku())
                .sellingPrice(price != null ? price.getSellingPrice() : BigDecimal.ZERO)
                .mrp(price != null ? price.getMrp() : BigDecimal.ZERO)
                .availableStock(availableStock)
                .stockStatus(stockStatus)
                .build();
    }

    private String getStockStatus(Integer availableStock) {
        if (availableStock == null || availableStock <= 0) {
            return "Out of Stock";
        } else if (availableStock <= 5) {
            return "Low Stock";
        } else {
            return "In Stock";
        }
    }
}

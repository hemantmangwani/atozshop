package com.atozshop.service;

import com.atozshop.dto.request.CreateVariantRequest;
import com.atozshop.dto.request.UpdateVariantRequest;
import com.atozshop.dto.response.LowStockAlertResponse;
import com.atozshop.dto.response.VariantResponse;
import com.atozshop.entity.Product;
import com.atozshop.entity.ProductVariant;
import com.atozshop.entity.VariantPrice;
import com.atozshop.repository.ProductRepository;
import com.atozshop.repository.ProductVariantRepository;
import com.atozshop.repository.StockLedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductVariantService {

    private final ProductVariantRepository variantRepository;
    private final ProductRepository productRepository;
    private final StockLedgerRepository stockLedgerRepository;
    private final VariantPriceService variantPriceService;

    @Transactional
    public VariantResponse create(CreateVariantRequest request) {
        // Validate product exists
        Product product = productRepository.findByIdAndTenantId(request.getProductId(), request.getTenantId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Validate unique SKU
        if (variantRepository.existsBySkuAndTenantId(request.getSku(), request.getTenantId())) {
            throw new RuntimeException("SKU already exists");
        }

        // Validate unique barcode if provided
        if (request.getBarcodeValue() != null &&
                variantRepository.existsByBarcodeValueAndTenantId(request.getBarcodeValue(), request.getTenantId())) {
            throw new RuntimeException("Barcode already exists");
        }

        ProductVariant variant = ProductVariant.builder()
                .tenantId(request.getTenantId())
                .productId(request.getProductId())
                .sku(request.getSku())
                .variantName(request.getVariantName())
                .unit(request.getUnit())
                .barcodeValue(request.getBarcodeValue())
                .qrValue(request.getQrValue())
                .minStockThreshold(request.getMinStockThreshold() != null ? request.getMinStockThreshold() : 0)
                .maxStockThreshold(request.getMaxStockThreshold())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        ProductVariant saved = variantRepository.save(variant);

        // Create initial price record
        variantPriceService.createOrUpdatePrice(
                request.getTenantId(),
                saved.getId(),
                request.getStoreId(),
                request.getCostPrice(),
                request.getSellingPrice(),
                request.getMrp()
        );

        return mapToResponse(saved, product.getName(), 0, request.getCostPrice(), request.getSellingPrice(), request.getMrp());
    }

    public List<VariantResponse> getAll(Long tenantId, Long storeId) {
        List<ProductVariant> variants = variantRepository.findByTenantId(tenantId);

        return variants.stream()
                .map(variant -> {
                    String productName = productRepository.findById(variant.getProductId())
                            .map(Product::getName)
                            .orElse(null);

                    Integer currentStock = stockLedgerRepository.getCurrentStock(variant.getId(), storeId, tenantId);

                    VariantPrice currentPrice = variantPriceService.getCurrentPrice(tenantId, variant.getId(), storeId);

                    return mapToResponse(variant, productName, currentStock,
                            currentPrice != null ? currentPrice.getCostPrice() : null,
                            currentPrice != null ? currentPrice.getSellingPrice() : null,
                            currentPrice != null ? currentPrice.getMrp() : null);
                })
                .collect(Collectors.toList());
    }

    public VariantResponse getById(Long id, Long tenantId, Long storeId) {
        ProductVariant variant = variantRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Product variant not found"));

        String productName = productRepository.findById(variant.getProductId())
                .map(Product::getName)
                .orElse(null);

        Integer currentStock = stockLedgerRepository.getCurrentStock(id, storeId, tenantId);

        VariantPrice currentPrice = variantPriceService.getCurrentPrice(tenantId, id, storeId);

        return mapToResponse(variant, productName, currentStock,
                currentPrice != null ? currentPrice.getCostPrice() : null,
                currentPrice != null ? currentPrice.getSellingPrice() : null,
                currentPrice != null ? currentPrice.getMrp() : null);
    }

    public VariantResponse getBySku(String sku, Long tenantId, Long storeId) {
        ProductVariant variant = variantRepository.findBySkuAndTenantId(sku, tenantId)
                .orElseThrow(() -> new RuntimeException("Product variant not found"));

        String productName = productRepository.findById(variant.getProductId())
                .map(Product::getName)
                .orElse(null);

        Integer currentStock = stockLedgerRepository.getCurrentStock(variant.getId(), storeId, tenantId);

        VariantPrice currentPrice = variantPriceService.getCurrentPrice(tenantId, variant.getId(), storeId);

        return mapToResponse(variant, productName, currentStock,
                currentPrice != null ? currentPrice.getCostPrice() : null,
                currentPrice != null ? currentPrice.getSellingPrice() : null,
                currentPrice != null ? currentPrice.getMrp() : null);
    }

    public VariantResponse getByBarcode(String barcode, Long tenantId, Long storeId) {
        ProductVariant variant = variantRepository.findByBarcodeValueAndTenantId(barcode, tenantId)
                .orElseThrow(() -> new RuntimeException("Product variant not found"));

        String productName = productRepository.findById(variant.getProductId())
                .map(Product::getName)
                .orElse(null);

        Integer currentStock = stockLedgerRepository.getCurrentStock(variant.getId(), storeId, tenantId);

        VariantPrice currentPrice = variantPriceService.getCurrentPrice(tenantId, variant.getId(), storeId);

        return mapToResponse(variant, productName, currentStock,
                currentPrice != null ? currentPrice.getCostPrice() : null,
                currentPrice != null ? currentPrice.getSellingPrice() : null,
                currentPrice != null ? currentPrice.getMrp() : null);
    }

    @Transactional
    public VariantResponse update(Long id, Long tenantId, UpdateVariantRequest request) {
        ProductVariant variant = variantRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Product variant not found"));

        // Validate unique barcode if being updated
        if (request.getBarcodeValue() != null && !request.getBarcodeValue().equals(variant.getBarcodeValue())) {
            if (variantRepository.existsByBarcodeValueAndTenantId(request.getBarcodeValue(), tenantId)) {
                throw new RuntimeException("Barcode already exists");
            }
            variant.setBarcodeValue(request.getBarcodeValue());
        }

        if (request.getVariantName() != null) {
            variant.setVariantName(request.getVariantName());
        }
        if (request.getUnit() != null) {
            variant.setUnit(request.getUnit());
        }
        if (request.getQrValue() != null) {
            variant.setQrValue(request.getQrValue());
        }
        if (request.getMinStockThreshold() != null) {
            variant.setMinStockThreshold(request.getMinStockThreshold());
        }
        if (request.getMaxStockThreshold() != null) {
            variant.setMaxStockThreshold(request.getMaxStockThreshold());
        }
        if (request.getIsActive() != null) {
            variant.setIsActive(request.getIsActive());
        }

        ProductVariant updated = variantRepository.save(variant);

        // Update price if changed
        if (request.getCostPrice() != null || request.getSellingPrice() != null) {
            VariantPrice currentPrice = variantPriceService.getCurrentPrice(tenantId, id, request.getStoreId());
            variantPriceService.createOrUpdatePrice(
                    tenantId,
                    id,
                    request.getStoreId(),
                    request.getCostPrice() != null ? request.getCostPrice() : currentPrice.getCostPrice(),
                    request.getSellingPrice() != null ? request.getSellingPrice() : currentPrice.getSellingPrice(),
                    request.getMrp() != null ? request.getMrp() : currentPrice.getMrp()
            );
        }

        String productName = productRepository.findById(updated.getProductId())
                .map(Product::getName)
                .orElse(null);

        Integer currentStock = stockLedgerRepository.getCurrentStock(id, request.getStoreId(), tenantId);
        VariantPrice updatedPrice = variantPriceService.getCurrentPrice(tenantId, id, request.getStoreId());

        return mapToResponse(updated, productName, currentStock,
                updatedPrice != null ? updatedPrice.getCostPrice() : null,
                updatedPrice != null ? updatedPrice.getSellingPrice() : null,
                updatedPrice != null ? updatedPrice.getMrp() : null);
    }

    public List<LowStockAlertResponse> getLowStockAlerts(Long tenantId, Long storeId) {
        List<ProductVariant> lowStockVariants = variantRepository.findLowStockVariants(tenantId, storeId);

        return lowStockVariants.stream()
                .map(variant -> {
                    String productName = productRepository.findById(variant.getProductId())
                            .map(Product::getName)
                            .orElse(null);

                    Integer currentStock = stockLedgerRepository.getCurrentStock(variant.getId(), storeId, tenantId);
                    int shortfall = variant.getMinStockThreshold() - currentStock;

                    String alertLevel;
                    if (currentStock == 0) {
                        alertLevel = "OUT_OF_STOCK";
                    } else if (currentStock <= variant.getMinStockThreshold() / 2) {
                        alertLevel = "CRITICAL";
                    } else {
                        alertLevel = "LOW";
                    }

                    return LowStockAlertResponse.builder()
                            .variantId(variant.getId())
                            .sku(variant.getSku())
                            .variantName(variant.getVariantName())
                            .productName(productName)
                            .currentStock(currentStock)
                            .minStockThreshold(variant.getMinStockThreshold())
                            .shortfall(shortfall)
                            .alertLevel(alertLevel)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private VariantResponse mapToResponse(ProductVariant variant, String productName,
                                           Integer currentStock,
                                           java.math.BigDecimal costPrice,
                                           java.math.BigDecimal sellingPrice,
                                           java.math.BigDecimal mrp) {
        return VariantResponse.builder()
                .id(variant.getId())
                .tenantId(variant.getTenantId())
                .productId(variant.getProductId())
                .productName(productName)
                .sku(variant.getSku())
                .variantName(variant.getVariantName())
                .unit(variant.getUnit())
                .barcodeValue(variant.getBarcodeValue())
                .qrValue(variant.getQrValue())
                .costPrice(costPrice)
                .sellingPrice(sellingPrice)
                .mrp(mrp)
                .minStockThreshold(variant.getMinStockThreshold())
                .maxStockThreshold(variant.getMaxStockThreshold())
                .currentStock(currentStock)
                .isActive(variant.getIsActive())
                .createdAt(variant.getCreatedAt())
                .updatedAt(variant.getUpdatedAt())
                .build();
    }
}

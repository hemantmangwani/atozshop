package com.atozshop.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateVariantRequest {

    @NotNull(message = "Tenant ID is required")
    private Long tenantId;

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotBlank(message = "SKU is required")
    @Size(max = 50, message = "SKU must not exceed 50 characters")
    private String sku;

    @Size(max = 200, message = "Variant name must not exceed 200 characters")
    private String variantName;

    @Size(max = 50, message = "Unit must not exceed 50 characters")
    private String unit;

    @Size(max = 100, message = "Barcode must not exceed 100 characters")
    private String barcodeValue;

    @Size(max = 255, message = "QR value must not exceed 255 characters")
    private String qrValue;

    @NotNull(message = "Cost price is required")
    @Positive(message = "Cost price must be positive")
    private BigDecimal costPrice;

    @NotNull(message = "Selling price is required")
    @Positive(message = "Selling price must be positive")
    private BigDecimal sellingPrice;

    private BigDecimal mrp;

    private Long storeId;

    private Integer minStockThreshold;

    private Integer maxStockThreshold;

    private Boolean isActive;
}

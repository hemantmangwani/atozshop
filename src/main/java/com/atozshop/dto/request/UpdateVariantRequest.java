package com.atozshop.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateVariantRequest {

    @Size(max = 200, message = "Variant name must not exceed 200 characters")
    private String variantName;

    @Size(max = 50, message = "Unit must not exceed 50 characters")
    private String unit;

    @Size(max = 100, message = "Barcode must not exceed 100 characters")
    private String barcodeValue;

    @Size(max = 255, message = "QR value must not exceed 255 characters")
    private String qrValue;

    @Positive(message = "Cost price must be positive")
    private BigDecimal costPrice;

    @Positive(message = "Selling price must be positive")
    private BigDecimal sellingPrice;

    private BigDecimal mrp;

    private Long storeId;

    private Integer minStockThreshold;

    private Integer maxStockThreshold;

    private Boolean isActive;
}

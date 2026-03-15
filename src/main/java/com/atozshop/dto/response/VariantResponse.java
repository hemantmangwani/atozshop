package com.atozshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariantResponse {

    private Long id;
    private Long tenantId;
    private Long productId;
    private String productName;
    private String sku;
    private String variantName;
    private String unit;
    private String barcodeValue;
    private String qrValue;
    private BigDecimal costPrice;
    private BigDecimal sellingPrice;
    private BigDecimal mrp;
    private Integer minStockThreshold;
    private Integer maxStockThreshold;
    private Integer currentStock;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

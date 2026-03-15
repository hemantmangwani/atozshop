package com.atozshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {

    private Long id;
    private Long variantId;
    private String sku;
    private String productName;
    private String variantName;

    // Pricing
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;

    // Fulfillment
    private Integer fulfilledQuantity;
    private Long substitutedVariantId;
    private String substitutedVariantName;
    private String substitutionReason;

    // Availability
    private Integer availableStock;
    private String stockStatus;  // "In Stock", "Low Stock", "Out of Stock"
}

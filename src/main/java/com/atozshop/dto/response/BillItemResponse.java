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
public class BillItemResponse {

    private Long id;
    private Long billId;
    private Long variantId;
    private String sku;
    private String productName;
    private String variantName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal mrp;
    private BigDecimal discountPercent;
    private BigDecimal discountAmount;
    private BigDecimal subtotal;
    private BigDecimal taxPercent;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
}

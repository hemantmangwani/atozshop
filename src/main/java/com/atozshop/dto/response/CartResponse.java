package com.atozshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponse {

    private Long customerId;
    private List<CartItemResponse> items;
    private Integer totalItems;
    private Integer totalQuantity;
    private BigDecimal subtotal;
    private BigDecimal estimatedTax;
    private BigDecimal estimatedTotal;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItemResponse {
        private Long variantId;
        private String sku;
        private String productName;
        private String variantName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
        private Integer availableStock;
        private String stockStatus;
        private String imageUrl;
    }
}

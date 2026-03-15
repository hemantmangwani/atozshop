package com.atozshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockAvailabilityResponse {

    private Long variantId;
    private String sku;
    private Integer totalStock;
    private Integer soldStock;
    private Integer reservedStock;
    private Integer availableStock;  // totalStock - soldStock - reservedStock
    private String stockStatus;      // "In Stock", "Low Stock", "Out of Stock"
    private Boolean isAvailable;
    private String message;          // e.g., "Only 3 left in stock!"

    public static StockAvailabilityResponse from(Long variantId, String sku, Integer totalStock, Integer soldStock, Integer reservedStock) {
        int available = totalStock - soldStock - reservedStock;
        String status;
        String message;
        boolean isAvailable;

        if (available <= 0) {
            status = "Out of Stock";
            message = "Currently unavailable";
            isAvailable = false;
        } else if (available <= 5) {
            status = "Low Stock";
            message = "Only " + available + " left in stock!";
            isAvailable = true;
        } else {
            status = "In Stock";
            message = "Available";
            isAvailable = true;
        }

        return StockAvailabilityResponse.builder()
                .variantId(variantId)
                .sku(sku)
                .totalStock(totalStock)
                .soldStock(soldStock)
                .reservedStock(reservedStock)
                .availableStock(available)
                .stockStatus(status)
                .isAvailable(isAvailable)
                .message(message)
                .build();
    }
}

package com.atozshop.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnItemRequest {

    @NotNull(message = "Bill item ID is required")
    private Long billItemId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Return quantity must be at least 1")
    private Integer quantity;

    private String itemReturnReason;  // Optional item-specific reason
}

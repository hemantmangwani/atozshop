package com.atozshop.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class IncomingStockRequest {

    @NotNull(message = "Tenant ID is required")
    private Long tenantId;

    @NotNull(message = "Store ID is required")
    private Long storeId;

    @Size(max = 200, message = "Supplier name must not exceed 200 characters")
    private String supplierName;

    private LocalDateTime transactionDate;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<IncomingStockItemRequest> items;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    private Long createdBy;
}

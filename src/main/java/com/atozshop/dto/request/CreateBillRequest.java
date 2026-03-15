package com.atozshop.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBillRequest {

    @NotNull(message = "Tenant ID is required")
    private Long tenantId;

    @NotNull(message = "Store ID is required")
    private Long storeId;

    private Long customerId;  // Optional - null for walk-in

    @NotNull(message = "Bill type is required")
    private String billType;  // SALES, SALES_RETURN

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<AddBillItemRequest> items;

    @Valid
    private List<ApplyDiscountRequest> discounts;

    private String notes;

    private Long cashierId;
}

package com.atozshop.dto.request;

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
public class CreateReturnRequest {

    @NotNull(message = "Tenant ID is required")
    private Long tenantId;

    @NotNull(message = "Original bill ID is required")
    private Long originalBillId;

    @NotEmpty(message = "At least one item must be returned")
    private List<ReturnItemRequest> items;

    private String returnReason;  // DEFECTIVE, WRONG_ITEM, CUSTOMER_REQUEST, etc.
    private String notes;
}

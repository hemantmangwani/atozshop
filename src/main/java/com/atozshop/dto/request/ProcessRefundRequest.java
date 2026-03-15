package com.atozshop.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessRefundRequest {

    @NotNull(message = "Return bill ID is required")
    private Long returnBillId;

    @NotNull(message = "Tenant ID is required")
    private Long tenantId;

    @NotNull(message = "Refund method is required")
    private String refundMethod;  // CASH, CARD, UPI, ORIGINAL_METHOD

    @NotNull(message = "Refund amount is required")
    @DecimalMin(value = "0.01", message = "Refund amount must be greater than 0")
    private BigDecimal refundAmount;

    private String referenceNumber;  // For card/UPI refunds
    private String notes;
}

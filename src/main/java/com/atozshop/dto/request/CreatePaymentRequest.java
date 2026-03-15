package com.atozshop.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
public class CreatePaymentRequest {

    @NotNull(message = "Bill ID is required")
    private Long billId;

    @NotNull(message = "Tenant ID is required")
    private Long tenantId;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;  // CASH, CARD, UPI, WALLET, CHEQUE

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    private String referenceNumber;  // For digital payments
    private String cardLast4;
    private String upiId;
    private String bankName;
    private String notes;
    private Long createdBy;
}

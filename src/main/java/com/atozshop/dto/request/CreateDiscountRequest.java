package com.atozshop.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDiscountRequest {

    @NotNull(message = "Tenant ID is required")
    private Long tenantId;

    @NotBlank(message = "Discount code is required")
    private String discountCode;

    @NotBlank(message = "Discount name is required")
    private String name;

    private String description;

    @NotBlank(message = "Discount type is required")
    private String discountType;  // PERCENTAGE, FIXED_AMOUNT

    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Discount value must be positive")
    private BigDecimal discountValue;

    private BigDecimal minPurchaseAmount;
    private BigDecimal maxDiscountAmount;

    @NotBlank(message = "Applicable on is required")
    private String applicableOn;  // ITEM, BILL, CATEGORY

    private LocalDate validFrom;
    private LocalDate validTo;
}

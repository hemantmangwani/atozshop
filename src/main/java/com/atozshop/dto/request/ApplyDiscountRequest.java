package com.atozshop.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplyDiscountRequest {

    private String discountCode;  // If using predefined discount

    // For ad-hoc discounts
    private String discountName;

    @NotBlank(message = "Discount type is required")
    private String discountType;  // PERCENTAGE, FIXED_AMOUNT

    @DecimalMin(value = "0.0", inclusive = false, message = "Discount value must be positive")
    private BigDecimal discountValue;
}

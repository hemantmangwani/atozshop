package com.atozshop.dto.request;

import jakarta.validation.constraints.DecimalMin;
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
public class UpdateDiscountRequest {

    private String name;
    private String description;
    private String discountType;

    @DecimalMin(value = "0.0", inclusive = false, message = "Discount value must be positive")
    private BigDecimal discountValue;

    private BigDecimal minPurchaseAmount;
    private BigDecimal maxDiscountAmount;
    private String applicableOn;
    private LocalDate validFrom;
    private LocalDate validTo;
    private Boolean isActive;
}

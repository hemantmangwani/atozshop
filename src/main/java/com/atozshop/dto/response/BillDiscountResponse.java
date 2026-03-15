package com.atozshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillDiscountResponse {

    private Long id;
    private Long billId;
    private Long discountId;
    private String discountName;
    private String discountCode;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal discountAmount;
}

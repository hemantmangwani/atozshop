package com.atozshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillSummaryResponse {

    private Long id;
    private String billNumber;
    private LocalDateTime billDate;
    private String billType;
    private String status;
    private String paymentStatus;
    private String customerName;
    private String customerPhone;
    private Integer totalItems;
    private Integer totalQuantity;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal balanceAmount;
}

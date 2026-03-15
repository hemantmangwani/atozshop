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
public class PaymentResponse {

    private Long id;
    private Long billId;
    private Long tenantId;
    private String paymentMethod;
    private LocalDateTime paymentDate;
    private BigDecimal amount;
    private String referenceNumber;
    private String cardLast4;
    private String upiId;
    private String bankName;
    private String notes;
    private Long createdBy;
    private LocalDateTime createdAt;
}

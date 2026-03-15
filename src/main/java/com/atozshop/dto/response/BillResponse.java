package com.atozshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillResponse {

    private Long id;
    private Long tenantId;
    private Long storeId;
    private String billNumber;
    private LocalDateTime billDate;
    private String billType;
    private String status;
    private String paymentStatus;

    // Customer info
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String customerCode;

    // Cashier info
    private Long cashierId;
    private Long createdBy;

    // Items
    private List<BillItemResponse> items;
    private Integer totalItems;
    private Integer totalQuantity;

    // Amounts
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal balanceAmount;

    // Payments and discounts
    private List<PaymentResponse> payments;
    private List<BillDiscountResponse> discounts;

    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

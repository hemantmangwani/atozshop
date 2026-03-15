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
public class ReceiptResponse {

    // Store info
    private String storeName;
    private String storeAddress;
    private String storePhone;
    private String storeGstin;

    // Bill info
    private String billNumber;
    private LocalDateTime billDate;
    private String billType;

    // Customer info (optional)
    private String customerName;
    private String customerPhone;
    private String customerGstin;

    // Items
    private List<BillItemResponse> items;

    // Amounts
    private Integer totalItems;
    private Integer totalQuantity;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal balanceAmount;

    // Payments
    private List<PaymentResponse> payments;

    // Footer
    private String cashierName;
    private String notes;
}

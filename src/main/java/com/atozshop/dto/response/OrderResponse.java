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
public class OrderResponse {

    private Long id;
    private String orderNumber;
    private LocalDateTime orderDate;

    // Customer
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;

    // Delivery
    private AddressResponse deliveryAddress;
    private String deliverySlot;
    private BigDecimal deliveryFee;
    private String customerNotes;

    // Items
    private List<OrderItemResponse> items;
    private Integer totalItems;
    private Integer totalQuantity;

    // Amounts
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;

    // Status
    private String status;
    private String paymentMethod;
    private String paymentStatus;

    // Tracking
    private LocalDateTime acceptedAt;
    private LocalDateTime packedAt;
    private LocalDateTime dispatchedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;
    private String cancelReason;

    // Timeline
    private List<OrderStatusHistory> statusHistory;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderStatusHistory {
        private String status;
        private LocalDateTime timestamp;
        private String performedBy;
    }
}

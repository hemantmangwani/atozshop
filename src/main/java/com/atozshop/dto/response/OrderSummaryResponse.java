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
public class OrderSummaryResponse {

    private Long id;
    private String orderNumber;
    private LocalDateTime orderDate;
    private String status;
    private String paymentMethod;
    private String paymentStatus;
    private Integer totalItems;
    private BigDecimal totalAmount;
    private String customerName;
    private String deliveryCity;
    private LocalDateTime estimatedDelivery;
}

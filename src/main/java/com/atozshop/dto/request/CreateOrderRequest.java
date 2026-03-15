package com.atozshop.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

    @NotNull(message = "Tenant ID is required")
    private Long tenantId;

    @NotNull(message = "Store ID is required")
    private Long storeId;

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Delivery address ID is required")
    private Long deliveryAddressId;

    private String deliverySlot;  // "9 AM - 12 PM"

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<OrderItemRequest> items;

    @NotNull(message = "Payment method is required")
    private String paymentMethod;  // COD, ONLINE, WALLET, UPI

    private String customerNotes;
    private String couponCode;  // Optional discount coupon
}

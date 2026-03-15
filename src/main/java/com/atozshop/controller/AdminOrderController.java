package com.atozshop.controller;

import com.atozshop.dto.response.OrderResponse;
import com.atozshop.dto.response.OrderSummaryResponse;
import com.atozshop.entity.Order;
import com.atozshop.security.CurrentUser;
import com.atozshop.security.UserPrincipal;
import com.atozshop.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
@Tag(name = "Admin Order Management", description = "APIs for admin to manage and fulfill orders")
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    @Operation(summary = "Get all orders", description = "List all orders with optional status filter")
    public ResponseEntity<List<OrderSummaryResponse>> getAllOrders(
            @CurrentUser UserPrincipal user,
            @RequestParam(required = false) String status
    ) {
        Order.OrderStatus orderStatus = status != null ? Order.OrderStatus.valueOf(status) : null;
        List<OrderSummaryResponse> orders = orderService.getStoreOrders(
            user.getTenantId(), user.getStoreIdOrDefault(), orderStatus);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order details", description = "Get detailed information about a specific order")
    public ResponseEntity<OrderResponse> getOrderById(
            @CurrentUser UserPrincipal user,
            @PathVariable Long id
    ) {
        OrderResponse order = orderService.getOrderById(id, user.getTenantId());
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{id}/accept")
    @Operation(
            summary = "Accept order",
            description = "Admin accepts order - this RESERVES stock to prevent overselling"
    )
    public ResponseEntity<OrderResponse> acceptOrder(
            @CurrentUser UserPrincipal user,
            @PathVariable Long id
    ) {
        OrderResponse order = orderService.acceptOrder(id, user.getTenantId(), user.getId());
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{id}/pack")
    @Operation(summary = "Mark as packed", description = "Mark order as packed and ready for delivery")
    public ResponseEntity<OrderResponse> markAsPacked(
            @CurrentUser UserPrincipal user,
            @PathVariable Long id
    ) {
        OrderResponse order = orderService.markAsPacked(id, user.getTenantId(), user.getId());
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{id}/dispatch")
    @Operation(summary = "Mark as dispatched", description = "Mark order as out for delivery")
    public ResponseEntity<OrderResponse> markAsDispatched(
            @CurrentUser UserPrincipal user,
            @PathVariable Long id
    ) {
        OrderResponse order = orderService.markAsDispatched(id, user.getTenantId(), user.getId());
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{id}/deliver")
    @Operation(
            summary = "Mark as delivered",
            description = "Mark order as delivered - this DEDUCTS stock from inventory and fulfills reservation"
    )
    public ResponseEntity<OrderResponse> markAsDelivered(
            @CurrentUser UserPrincipal user,
            @PathVariable Long id
    ) {
        OrderResponse order = orderService.markAsDelivered(id, user.getTenantId(), user.getId());
        return ResponseEntity.ok(order);
    }
}

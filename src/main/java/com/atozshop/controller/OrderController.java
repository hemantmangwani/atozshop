package com.atozshop.controller;

import com.atozshop.dto.request.CancelOrderRequest;
import com.atozshop.dto.request.CreateOrderRequest;
import com.atozshop.dto.response.OrderResponse;
import com.atozshop.dto.response.OrderSummaryResponse;
import com.atozshop.security.CurrentUser;
import com.atozshop.security.UserPrincipal;
import com.atozshop.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Customer Orders", description = "APIs for customer order placement and tracking")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Place a new order", description = "Customer places a new order with selected items")
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.placeOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get my orders", description = "Get all orders for the logged-in customer")
    public ResponseEntity<List<OrderSummaryResponse>> getMyOrders(@CurrentUser UserPrincipal user) {
        if (user.getCustomerId() == null) {
            // Return empty list if user doesn't have a customer profile
            return ResponseEntity.ok(List.of());
        }
        List<OrderSummaryResponse> orders = orderService.getCustomerOrders(user.getCustomerId(), user.getTenantId());
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get customer orders", description = "Get all orders for a specific customer")
    public ResponseEntity<List<OrderSummaryResponse>> getCustomerOrders(
            @CurrentUser UserPrincipal user,
            @PathVariable Long customerId
    ) {
        List<OrderSummaryResponse> orders = orderService.getCustomerOrders(customerId, user.getTenantId());
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

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel order", description = "Customer cancels an order (before it's packed)")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long id,
            @Valid @RequestBody CancelOrderRequest request
    ) {
        OrderResponse order = orderService.cancelOrder(id, request);
        return ResponseEntity.ok(order);
    }
}

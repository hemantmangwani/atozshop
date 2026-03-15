package com.atozshop.service;

import com.atozshop.dto.request.CancelOrderRequest;
import com.atozshop.dto.request.CreateOrderRequest;
import com.atozshop.dto.response.*;
import com.atozshop.entity.*;
import com.atozshop.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CustomerRepository customerRepository;
    private final CustomerAddressRepository addressRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductRepository productRepository;
    private final VariantPriceRepository priceRepository;
    private final StockReservationService reservationService;
    private final StockService stockService;

    /**
     * Place a new order
     */
    @Transactional
    public OrderResponse placeOrder(CreateOrderRequest request) {
        log.info("Creating order for customer {}, tenant {}", request.getCustomerId(), request.getTenantId());

        // 1. Validate customer exists
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        if (!customer.getTenantId().equals(request.getTenantId())) {
            throw new RuntimeException("Customer does not belong to this tenant");
        }

        // 2. Validate delivery address
        CustomerAddress address = addressRepository.findByIdAndCustomerId(
                request.getDeliveryAddressId(), request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Delivery address not found"));

        // 3. Validate stock availability for all items
        for (var itemReq : request.getItems()) {
            if (!reservationService.hasAvailableStock(
                    itemReq.getVariantId(), request.getStoreId(), request.getTenantId(), itemReq.getQuantity())) {

                Integer available = reservationService.getAvailableStock(
                        itemReq.getVariantId(), request.getStoreId(), request.getTenantId());
                throw new RuntimeException("Insufficient stock for variant " + itemReq.getVariantId() +
                        ". Available: " + available + ", Requested: " + itemReq.getQuantity());
            }
        }

        // 4. Create order
        Order order = new Order();
        order.setTenantId(request.getTenantId());
        order.setStoreId(request.getStoreId());
        order.setCustomerId(request.getCustomerId());
        order.setOrderNumber(generateOrderNumber(request.getTenantId()));
        order.setOrderDate(LocalDateTime.now());
        order.setDeliveryAddressId(request.getDeliveryAddressId());
        order.setDeliverySlot(request.getDeliverySlot());
        order.setCustomerNotes(request.getCustomerNotes());
        order.setStatus(Order.OrderStatus.NEW);
        order.setPaymentMethod(Order.PaymentMethod.valueOf(request.getPaymentMethod()));
        order.setPaymentStatus(Order.PaymentStatus.PENDING);

        // 5. Create order items and calculate totals
        BigDecimal subtotal = BigDecimal.ZERO;
        int totalItems = 0;
        int totalQuantity = 0;

        List<OrderItem> orderItems = new ArrayList<>();

        for (var itemReq : request.getItems()) {
            // Get variant and price
            ProductVariant variant = variantRepository.findById(itemReq.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Variant not found: " + itemReq.getVariantId()));

            Product product = productRepository.findById(variant.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + variant.getProductId()));

            VariantPrice price = priceRepository.findCurrentPrice(
                    request.getTenantId(),
                    itemReq.getVariantId(),
                    request.getStoreId(),
                    java.time.LocalDate.now()).orElseThrow(() ->
                    new RuntimeException("Price not found for variant: " + itemReq.getVariantId()));

            // Create order item
            OrderItem item = new OrderItem();
            item.setVariantId(variant.getId());
            item.setSku(variant.getSku());
            item.setProductName(product.getName());
            item.setVariantName(variant.getVariantName());
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(price.getSellingPrice());
            item.setDiscountAmount(BigDecimal.ZERO);
            item.setTaxAmount(BigDecimal.ZERO);

            // Calculate item total
            BigDecimal itemTotal = price.getSellingPrice().multiply(new BigDecimal(itemReq.getQuantity()));
            item.setTotalAmount(itemTotal);
            item.setFulfilledQuantity(0);

            orderItems.add(item);

            subtotal = subtotal.add(itemTotal);
            totalItems++;
            totalQuantity += itemReq.getQuantity();
        }

        // 6. Calculate delivery fee (simplified - can be enhanced)
        BigDecimal deliveryFee = calculateDeliveryFee(address, subtotal);
        order.setDeliveryFee(deliveryFee);

        // 7. Set order totals
        order.setSubtotal(subtotal);
        order.setDiscountAmount(BigDecimal.ZERO);  // TODO: Apply discount if coupon provided
        order.setTaxAmount(BigDecimal.ZERO);       // TODO: Calculate tax
        order.setTotalAmount(subtotal.add(deliveryFee));

        // 8. Save order
        order = orderRepository.save(order);

        // 9. Save order items
        Long orderId = order.getId();
        for (OrderItem item : orderItems) {
            item.setOrderId(orderId);
            orderItemRepository.save(item);
        }

        log.info("Order created: {}, Total: ₹{}", order.getOrderNumber(), order.getTotalAmount());

        // 10. Return order response
        return buildOrderResponse(order, orderItems, customer, address);
    }

    /**
     * Accept order (admin action) - reserves stock
     */
    @Transactional
    public OrderResponse acceptOrder(Long orderId, Long tenantId, Long acceptedBy) {
        log.info("Accepting order {}, tenant {}", orderId, tenantId);

        Order order = orderRepository.findByIdAndTenantId(orderId, tenantId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != Order.OrderStatus.NEW) {
            throw new RuntimeException("Can only accept orders in NEW status. Current status: " + order.getStatus());
        }

        // Get order items
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);

        // Reserve stock for all items
        List<StockReservationService.ReservationItem> reservationItems = items.stream()
                .map(item -> StockReservationService.ReservationItem.builder()
                        .variantId(item.getVariantId())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        reservationService.reserveStockBatch(tenantId, order.getStoreId(), orderId, reservationItems, acceptedBy);

        // Update order status
        order.setStatus(Order.OrderStatus.ACCEPTED);
        order.setAcceptedAt(LocalDateTime.now());
        order.setAcceptedBy(acceptedBy);
        order = orderRepository.save(order);

        log.info("Order accepted: {}", order.getOrderNumber());

        // Get customer and address for response
        Customer customer = customerRepository.findById(order.getCustomerId()).orElse(null);
        CustomerAddress address = addressRepository.findById(order.getDeliveryAddressId()).orElse(null);

        return buildOrderResponse(order, items, customer, address);
    }

    /**
     * Mark order as packed
     */
    @Transactional
    public OrderResponse markAsPacked(Long orderId, Long tenantId, Long packedBy) {
        log.info("Marking order {} as packed", orderId);

        Order order = orderRepository.findByIdAndTenantId(orderId, tenantId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != Order.OrderStatus.ACCEPTED) {
            throw new RuntimeException("Can only pack orders in ACCEPTED status. Current status: " + order.getStatus());
        }

        order.setStatus(Order.OrderStatus.PACKED);
        order.setPackedAt(LocalDateTime.now());
        order.setPackedBy(packedBy);
        order = orderRepository.save(order);

        log.info("Order packed: {}", order.getOrderNumber());

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        Customer customer = customerRepository.findById(order.getCustomerId()).orElse(null);
        CustomerAddress address = addressRepository.findById(order.getDeliveryAddressId()).orElse(null);

        return buildOrderResponse(order, items, customer, address);
    }

    /**
     * Mark order as out for delivery
     */
    @Transactional
    public OrderResponse markAsDispatched(Long orderId, Long tenantId, Long dispatchedBy) {
        log.info("Marking order {} as dispatched", orderId);

        Order order = orderRepository.findByIdAndTenantId(orderId, tenantId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != Order.OrderStatus.PACKED) {
            throw new RuntimeException("Can only dispatch orders in PACKED status. Current status: " + order.getStatus());
        }

        order.setStatus(Order.OrderStatus.OUT_FOR_DELIVERY);
        order.setDispatchedAt(LocalDateTime.now());
        order.setDispatchedBy(dispatchedBy);
        order = orderRepository.save(order);

        log.info("Order dispatched: {}", order.getOrderNumber());

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        Customer customer = customerRepository.findById(order.getCustomerId()).orElse(null);
        CustomerAddress address = addressRepository.findById(order.getDeliveryAddressId()).orElse(null);

        return buildOrderResponse(order, items, customer, address);
    }

    /**
     * Mark order as delivered - deducts stock
     */
    @Transactional
    public OrderResponse markAsDelivered(Long orderId, Long tenantId, Long deliveredBy) {
        log.info("Marking order {} as delivered", orderId);

        Order order = orderRepository.findByIdAndTenantId(orderId, tenantId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != Order.OrderStatus.OUT_FOR_DELIVERY) {
            throw new RuntimeException("Can only mark orders as delivered when OUT_FOR_DELIVERY. Current status: " + order.getStatus());
        }

        // Get order items
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);

        // Deduct stock via stock ledger (SALE entries)
        for (OrderItem item : items) {
            stockService.recordStockMovement(
                    tenantId,
                    order.getStoreId(),
                    item.getVariantId(),
                    StockLedger.TransactionType.SALE,
                    orderId,
                    -item.getQuantity(),  // Negative for sale
                    item.getUnitPrice(),
                    item.getUnitPrice(),
                    "Online order: " + order.getOrderNumber(),
                    LocalDateTime.now(),
                    deliveredBy
            );
        }

        // Mark reservations as fulfilled
        reservationService.fulfillReservation(orderId);

        // Update order status
        order.setStatus(Order.OrderStatus.DELIVERED);
        order.setDeliveredAt(LocalDateTime.now());
        order.setDeliveredBy(deliveredBy);
        order.setPaymentStatus(Order.PaymentStatus.PAID);  // Mark as paid on delivery
        order = orderRepository.save(order);

        log.info("Order delivered: {}, stock deducted", order.getOrderNumber());

        Customer customer = customerRepository.findById(order.getCustomerId()).orElse(null);
        CustomerAddress address = addressRepository.findById(order.getDeliveryAddressId()).orElse(null);

        return buildOrderResponse(order, items, customer, address);
    }

    /**
     * Cancel order - releases reserved stock
     */
    @Transactional
    public OrderResponse cancelOrder(Long orderId, CancelOrderRequest request) {
        log.info("Cancelling order {}: {}", orderId, request.getCancelReason());

        Order order = orderRepository.findByIdAndTenantId(orderId, request.getTenantId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new RuntimeException("Cannot cancel delivered orders. Use return process instead.");
        }

        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new RuntimeException("Order is already cancelled");
        }

        // Release reserved stock (if order was accepted)
        if (order.getStatus() == Order.OrderStatus.ACCEPTED ||
            order.getStatus() == Order.OrderStatus.PACKED ||
            order.getStatus() == Order.OrderStatus.OUT_FOR_DELIVERY) {

            reservationService.releaseReservation(orderId, request.getCancelReason());
        }

        // Update order
        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancelReason(request.getCancelReason());
        order.setCancelledBy(request.getCancelledBy());
        order = orderRepository.save(order);

        log.info("Order cancelled: {}", order.getOrderNumber());

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        Customer customer = customerRepository.findById(order.getCustomerId()).orElse(null);
        CustomerAddress address = addressRepository.findById(order.getDeliveryAddressId()).orElse(null);

        return buildOrderResponse(order, items, customer, address);
    }

    /**
     * Get order by ID
     */
    public OrderResponse getOrderById(Long orderId, Long tenantId) {
        Order order = orderRepository.findByIdAndTenantId(orderId, tenantId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        Customer customer = customerRepository.findById(order.getCustomerId()).orElse(null);
        CustomerAddress address = addressRepository.findById(order.getDeliveryAddressId()).orElse(null);

        return buildOrderResponse(order, items, customer, address);
    }

    /**
     * Get customer's orders
     */
    public List<OrderSummaryResponse> getCustomerOrders(Long customerId, Long tenantId) {
        List<Order> orders = orderRepository.findByCustomerIdAndTenantIdOrderByOrderDateDesc(customerId, tenantId);

        return orders.stream()
                .map(this::buildOrderSummary)
                .collect(Collectors.toList());
    }

    /**
     * Get all orders for a store
     */
    public List<OrderSummaryResponse> getStoreOrders(Long tenantId, Long storeId, Order.OrderStatus status) {
        List<Order> orders;
        if (status != null) {
            orders = orderRepository.findByTenantIdAndStoreIdAndStatus(tenantId, storeId, status);
        } else {
            orders = orderRepository.findByTenantIdAndStoreId(tenantId, storeId);
        }

        return orders.stream()
                .map(this::buildOrderSummary)
                .collect(Collectors.toList());
    }

    // Helper methods

    private String generateOrderNumber(Long tenantId) {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "ORD-" + dateStr + "-";

        String lastNumber = orderRepository.findLastOrderNumberLike(tenantId, prefix + "%");

        int sequence = 1;
        if (lastNumber != null && lastNumber.length() > 13) {
            try {
                sequence = Integer.parseInt(lastNumber.substring(13)) + 1;
            } catch (NumberFormatException e) {
                log.warn("Could not parse sequence from last order number: {}", lastNumber);
            }
        }

        return prefix + String.format("%03d", sequence);
    }

    private BigDecimal calculateDeliveryFee(CustomerAddress address, BigDecimal subtotal) {
        // Simplified delivery fee calculation
        // TODO: Enhance with zone-based pricing, distance calculation, etc.

        if (subtotal.compareTo(new BigDecimal("500")) >= 0) {
            return BigDecimal.ZERO;  // Free delivery above ₹500
        }
        return new BigDecimal("50");  // ₹50 delivery fee
    }

    private OrderResponse buildOrderResponse(Order order, List<OrderItem> items,
                                            Customer customer, CustomerAddress address) {

        List<OrderItemResponse> itemResponses = items.stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .variantId(item.getVariantId())
                        .sku(item.getSku())
                        .productName(item.getProductName())
                        .variantName(item.getVariantName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .discountAmount(item.getDiscountAmount())
                        .taxAmount(item.getTaxAmount())
                        .totalAmount(item.getTotalAmount())
                        .fulfilledQuantity(item.getFulfilledQuantity())
                        .build())
                .collect(Collectors.toList());

        AddressResponse addressResponse = address != null ? AddressResponse.builder()
                .id(address.getId())
                .customerId(address.getCustomerId())
                .addressType(address.getAddressType() != null ? address.getAddressType().name() : null)
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .landmark(address.getLandmark())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .phone(address.getPhone())
                .isDefault(address.getIsDefault())
                .build() : null;

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .orderDate(order.getOrderDate())
                .customerId(order.getCustomerId())
                .customerName(customer != null ? customer.getName() : null)
                .customerPhone(customer != null ? customer.getPhone() : null)
                .customerEmail(customer != null ? customer.getEmail() : null)
                .deliveryAddress(addressResponse)
                .deliverySlot(order.getDeliverySlot())
                .deliveryFee(order.getDeliveryFee())
                .customerNotes(order.getCustomerNotes())
                .items(itemResponses)
                .totalItems(items.size())
                .totalQuantity(items.stream().mapToInt(OrderItem::getQuantity).sum())
                .subtotal(order.getSubtotal())
                .discountAmount(order.getDiscountAmount())
                .taxAmount(order.getTaxAmount())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null)
                .paymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus().name() : null)
                .acceptedAt(order.getAcceptedAt())
                .packedAt(order.getPackedAt())
                .dispatchedAt(order.getDispatchedAt())
                .deliveredAt(order.getDeliveredAt())
                .cancelledAt(order.getCancelledAt())
                .cancelReason(order.getCancelReason())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private OrderSummaryResponse buildOrderSummary(Order order) {
        Customer customer = customerRepository.findById(order.getCustomerId()).orElse(null);
        CustomerAddress address = addressRepository.findById(order.getDeliveryAddressId()).orElse(null);

        return OrderSummaryResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .orderDate(order.getOrderDate())
                .status(order.getStatus().name())
                .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null)
                .paymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus().name() : null)
                .totalItems(orderItemRepository.findByOrderId(order.getId()).size())
                .totalAmount(order.getTotalAmount())
                .customerName(customer != null ? customer.getName() : null)
                .deliveryCity(address != null ? address.getCity() : null)
                .build();
    }
}

package com.atozshop.service;

import com.atozshop.dto.request.AddBillItemRequest;
import com.atozshop.dto.request.ApplyDiscountRequest;
import com.atozshop.dto.request.CreateBillRequest;
import com.atozshop.dto.request.UpdateBillItemRequest;
import com.atozshop.dto.response.*;
import com.atozshop.entity.*;
import com.atozshop.exception.InsufficientStockException;
import com.atozshop.exception.ResourceNotFoundException;
import com.atozshop.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BillService {

    private final BillRepository billRepository;
    private final BillItemRepository billItemRepository;
    private final BillDiscountRepository billDiscountRepository;
    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductRepository productRepository;
    private final VariantPriceRepository variantPriceRepository;
    private final StockLedgerRepository stockLedgerRepository;
    private final StockService stockService;
    private final DiscountRepository discountRepository;

    @Transactional
    public BillResponse createBill(CreateBillRequest request) {
        // Validate customer if provided
        if (request.getCustomerId() != null) {
            customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        }

        // Generate bill number
        String billNumber = generateBillNumber(request.getTenantId());

        // Create bill entity
        Bill bill = Bill.builder()
            .tenantId(request.getTenantId())
            .storeId(request.getStoreId())
            .customerId(request.getCustomerId())
            .cashierId(request.getCashierId())
            .billNumber(billNumber)
            .billDate(LocalDateTime.now())
            .billType(Bill.BillType.valueOf(request.getBillType()))
            .status(Bill.BillStatus.DRAFT)
            .paymentStatus(Bill.PaymentStatus.UNPAID)
            .notes(request.getNotes())
            .createdBy(request.getCashierId())
            .build();

        bill = billRepository.save(bill);

        // Add items
        List<BillItem> billItems = new ArrayList<>();
        for (AddBillItemRequest itemRequest : request.getItems()) {
            BillItem item = createBillItem(bill.getId(), itemRequest, request.getTenantId(), request.getStoreId());
            billItems.add(billItemRepository.save(item));
        }

        // Apply discounts if any
        List<BillDiscount> billDiscounts = new ArrayList<>();
        if (request.getDiscounts() != null && !request.getDiscounts().isEmpty()) {
            for (ApplyDiscountRequest discountRequest : request.getDiscounts()) {
                BillDiscount discount = applyDiscount(bill.getId(), discountRequest, request.getTenantId());
                billDiscounts.add(billDiscountRepository.save(discount));
            }
        }

        // Calculate totals
        recalculateBillTotals(bill.getId());

        return getBillResponse(bill.getId(), request.getTenantId());
    }

    @Transactional(readOnly = true)
    public BillResponse getBillById(Long billId, Long tenantId) {
        Bill bill = billRepository.findByIdAndTenantId(billId, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + billId));

        return getBillResponse(billId, tenantId);
    }

    @Transactional(readOnly = true)
    public BillResponse getBillByNumber(String billNumber, Long tenantId) {
        Bill bill = billRepository.findByBillNumberAndTenantId(billNumber, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Bill not found with number: " + billNumber));

        return getBillResponse(bill.getId(), tenantId);
    }

    @Transactional(readOnly = true)
    public List<BillSummaryResponse> getAllBills(Long tenantId, Long storeId) {
        List<Bill> bills = billRepository.findByTenantIdAndStoreId(tenantId, storeId);
        return bills.stream().map(this::mapToBillSummary).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BillSummaryResponse getBillsSummary(Long tenantId, Long storeId) {
        List<Bill> bills = billRepository.findByTenantIdAndStoreId(tenantId, storeId);

        int totalBills = bills.size();
        int totalItems = bills.stream().mapToInt(Bill::getTotalItems).sum();
        int totalQuantity = bills.stream().mapToInt(Bill::getTotalQuantity).sum();
        BigDecimal totalAmount = bills.stream()
                .map(Bill::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal paidAmount = bills.stream()
                .map(Bill::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal balanceAmount = bills.stream()
                .map(Bill::getBalanceAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return BillSummaryResponse.builder()
                .id(null)
                .billNumber("SUMMARY")
                .billDate(LocalDateTime.now())
                .billType("ALL")
                .status("SUMMARY")
                .paymentStatus("SUMMARY")
                .customerName(totalBills + " bills")
                .customerPhone(null)
                .totalItems(totalItems)
                .totalQuantity(totalQuantity)
                .totalAmount(totalAmount)
                .paidAmount(paidAmount)
                .balanceAmount(balanceAmount)
                .build();
    }

    @Transactional
    public BillResponse addItemToBill(Long billId, AddBillItemRequest request, Long tenantId, Long storeId) {
        Bill bill = billRepository.findByIdAndTenantId(billId, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));

        if (bill.getStatus() != Bill.BillStatus.DRAFT) {
            throw new IllegalStateException("Cannot add items to a " + bill.getStatus() + " bill");
        }

        BillItem item = createBillItem(billId, request, tenantId, storeId);
        billItemRepository.save(item);

        recalculateBillTotals(billId);
        return getBillResponse(billId, tenantId);
    }

    @Transactional
    public BillResponse updateBillItem(Long billId, Long itemId, UpdateBillItemRequest request, Long tenantId) {
        Bill bill = billRepository.findByIdAndTenantId(billId, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));

        if (bill.getStatus() != Bill.BillStatus.DRAFT) {
            throw new IllegalStateException("Cannot update items in a " + bill.getStatus() + " bill");
        }

        BillItem item = billItemRepository.findByIdAndBillId(itemId, billId)
            .orElseThrow(() -> new ResourceNotFoundException("Bill item not found"));

        if (request.getQuantity() != null) {
            item.setQuantity(request.getQuantity());
        }
        if (request.getDiscountPercent() != null) {
            item.setDiscountPercent(request.getDiscountPercent());
        }
        if (request.getDiscountAmount() != null) {
            item.setDiscountAmount(request.getDiscountAmount());
        }

        // Recalculate item amounts
        calculateItemAmounts(item);
        billItemRepository.save(item);

        recalculateBillTotals(billId);
        return getBillResponse(billId, tenantId);
    }

    @Transactional
    public BillResponse removeItemFromBill(Long billId, Long itemId, Long tenantId) {
        Bill bill = billRepository.findByIdAndTenantId(billId, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));

        if (bill.getStatus() != Bill.BillStatus.DRAFT) {
            throw new IllegalStateException("Cannot remove items from a " + bill.getStatus() + " bill");
        }

        billItemRepository.deleteById(itemId);

        recalculateBillTotals(billId);
        return getBillResponse(billId, tenantId);
    }

    @Transactional
    public BillResponse confirmBill(Long billId, Long tenantId) {
        Bill bill = billRepository.findByIdAndTenantId(billId, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));

        if (bill.getStatus() != Bill.BillStatus.DRAFT) {
            throw new IllegalStateException("Bill is already " + bill.getStatus());
        }

        List<BillItem> items = billItemRepository.findByBillId(billId);

        // Validate stock availability
        validateStockAvailability(items, bill.getStoreId(), tenantId);

        // Deduct stock using Phase 1 StockService
        for (BillItem item : items) {
            stockService.recordStockMovement(
                tenantId,
                bill.getStoreId(),
                item.getVariantId(),
                StockLedger.TransactionType.SALE,
                billId,
                -item.getQuantity(),  // Negative for sale
                item.getUnitPrice(),  // Cost snapshot
                item.getUnitPrice(),  // Selling price snapshot
                "Sale via Bill: " + bill.getBillNumber(),
                bill.getBillDate(),
                bill.getCashierId()
            );
        }

        // Update customer total purchases if customer exists
        if (bill.getCustomerId() != null) {
            Customer customer = customerRepository.findById(bill.getCustomerId()).orElse(null);
            if (customer != null) {
                customer.setTotalPurchases(customer.getTotalPurchases().add(bill.getTotalAmount()));
                customerRepository.save(customer);
            }
        }

        bill.setStatus(Bill.BillStatus.CONFIRMED);
        billRepository.save(bill);

        return getBillResponse(billId, tenantId);
    }

    @Transactional
    public BillResponse cancelBill(Long billId, Long tenantId) {
        Bill bill = billRepository.findByIdAndTenantId(billId, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));

        if (bill.getStatus() != Bill.BillStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT bills can be cancelled");
        }

        bill.setStatus(Bill.BillStatus.CANCELLED);
        billRepository.save(bill);

        return getBillResponse(billId, tenantId);
    }

    private void validateStockAvailability(List<BillItem> items, Long storeId, Long tenantId) {
        for (BillItem item : items) {
            Integer availableStock = stockLedgerRepository.getCurrentStock(item.getVariantId(), storeId, tenantId);

            if (availableStock == null || availableStock < item.getQuantity()) {
                throw new InsufficientStockException(
                    item.getSku(),
                    item.getQuantity(),
                    availableStock != null ? availableStock : 0
                );
            }
        }
    }

    private BillItem createBillItem(Long billId, AddBillItemRequest request, Long tenantId, Long storeId) {
        // Get variant
        ProductVariant variant = variantRepository.findById(request.getVariantId())
            .orElseThrow(() -> new ResourceNotFoundException("Variant not found"));

        // Get product
        Product product = productRepository.findById(variant.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Get current price
        VariantPrice price = variantPriceRepository.findCurrentPrice(tenantId, request.getVariantId(), storeId, LocalDate.now())
            .orElseThrow(() -> new ResourceNotFoundException("Price not found for variant"));

        BillItem item = BillItem.builder()
            .billId(billId)
            .variantId(variant.getId())
            .sku(variant.getSku())
            .productName(product.getName())
            .variantName(variant.getVariantName())
            .quantity(request.getQuantity())
            .unitPrice(price.getSellingPrice())
            .mrp(price.getMrp())
            .discountPercent(request.getDiscountPercent() != null ? request.getDiscountPercent() : BigDecimal.ZERO)
            .discountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO)
            .taxPercent(BigDecimal.ZERO)  // Can be configured later
            .build();

        calculateItemAmounts(item);
        return item;
    }

    private void calculateItemAmounts(BillItem item) {
        // Subtotal = quantity × unit_price
        BigDecimal subtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        item.setSubtotal(subtotal);

        // Calculate discount
        BigDecimal discount = BigDecimal.ZERO;
        if (item.getDiscountPercent().compareTo(BigDecimal.ZERO) > 0) {
            discount = subtotal.multiply(item.getDiscountPercent()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else if (item.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            discount = item.getDiscountAmount();
        }
        item.setDiscountAmount(discount);

        // Taxable amount = subtotal - discount
        BigDecimal taxableAmount = subtotal.subtract(discount);

        // Calculate tax
        BigDecimal tax = taxableAmount.multiply(item.getTaxPercent()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        item.setTaxAmount(tax);

        // Total = taxable amount + tax
        BigDecimal total = taxableAmount.add(tax);
        item.setTotalAmount(total);
    }

    private BillDiscount applyDiscount(Long billId, ApplyDiscountRequest request, Long tenantId) {
        Discount discount = null;
        if (request.getDiscountCode() != null) {
            discount = discountRepository.findActiveDiscountByCode(
                tenantId, request.getDiscountCode(), LocalDate.now()
            ).orElseThrow(() -> new ResourceNotFoundException("Discount not found or expired"));
        }

        BillDiscount billDiscount = BillDiscount.builder()
            .billId(billId)
            .discountId(discount != null ? discount.getId() : null)
            .discountName(discount != null ? discount.getName() : request.getDiscountName())
            .discountCode(request.getDiscountCode())
            .discountType(request.getDiscountType())
            .discountValue(request.getDiscountValue())
            .discountAmount(BigDecimal.ZERO)  // Calculated later
            .build();

        return billDiscount;
    }

    private void recalculateBillTotals(Long billId) {
        Bill bill = billRepository.findById(billId)
            .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));

        List<BillItem> items = billItemRepository.findByBillId(billId);

        // Calculate totals
        int totalItems = items.size();
        int totalQuantity = items.stream().mapToInt(BillItem::getQuantity).sum();
        BigDecimal subtotal = items.stream()
            .map(BillItem::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discountAmount = billDiscountRepository.getTotalDiscountForBill(billId);
        BigDecimal taxAmount = items.stream()
            .map(BillItem::getTaxAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAmount = subtotal.subtract(discountAmount);
        BigDecimal balanceAmount = totalAmount.subtract(bill.getPaidAmount());

        bill.setTotalItems(totalItems);
        bill.setTotalQuantity(totalQuantity);
        bill.setSubtotal(subtotal);
        bill.setDiscountAmount(discountAmount);
        bill.setTaxAmount(taxAmount);
        bill.setTotalAmount(totalAmount);
        bill.setBalanceAmount(balanceAmount);

        billRepository.save(bill);
    }

    private String generateBillNumber(Long tenantId) {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "BIL-" + dateStr + "-";

        String lastNumber = billRepository.findLastBillNumberForDate(tenantId, prefix)
            .orElse(null);

        int sequence = 1;
        if (lastNumber != null) {
            sequence = Integer.parseInt(lastNumber.substring(14)) + 1;
        }

        return prefix + String.format("%03d", sequence);
    }

    private BillResponse getBillResponse(Long billId, Long tenantId) {
        Bill bill = billRepository.findByIdAndTenantId(billId, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));

        List<BillItem> items = billItemRepository.findByBillId(billId);
        List<BillDiscount> discounts = billDiscountRepository.findByBillId(billId);
        List<Payment> payments = paymentRepository.findByBillId(billId);

        Customer customer = null;
        if (bill.getCustomerId() != null) {
            customer = customerRepository.findById(bill.getCustomerId()).orElse(null);
        }

        return BillResponse.builder()
            .id(bill.getId())
            .tenantId(bill.getTenantId())
            .storeId(bill.getStoreId())
            .billNumber(bill.getBillNumber())
            .billDate(bill.getBillDate())
            .billType(bill.getBillType().name())
            .status(bill.getStatus().name())
            .paymentStatus(bill.getPaymentStatus().name())
            .customerId(bill.getCustomerId())
            .customerName(customer != null ? customer.getName() : null)
            .customerPhone(customer != null ? customer.getPhone() : null)
            .customerCode(customer != null ? customer.getCustomerCode() : null)
            .cashierId(bill.getCashierId())
            .createdBy(bill.getCreatedBy())
            .items(items.stream().map(this::mapToBillItemResponse).collect(Collectors.toList()))
            .totalItems(bill.getTotalItems())
            .totalQuantity(bill.getTotalQuantity())
            .subtotal(bill.getSubtotal())
            .discountAmount(bill.getDiscountAmount())
            .taxAmount(bill.getTaxAmount())
            .totalAmount(bill.getTotalAmount())
            .paidAmount(bill.getPaidAmount())
            .balanceAmount(bill.getBalanceAmount())
            .payments(payments.stream().map(this::mapToPaymentResponse).collect(Collectors.toList()))
            .discounts(discounts.stream().map(this::mapToBillDiscountResponse).collect(Collectors.toList()))
            .notes(bill.getNotes())
            .createdAt(bill.getCreatedAt())
            .updatedAt(bill.getUpdatedAt())
            .build();
    }

    private BillSummaryResponse mapToBillSummary(Bill bill) {
        Customer customer = null;
        if (bill.getCustomerId() != null) {
            customer = customerRepository.findById(bill.getCustomerId()).orElse(null);
        }

        return BillSummaryResponse.builder()
            .id(bill.getId())
            .billNumber(bill.getBillNumber())
            .billDate(bill.getBillDate())
            .billType(bill.getBillType().name())
            .status(bill.getStatus().name())
            .paymentStatus(bill.getPaymentStatus().name())
            .customerName(customer != null ? customer.getName() : null)
            .customerPhone(customer != null ? customer.getPhone() : null)
            .totalItems(bill.getTotalItems())
            .totalQuantity(bill.getTotalQuantity())
            .totalAmount(bill.getTotalAmount())
            .paidAmount(bill.getPaidAmount())
            .balanceAmount(bill.getBalanceAmount())
            .build();
    }

    private BillItemResponse mapToBillItemResponse(BillItem item) {
        return BillItemResponse.builder()
            .id(item.getId())
            .billId(item.getBillId())
            .variantId(item.getVariantId())
            .sku(item.getSku())
            .productName(item.getProductName())
            .variantName(item.getVariantName())
            .quantity(item.getQuantity())
            .unitPrice(item.getUnitPrice())
            .mrp(item.getMrp())
            .discountPercent(item.getDiscountPercent())
            .discountAmount(item.getDiscountAmount())
            .subtotal(item.getSubtotal())
            .taxPercent(item.getTaxPercent())
            .taxAmount(item.getTaxAmount())
            .totalAmount(item.getTotalAmount())
            .build();
    }

    private BillDiscountResponse mapToBillDiscountResponse(BillDiscount discount) {
        return BillDiscountResponse.builder()
            .id(discount.getId())
            .billId(discount.getBillId())
            .discountId(discount.getDiscountId())
            .discountName(discount.getDiscountName())
            .discountCode(discount.getDiscountCode())
            .discountType(discount.getDiscountType())
            .discountValue(discount.getDiscountValue())
            .discountAmount(discount.getDiscountAmount())
            .build();
    }

    private PaymentResponse mapToPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
            .id(payment.getId())
            .billId(payment.getBillId())
            .tenantId(payment.getTenantId())
            .paymentMethod(payment.getPaymentMethod().name())
            .paymentDate(payment.getPaymentDate())
            .amount(payment.getAmount())
            .referenceNumber(payment.getReferenceNumber())
            .cardLast4(payment.getCardLast4())
            .upiId(payment.getUpiId())
            .bankName(payment.getBankName())
            .notes(payment.getNotes())
            .createdBy(payment.getCreatedBy())
            .createdAt(payment.getCreatedAt())
            .build();
    }
}

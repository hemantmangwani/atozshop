package com.atozshop.service;

import com.atozshop.dto.request.CreateReturnRequest;
import com.atozshop.dto.request.ProcessRefundRequest;
import com.atozshop.dto.response.BillResponse;
import com.atozshop.entity.*;
import com.atozshop.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReturnService {

    private final BillRepository billRepository;
    private final BillItemRepository billItemRepository;
    private final PaymentRepository paymentRepository;
    private final StockService stockService;
    private final BillService billService;

    @Transactional
    public BillResponse createReturnBill(CreateReturnRequest request) {
        log.info("Creating return bill for original bill ID: {}, tenant: {}",
                request.getOriginalBillId(), request.getTenantId());

        // 1. Validate original bill exists and is confirmed
        Bill originalBill = billRepository.findByIdAndTenantId(request.getOriginalBillId(), request.getTenantId())
                .orElseThrow(() -> new RuntimeException("Original bill not found"));

        if (!"CONFIRMED".equals(originalBill.getStatus())) {
            throw new RuntimeException("Can only return items from confirmed bills");
        }

        // 2. Get original bill items
        List<BillItem> originalItems = billItemRepository.findByBillId(request.getOriginalBillId());

        // 3. Validate return quantities
        validateReturnQuantities(request, originalItems);

        // 4. Create return bill
        Bill returnBill = new Bill();
        returnBill.setTenantId(request.getTenantId());
        returnBill.setStoreId(originalBill.getStoreId());
        returnBill.setCustomerId(originalBill.getCustomerId());
        returnBill.setCashierId(originalBill.getCashierId());
        returnBill.setBillNumber(generateReturnBillNumber(request.getTenantId()));
        returnBill.setBillDate(LocalDateTime.now());
        returnBill.setBillType(Bill.BillType.SALES_RETURN);
        returnBill.setStatus(Bill.BillStatus.DRAFT);
        returnBill.setPaymentStatus(Bill.PaymentStatus.UNPAID);
        returnBill.setNotes("Return for Bill: " + originalBill.getBillNumber() +
                (request.getNotes() != null ? " | " + request.getNotes() : ""));

        // 5. Create return bill items and calculate totals
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalItems = 0;
        int totalQuantity = 0;

        List<BillItem> returnItems = new ArrayList<>();

        for (var returnItemReq : request.getItems()) {
            // Find original bill item
            BillItem originalItem = originalItems.stream()
                    .filter(item -> item.getId().equals(returnItemReq.getBillItemId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Bill item not found: " + returnItemReq.getBillItemId()));

            // Create return item (negative quantities for returns)
            BillItem returnItem = new BillItem();
            returnItem.setBillId(returnBill.getId());
            returnItem.setVariantId(originalItem.getVariantId());
            returnItem.setSku(originalItem.getSku());
            returnItem.setProductName(originalItem.getProductName());
            returnItem.setVariantName(originalItem.getVariantName());
            returnItem.setQuantity(-returnItemReq.getQuantity()); // Negative for returns
            returnItem.setUnitPrice(originalItem.getUnitPrice());
            returnItem.setMrp(originalItem.getMrp());
            returnItem.setDiscountPercent(originalItem.getDiscountPercent());

            // Calculate amounts (will be negative)
            BigDecimal itemSubtotal = originalItem.getUnitPrice()
                    .multiply(new BigDecimal(-returnItemReq.getQuantity()));
            BigDecimal itemDiscount = itemSubtotal
                    .multiply(originalItem.getDiscountPercent().divide(new BigDecimal(100)));

            returnItem.setDiscountAmount(itemDiscount);
            returnItem.setSubtotal(itemSubtotal);
            returnItem.setTaxPercent(originalItem.getTaxPercent());

            BigDecimal taxableAmount = itemSubtotal.subtract(itemDiscount);
            BigDecimal itemTax = taxableAmount
                    .multiply(originalItem.getTaxPercent().divide(new BigDecimal(100)));

            returnItem.setTaxAmount(itemTax);
            returnItem.setTotalAmount(taxableAmount.add(itemTax));

            returnItems.add(returnItem);

            totalAmount = totalAmount.add(returnItem.getTotalAmount());
            totalItems++;
            totalQuantity += returnItemReq.getQuantity();
        }

        // 6. Set bill totals
        returnBill.setTotalItems(totalItems);
        returnBill.setTotalQuantity(totalQuantity);
        returnBill.setSubtotal(totalAmount);
        returnBill.setTotalAmount(totalAmount);
        returnBill.setBalanceAmount(totalAmount);

        // 7. Save return bill
        returnBill = billRepository.save(returnBill);

        // 8. Save return items
        for (BillItem item : returnItems) {
            item.setBillId(returnBill.getId());
            billItemRepository.save(item);
        }

        log.info("Return bill created: {}, amount: {}", returnBill.getBillNumber(), totalAmount);

        // 9. Auto-confirm return and adjust stock
        return confirmReturnBill(returnBill.getId(), request.getTenantId());
    }

    @Transactional
    public BillResponse confirmReturnBill(Long returnBillId, Long tenantId) {
        log.info("Confirming return bill ID: {}, tenant: {}", returnBillId, tenantId);

        // 1. Get return bill
        Bill returnBill = billRepository.findByIdAndTenantId(returnBillId, tenantId)
                .orElseThrow(() -> new RuntimeException("Return bill not found"));

        if (returnBill.getStatus() != Bill.BillStatus.DRAFT) {
            throw new RuntimeException("Return bill is not in DRAFT status");
        }

        if (returnBill.getBillType() != Bill.BillType.SALES_RETURN) {
            throw new RuntimeException("Bill is not a return bill");
        }

        // 2. Get return items
        List<BillItem> returnItems = billItemRepository.findByBillId(returnBillId);

        // 3. Adjust stock for each returned item (add back to inventory)
        for (BillItem item : returnItems) {
            // Note: item.quantity is negative, so we negate it to add back to stock
            int returnedQuantity = Math.abs(item.getQuantity());

            stockService.recordStockMovement(
                    tenantId,
                    returnBill.getStoreId(),
                    item.getVariantId(),
                    StockLedger.TransactionType.RETURN,
                    returnBillId,
                    returnedQuantity,  // Positive quantity to add back
                    item.getUnitPrice(), // Cost price
                    item.getUnitPrice(), // Selling price
                    "Stock returned via Bill: " + returnBill.getBillNumber(),
                    returnBill.getBillDate(),
                    returnBill.getCashierId()
            );
        }

        // 4. Update bill status
        returnBill.setStatus(Bill.BillStatus.CONFIRMED);
        returnBill = billRepository.save(returnBill);

        log.info("Return bill confirmed: {}, stock adjusted", returnBill.getBillNumber());

        // 5. Return bill response
        return billService.getBillById(returnBillId, tenantId);
    }

    @Transactional
    public BillResponse processRefund(ProcessRefundRequest request) {
        log.info("Processing refund for return bill ID: {}, amount: {}, method: {}",
                request.getReturnBillId(), request.getRefundAmount(), request.getRefundMethod());

        // 1. Get return bill
        Bill returnBill = billRepository.findByIdAndTenantId(request.getReturnBillId(), request.getTenantId())
                .orElseThrow(() -> new RuntimeException("Return bill not found"));

        if (returnBill.getBillType() != Bill.BillType.SALES_RETURN) {
            throw new RuntimeException("Bill is not a return bill");
        }

        if (returnBill.getStatus() != Bill.BillStatus.CONFIRMED) {
            throw new RuntimeException("Return bill must be confirmed before processing refund");
        }

        // 2. Validate refund amount
        BigDecimal totalReturnAmount = returnBill.getTotalAmount().abs(); // Make positive
        BigDecimal alreadyRefunded = returnBill.getPaidAmount().abs(); // Already refunded amount
        BigDecimal balanceToRefund = totalReturnAmount.subtract(alreadyRefunded);

        if (request.getRefundAmount().compareTo(balanceToRefund) > 0) {
            throw new RuntimeException("Refund amount exceeds balance to refund: " + balanceToRefund);
        }

        // 3. Create refund payment record
        Payment refundPayment = new Payment();
        refundPayment.setBillId(returnBill.getId());
        refundPayment.setTenantId(request.getTenantId());
        refundPayment.setPaymentMethod(Payment.PaymentMethod.valueOf(request.getRefundMethod()));
        refundPayment.setPaymentDate(LocalDateTime.now());
        refundPayment.setAmount(request.getRefundAmount().negate()); // Negative for refund
        refundPayment.setReferenceNumber(request.getReferenceNumber());
        refundPayment.setNotes(request.getNotes());
        refundPayment.setCreatedBy(returnBill.getCashierId());

        refundPayment = paymentRepository.save(refundPayment);

        // 4. Update return bill payment status
        BigDecimal newPaidAmount = returnBill.getPaidAmount().subtract(request.getRefundAmount());
        BigDecimal newBalance = returnBill.getTotalAmount().subtract(newPaidAmount);

        returnBill.setPaidAmount(newPaidAmount);
        returnBill.setBalanceAmount(newBalance);

        // Update payment status
        if (newBalance.abs().compareTo(new BigDecimal("0.01")) < 0) {
            returnBill.setPaymentStatus(Bill.PaymentStatus.REFUNDED);
        } else {
            returnBill.setPaymentStatus(Bill.PaymentStatus.PARTIAL);
        }

        returnBill = billRepository.save(returnBill);

        log.info("Refund processed: ₹{} via {}, payment status: {}",
                request.getRefundAmount(), request.getRefundMethod(), returnBill.getPaymentStatus());

        // 5. Return updated bill
        return billService.getBillById(returnBill.getId(), request.getTenantId());
    }

    private void validateReturnQuantities(CreateReturnRequest request, List<BillItem> originalItems) {
        for (var returnItemReq : request.getItems()) {
            BillItem originalItem = originalItems.stream()
                    .filter(item -> item.getId().equals(returnItemReq.getBillItemId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Bill item not found: " + returnItemReq.getBillItemId()));

            if (returnItemReq.getQuantity() > originalItem.getQuantity()) {
                throw new RuntimeException("Cannot return more items than purchased. Item: " +
                        originalItem.getProductName() + ", purchased: " + originalItem.getQuantity() +
                        ", returning: " + returnItemReq.getQuantity());
            }

            if (returnItemReq.getQuantity() <= 0) {
                throw new RuntimeException("Return quantity must be greater than 0");
            }
        }
    }

    private String generateReturnBillNumber(Long tenantId) {
        String dateStr = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "RET-" + dateStr + "-";

        // Find last return bill number for today
        String lastNumber = billRepository.findLastBillNumberLike(tenantId, prefix + "%");

        int sequence = 1;
        if (lastNumber != null && lastNumber.length() > 13) {
            try {
                sequence = Integer.parseInt(lastNumber.substring(13)) + 1;
            } catch (NumberFormatException e) {
                log.warn("Could not parse sequence from last bill number: {}", lastNumber);
            }
        }

        return prefix + String.format("%03d", sequence);
    }
}

package com.atozshop.service;

import com.atozshop.dto.request.CreatePaymentRequest;
import com.atozshop.dto.response.PaymentResponse;
import com.atozshop.entity.Bill;
import com.atozshop.entity.Payment;
import com.atozshop.exception.PaymentException;
import com.atozshop.exception.ResourceNotFoundException;
import com.atozshop.repository.BillRepository;
import com.atozshop.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BillRepository billRepository;

    @Transactional
    public PaymentResponse processPayment(CreatePaymentRequest request) {
        // Get bill
        Bill bill = billRepository.findByIdAndTenantId(request.getBillId(), request.getTenantId())
            .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));

        // Validate bill status
        if (bill.getStatus() != Bill.BillStatus.CONFIRMED) {
            throw new PaymentException("Payment can only be made for CONFIRMED bills");
        }

        // Validate amount
        if (request.getAmount().compareTo(bill.getBalanceAmount()) > 0) {
            throw new PaymentException("Payment amount cannot exceed balance amount");
        }

        // Create payment
        Payment payment = Payment.builder()
            .billId(request.getBillId())
            .tenantId(request.getTenantId())
            .paymentMethod(Payment.PaymentMethod.valueOf(request.getPaymentMethod()))
            .paymentDate(LocalDateTime.now())
            .amount(request.getAmount())
            .referenceNumber(request.getReferenceNumber())
            .cardLast4(request.getCardLast4())
            .upiId(request.getUpiId())
            .bankName(request.getBankName())
            .notes(request.getNotes())
            .createdBy(request.getCreatedBy())
            .build();

        payment = paymentRepository.save(payment);

        // Update bill
        BigDecimal newPaidAmount = bill.getPaidAmount().add(request.getAmount());
        BigDecimal newBalanceAmount = bill.getTotalAmount().subtract(newPaidAmount);

        bill.setPaidAmount(newPaidAmount);
        bill.setBalanceAmount(newBalanceAmount);

        // Update payment status
        if (newBalanceAmount.compareTo(BigDecimal.ZERO) == 0) {
            bill.setPaymentStatus(Bill.PaymentStatus.PAID);
        } else if (newPaidAmount.compareTo(BigDecimal.ZERO) > 0) {
            bill.setPaymentStatus(Bill.PaymentStatus.PARTIAL);
        }

        billRepository.save(bill);

        return mapToResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByBill(Long billId) {
        return paymentRepository.findByBillId(billId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByDateRange(Long tenantId, LocalDateTime from, LocalDateTime to) {
        return paymentRepository.findByTenantIdAndPaymentDateBetween(tenantId, from, to)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<com.atozshop.dto.response.PaymentSummaryResponse> getPaymentSummary(Long tenantId) {
        List<Payment> payments = paymentRepository.findByTenantId(tenantId);

        Map<Payment.PaymentMethod, List<Payment>> groupedPayments = payments.stream()
                .collect(Collectors.groupingBy(Payment::getPaymentMethod));

        return groupedPayments.entrySet().stream()
                .map(entry -> {
                    BigDecimal totalAmount = entry.getValue().stream()
                            .map(Payment::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return com.atozshop.dto.response.PaymentSummaryResponse.builder()
                            .paymentMethod(entry.getKey().name())
                            .totalAmount(totalAmount)
                            .transactionCount((long) entry.getValue().size())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private PaymentResponse mapToResponse(Payment payment) {
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

package com.atozshop.controller;

import com.atozshop.dto.request.CreatePaymentRequest;
import com.atozshop.dto.response.PaymentResponse;
import com.atozshop.security.CurrentUser;
import com.atozshop.security.UserPrincipal;
import com.atozshop.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "APIs for managing bill payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Process payment", description = "Processes a payment for a bill and updates payment status")
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody CreatePaymentRequest request) {
        PaymentResponse response = paymentService.processPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/summary")
    @Operation(summary = "Get payment summary", description = "Retrieves payment summary statistics")
    public ResponseEntity<List<com.atozshop.dto.response.PaymentSummaryResponse>> getPaymentSummary(@CurrentUser UserPrincipal user) {
        List<com.atozshop.dto.response.PaymentSummaryResponse> summary = paymentService.getPaymentSummary(user.getTenantId());
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/bill/{billId}")
    @Operation(summary = "Get payments by bill", description = "Retrieves all payments for a specific bill")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByBill(@PathVariable Long billId) {
        List<PaymentResponse> payments = paymentService.getPaymentsByBill(billId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/range")
    @Operation(summary = "Get payments by date range", description = "Retrieves all payments within a date range")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByDateRange(
        @CurrentUser UserPrincipal user,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        List<PaymentResponse> payments = paymentService.getPaymentsByDateRange(user.getTenantId(), from, to);
        return ResponseEntity.ok(payments);
    }
}

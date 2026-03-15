package com.atozshop.controller;

import com.atozshop.dto.request.AddBillItemRequest;
import com.atozshop.dto.request.CreateBillRequest;
import com.atozshop.dto.request.CreateReturnRequest;
import com.atozshop.dto.request.ProcessRefundRequest;
import com.atozshop.dto.request.UpdateBillItemRequest;
import com.atozshop.dto.response.BillResponse;
import com.atozshop.dto.response.BillSummaryResponse;
import com.atozshop.security.CurrentUser;
import com.atozshop.security.UserPrincipal;
import com.atozshop.service.BillService;
import com.atozshop.service.ReceiptService;
import com.atozshop.service.ReturnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bills")
@RequiredArgsConstructor
@Tag(name = "Bill Management", description = "APIs for managing sales bills and invoices")
public class BillController {

    private final BillService billService;
    private final ReceiptService receiptService;
    private final ReturnService returnService;

    @PostMapping
    @Operation(summary = "Create a new bill", description = "Creates a new sales bill with items in DRAFT status")
    public ResponseEntity<BillResponse> createBill(@Valid @RequestBody CreateBillRequest request) {
        BillResponse response = billService.createBill(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all bills", description = "Retrieves all bills for a tenant and store")
    public ResponseEntity<List<BillSummaryResponse>> getAllBills(@CurrentUser UserPrincipal user) {
        List<BillSummaryResponse> bills = billService.getAllBills(user.getTenantId(), user.getStoreIdOrDefault());
        return ResponseEntity.ok(bills);
    }

    @GetMapping("/summary")
    @Operation(summary = "Get bills summary", description = "Retrieves summary statistics for all bills")
    public ResponseEntity<BillSummaryResponse> getBillsSummary(@CurrentUser UserPrincipal user) {
        BillSummaryResponse summary = billService.getBillsSummary(user.getTenantId(), user.getStoreIdOrDefault());
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get bill by ID", description = "Retrieves a specific bill with all details")
    public ResponseEntity<BillResponse> getBillById(
        @CurrentUser UserPrincipal user,
        @PathVariable Long id
    ) {
        BillResponse bill = billService.getBillById(id, user.getTenantId());
        return ResponseEntity.ok(bill);
    }

    @GetMapping("/number/{billNumber}")
    @Operation(summary = "Get bill by number", description = "Retrieves a bill by its bill number")
    public ResponseEntity<BillResponse> getBillByNumber(
        @CurrentUser UserPrincipal user,
        @PathVariable String billNumber
    ) {
        BillResponse bill = billService.getBillByNumber(billNumber, user.getTenantId());
        return ResponseEntity.ok(bill);
    }

    @PostMapping("/{id}/items")
    @Operation(summary = "Add item to bill", description = "Adds a new item to a DRAFT bill")
    public ResponseEntity<BillResponse> addItemToBill(
        @CurrentUser UserPrincipal user,
        @PathVariable Long id,
        @Valid @RequestBody AddBillItemRequest request
    ) {
        BillResponse bill = billService.addItemToBill(id, request, user.getTenantId(), user.getStoreIdOrDefault());
        return ResponseEntity.ok(bill);
    }

    @PutMapping("/{id}/items/{itemId}")
    @Operation(summary = "Update bill item", description = "Updates quantity or discount for a bill item in DRAFT bill")
    public ResponseEntity<BillResponse> updateBillItem(
        @CurrentUser UserPrincipal user,
        @PathVariable Long id,
        @PathVariable Long itemId,
        @Valid @RequestBody UpdateBillItemRequest request
    ) {
        BillResponse bill = billService.updateBillItem(id, itemId, request, user.getTenantId());
        return ResponseEntity.ok(bill);
    }

    @DeleteMapping("/{id}/items/{itemId}")
    @Operation(summary = "Remove item from bill", description = "Removes an item from a DRAFT bill")
    public ResponseEntity<BillResponse> removeItemFromBill(
        @CurrentUser UserPrincipal user,
        @PathVariable Long id,
        @PathVariable Long itemId
    ) {
        BillResponse bill = billService.removeItemFromBill(id, itemId, user.getTenantId());
        return ResponseEntity.ok(bill);
    }

    @PostMapping("/{id}/confirm")
    @Operation(
        summary = "Confirm bill and deduct stock",
        description = "Confirms the bill, deducts stock via Phase 1 ledger, and makes it immutable. This is the critical integration point with Phase 1."
    )
    public ResponseEntity<BillResponse> confirmBill(
        @CurrentUser UserPrincipal user,
        @PathVariable Long id
    ) {
        BillResponse bill = billService.confirmBill(id, user.getTenantId());
        return ResponseEntity.ok(bill);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel bill", description = "Cancels a DRAFT bill (only DRAFT bills can be cancelled)")
    public ResponseEntity<BillResponse> cancelBill(
        @CurrentUser UserPrincipal user,
        @PathVariable Long id
    ) {
        BillResponse bill = billService.cancelBill(id, user.getTenantId());
        return ResponseEntity.ok(bill);
    }

    @GetMapping("/{id}/receipt/pdf")
    @Operation(summary = "Generate PDF receipt", description = "Generates a PDF invoice/receipt for the bill")
    public ResponseEntity<byte[]> generatePDFReceipt(
        @CurrentUser UserPrincipal user,
        @PathVariable Long id
    ) {
        byte[] pdfBytes = receiptService.generateReceiptPDF(id, user.getTenantId());

        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=receipt-" + id + ".pdf")
                .body(pdfBytes);
    }

    @GetMapping("/{id}/receipt/thermal")
    @Operation(summary = "Generate thermal printer receipt", description = "Generates plain text receipt for 80mm thermal printers")
    public ResponseEntity<String> generateThermalReceipt(
        @CurrentUser UserPrincipal user,
        @PathVariable Long id
    ) {
        String receipt = receiptService.generateThermalReceipt(id, user.getTenantId());

        return ResponseEntity.ok()
                .header("Content-Type", "text/plain")
                .body(receipt);
    }

    @PostMapping("/{id}/return")
    @Operation(
        summary = "Create return bill for original bill",
        description = "Creates a SALES_RETURN bill for items being returned from an original sales bill. Automatically adjusts stock."
    )
    public ResponseEntity<BillResponse> createReturn(
        @PathVariable Long id,
        @Valid @RequestBody CreateReturnRequest request
    ) {
        // Set original bill ID from path parameter
        request.setOriginalBillId(id);

        BillResponse returnBill = returnService.createReturnBill(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(returnBill);
    }

    @PostMapping("/{id}/refund")
    @Operation(
        summary = "Process refund for return bill",
        description = "Records a refund payment for a SALES_RETURN bill"
    )
    public ResponseEntity<BillResponse> processRefund(
        @PathVariable Long id,
        @Valid @RequestBody ProcessRefundRequest request
    ) {
        // Set return bill ID from path parameter
        request.setReturnBillId(id);

        BillResponse bill = returnService.processRefund(request);
        return ResponseEntity.ok(bill);
    }
}

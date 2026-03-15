package com.atozshop.controller;

import com.atozshop.dto.request.CreateCustomerRequest;
import com.atozshop.dto.request.UpdateCustomerRequest;
import com.atozshop.dto.response.CustomerPurchaseHistoryResponse;
import com.atozshop.dto.response.CustomerResponse;
import com.atozshop.security.CurrentUser;
import com.atozshop.security.UserPrincipal;
import com.atozshop.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customer Management", description = "APIs for managing customers")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @Operation(summary = "Create a new customer", description = "Creates a new customer with auto-generated customer code")
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        CustomerResponse response = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all customers", description = "Retrieves all active customers for a tenant")
    public ResponseEntity<List<CustomerResponse>> getAllCustomers(@CurrentUser UserPrincipal user) {
        List<CustomerResponse> customers = customerService.getAllCustomers(user.getTenantId());
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/search")
    @Operation(summary = "Search customers", description = "Search customers by name or phone")
    public ResponseEntity<List<CustomerResponse>> searchCustomers(
        @CurrentUser UserPrincipal user,
        @RequestParam String keyword
    ) {
        List<CustomerResponse> customers = customerService.searchCustomers(keyword, user.getTenantId());
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID", description = "Retrieves a specific customer by ID")
    public ResponseEntity<CustomerResponse> getCustomerById(
        @CurrentUser UserPrincipal user,
        @PathVariable Long id
    ) {
        CustomerResponse customer = customerService.getCustomerById(id, user.getTenantId());
        return ResponseEntity.ok(customer);
    }

    @GetMapping("/phone/{phone}")
    @Operation(summary = "Find customer by phone", description = "Finds a customer by phone number")
    public ResponseEntity<CustomerResponse> getCustomerByPhone(
        @CurrentUser UserPrincipal user,
        @PathVariable String phone
    ) {
        CustomerResponse customer = customerService.getCustomerByPhone(phone, user.getTenantId());
        return ResponseEntity.ok(customer);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update customer", description = "Updates customer information")
    public ResponseEntity<CustomerResponse> updateCustomer(
        @CurrentUser UserPrincipal user,
        @PathVariable Long id,
        @Valid @RequestBody UpdateCustomerRequest request
    ) {
        CustomerResponse customer = customerService.updateCustomer(id, request, user.getTenantId());
        return ResponseEntity.ok(customer);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete customer", description = "Soft deletes a customer (sets isActive to false)")
    public ResponseEntity<Void> deleteCustomer(
        @CurrentUser UserPrincipal user,
        @PathVariable Long id
    ) {
        customerService.deleteCustomer(id, user.getTenantId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/purchase-history")
    @Operation(summary = "Get customer purchase history", description = "Retrieves customer's purchase history and statistics")
    public ResponseEntity<CustomerPurchaseHistoryResponse> getCustomerPurchaseHistory(
        @CurrentUser UserPrincipal user,
        @PathVariable Long id
    ) {
        CustomerPurchaseHistoryResponse history = customerService.getCustomerPurchaseHistory(id, user.getTenantId());
        return ResponseEntity.ok(history);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user's customer record", description = "Gets or creates a customer record for the logged-in user")
    public ResponseEntity<CustomerResponse> getCurrentUserCustomer(@CurrentUser UserPrincipal userPrincipal) {
        CustomerResponse customer = customerService.getOrCreateCustomerForUser(
            userPrincipal.getEmail(),
            userPrincipal.getTenantId(),
            userPrincipal.getFullName(),
            userPrincipal.getPhone()
        );
        return ResponseEntity.ok(customer);
    }
}

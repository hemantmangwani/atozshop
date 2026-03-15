package com.atozshop.controller;

import com.atozshop.dto.request.AddAddressRequest;
import com.atozshop.dto.request.UpdateAddressRequest;
import com.atozshop.dto.response.AddressResponse;
import com.atozshop.entity.CustomerAddress;
import com.atozshop.repository.CustomerAddressRepository;
import com.atozshop.security.CurrentUser;
import com.atozshop.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/customers/addresses")
@RequiredArgsConstructor
@Tag(name = "Customer Addresses", description = "APIs for managing customer delivery addresses")
public class CustomerAddressController {

    private final CustomerAddressRepository addressRepository;

    @PostMapping
    @Operation(summary = "Add address", description = "Add a new delivery address for customer")
    @Transactional
    public ResponseEntity<AddressResponse> addAddress(@Valid @RequestBody AddAddressRequest request) {
        // If this is marked as default, clear other defaults
        if (request.getIsDefault() != null && request.getIsDefault()) {
            addressRepository.clearDefaultForCustomer(request.getCustomerId());
        }

        CustomerAddress address = CustomerAddress.builder()
                .customerId(request.getCustomerId())
                .addressType(request.getAddressType() != null ?
                        CustomerAddress.AddressType.valueOf(request.getAddressType()) : null)
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .landmark(request.getLandmark())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry() != null ? request.getCountry() : "India")
                .phone(request.getPhone())
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .build();

        address = addressRepository.save(address);

        return ResponseEntity.status(HttpStatus.CREATED).body(buildAddressResponse(address));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get customer addresses", description = "Get all addresses for a customer")
    public ResponseEntity<List<AddressResponse>> getCustomerAddresses(@PathVariable Long customerId) {
        List<CustomerAddress> addresses = addressRepository.findByCustomerId(customerId);

        List<AddressResponse> responses = addresses.stream()
                .map(this::buildAddressResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/my")
    @Operation(summary = "Get my addresses", description = "Get all addresses for the logged-in customer")
    public ResponseEntity<List<AddressResponse>> getMyAddresses(@CurrentUser UserPrincipal user) {
        if (user.getCustomerId() == null) {
            throw new RuntimeException("User does not have a customer profile");
        }
        List<CustomerAddress> addresses = addressRepository.findByCustomerId(user.getCustomerId());

        List<AddressResponse> responses = addresses.stream()
                .map(this::buildAddressResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get address by ID", description = "Get a specific address by ID")
    public ResponseEntity<AddressResponse> getAddressById(
            @CurrentUser UserPrincipal user,
            @PathVariable Long id
    ) {
        Long customerId = user.getCustomerId();
        if (customerId == null) {
            throw new RuntimeException("User does not have a customer profile");
        }
        CustomerAddress address = addressRepository.findByIdAndCustomerId(id, customerId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        return ResponseEntity.ok(buildAddressResponse(address));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update address", description = "Update an existing address")
    @Transactional
    public ResponseEntity<AddressResponse> updateAddress(
            @CurrentUser UserPrincipal user,
            @PathVariable Long id,
            @Valid @RequestBody UpdateAddressRequest request
    ) {
        Long customerId = user.getCustomerId();
        if (customerId == null) {
            throw new RuntimeException("User does not have a customer profile");
        }
        CustomerAddress address = addressRepository.findByIdAndCustomerId(id, customerId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        // If this is marked as default, clear other defaults
        if (request.getIsDefault() != null && request.getIsDefault()) {
            addressRepository.clearDefaultForCustomer(customerId);
        }

        // Update fields
        if (request.getAddressType() != null) {
            address.setAddressType(CustomerAddress.AddressType.valueOf(request.getAddressType()));
        }
        if (request.getAddressLine1() != null) {
            address.setAddressLine1(request.getAddressLine1());
        }
        if (request.getAddressLine2() != null) {
            address.setAddressLine2(request.getAddressLine2());
        }
        if (request.getLandmark() != null) {
            address.setLandmark(request.getLandmark());
        }
        if (request.getCity() != null) {
            address.setCity(request.getCity());
        }
        if (request.getState() != null) {
            address.setState(request.getState());
        }
        if (request.getPostalCode() != null) {
            address.setPostalCode(request.getPostalCode());
        }
        if (request.getCountry() != null) {
            address.setCountry(request.getCountry());
        }
        if (request.getPhone() != null) {
            address.setPhone(request.getPhone());
        }
        if (request.getIsDefault() != null) {
            address.setIsDefault(request.getIsDefault());
        }

        address = addressRepository.save(address);

        return ResponseEntity.ok(buildAddressResponse(address));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete address", description = "Delete a customer address")
    @Transactional
    public ResponseEntity<Void> deleteAddress(
            @CurrentUser UserPrincipal user,
            @PathVariable Long id
    ) {
        Long customerId = user.getCustomerId();
        if (customerId == null) {
            throw new RuntimeException("User does not have a customer profile");
        }
        if (!addressRepository.existsByIdAndCustomerId(id, customerId)) {
            throw new RuntimeException("Address not found");
        }

        addressRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/default")
    @Operation(summary = "Set as default", description = "Set an address as the default delivery address")
    @Transactional
    public ResponseEntity<AddressResponse> setAsDefault(
            @CurrentUser UserPrincipal user,
            @PathVariable Long id
    ) {
        Long customerId = user.getCustomerId();
        if (customerId == null) {
            throw new RuntimeException("User does not have a customer profile");
        }
        CustomerAddress address = addressRepository.findByIdAndCustomerId(id, customerId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        // Clear other defaults
        addressRepository.clearDefaultForCustomer(customerId);

        // Set this as default
        address.setIsDefault(true);
        address = addressRepository.save(address);

        return ResponseEntity.ok(buildAddressResponse(address));
    }

    // Helper method
    private AddressResponse buildAddressResponse(CustomerAddress address) {
        return AddressResponse.builder()
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
                .createdAt(address.getCreatedAt())
                .updatedAt(address.getUpdatedAt())
                .build();
    }
}

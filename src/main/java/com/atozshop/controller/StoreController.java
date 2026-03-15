package com.atozshop.controller;

import com.atozshop.dto.request.CreateStoreRequest;
import com.atozshop.dto.request.UpdateStoreRequest;
import com.atozshop.dto.response.StoreResponse;
import com.atozshop.security.CurrentUser;
import com.atozshop.security.UserPrincipal;
import com.atozshop.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
@Tag(name = "Store Management", description = "APIs for managing physical store locations")
public class StoreController {

    private final StoreService storeService;

    @PostMapping
    @Operation(summary = "Create a new store")
    public ResponseEntity<StoreResponse> createStore(@Valid @RequestBody CreateStoreRequest request) {
        StoreResponse response = storeService.createStore(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get store by ID")
    public ResponseEntity<StoreResponse> getStoreById(
            @CurrentUser UserPrincipal user,
            @PathVariable Long id) {
        StoreResponse response = storeService.getStoreById(id, user.getTenantId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get store by code")
    public ResponseEntity<StoreResponse> getStoreByCode(
            @CurrentUser UserPrincipal user,
            @PathVariable String code) {
        StoreResponse response = storeService.getStoreByCode(code, user.getTenantId());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all active stores")
    public ResponseEntity<List<StoreResponse>> getAllStores(@CurrentUser UserPrincipal user) {
        List<StoreResponse> response = storeService.getAllStores(user.getTenantId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update store")
    public ResponseEntity<StoreResponse> updateStore(
            @CurrentUser UserPrincipal user,
            @PathVariable Long id,
            @Valid @RequestBody UpdateStoreRequest request) {
        StoreResponse response = storeService.updateStore(id, user.getTenantId(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete store (soft delete)")
    public ResponseEntity<Void> deleteStore(
            @CurrentUser UserPrincipal user,
            @PathVariable Long id) {
        storeService.deleteStore(id, user.getTenantId());
        return ResponseEntity.noContent().build();
    }
}

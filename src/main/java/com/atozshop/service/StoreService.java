package com.atozshop.service;

import com.atozshop.dto.request.CreateStoreRequest;
import com.atozshop.dto.request.UpdateStoreRequest;
import com.atozshop.dto.response.StoreResponse;
import com.atozshop.entity.Store;
import com.atozshop.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;

    @Transactional
    public StoreResponse createStore(CreateStoreRequest request) {
        // Check if code already exists
        if (storeRepository.existsByCodeAndTenantId(request.getCode(), request.getTenantId())) {
            throw new IllegalArgumentException("Store code already exists: " + request.getCode());
        }

        Store store = Store.builder()
                .tenantId(request.getTenantId())
                .name(request.getName())
                .code(request.getCode())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .phone(request.getPhone())
                .email(request.getEmail())
                .gstNumber(request.getGstNumber())
                .logoUrl(request.getLogoUrl())
                .isActive(true)
                .build();

        Store saved = storeRepository.save(store);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public StoreResponse getStoreById(Long id, Long tenantId) {
        Store store = storeRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found with ID: " + id));
        return mapToResponse(store);
    }

    @Transactional(readOnly = true)
    public StoreResponse getStoreByCode(String code, Long tenantId) {
        Store store = storeRepository.findByCodeAndTenantId(code, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found with code: " + code));
        return mapToResponse(store);
    }

    @Transactional(readOnly = true)
    public List<StoreResponse> getAllStores(Long tenantId) {
        return storeRepository.findByTenantIdAndIsActive(tenantId, true).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public StoreResponse updateStore(Long id, Long tenantId, UpdateStoreRequest request) {
        Store store = storeRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found with ID: " + id));

        if (request.getName() != null) {
            store.setName(request.getName());
        }
        if (request.getAddress() != null) {
            store.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            store.setCity(request.getCity());
        }
        if (request.getState() != null) {
            store.setState(request.getState());
        }
        if (request.getPostalCode() != null) {
            store.setPostalCode(request.getPostalCode());
        }
        if (request.getCountry() != null) {
            store.setCountry(request.getCountry());
        }
        if (request.getPhone() != null) {
            store.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            store.setEmail(request.getEmail());
        }
        if (request.getGstNumber() != null) {
            store.setGstNumber(request.getGstNumber());
        }
        if (request.getLogoUrl() != null) {
            store.setLogoUrl(request.getLogoUrl());
        }
        if (request.getIsActive() != null) {
            store.setIsActive(request.getIsActive());
        }

        Store updated = storeRepository.save(store);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteStore(Long id, Long tenantId) {
        Store store = storeRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found with ID: " + id));

        // Soft delete
        store.setIsActive(false);
        storeRepository.save(store);
    }

    private StoreResponse mapToResponse(Store store) {
        return StoreResponse.builder()
                .id(store.getId())
                .tenantId(store.getTenantId())
                .name(store.getName())
                .code(store.getCode())
                .address(store.getAddress())
                .city(store.getCity())
                .state(store.getState())
                .postalCode(store.getPostalCode())
                .country(store.getCountry())
                .phone(store.getPhone())
                .email(store.getEmail())
                .gstNumber(store.getGstNumber())
                .logoUrl(store.getLogoUrl())
                .isActive(store.getIsActive())
                .createdAt(store.getCreatedAt())
                .updatedAt(store.getUpdatedAt())
                .build();
    }
}

package com.atozshop.service;

import com.atozshop.dto.request.CreateSupplierRequest;
import com.atozshop.dto.request.UpdateSupplierRequest;
import com.atozshop.dto.response.SupplierResponse;
import com.atozshop.entity.Supplier;
import com.atozshop.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;

    @Transactional
    public SupplierResponse createSupplier(CreateSupplierRequest request) {
        // Generate supplier code: SUP-YYYYMMDD-XXX
        String supplierCode = generateSupplierCode(request.getTenantId());

        Supplier.SupplierType type = Supplier.SupplierType.LOCAL;
        if (request.getSupplierType() != null) {
            try {
                type = Supplier.SupplierType.valueOf(request.getSupplierType().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid supplier type: " + request.getSupplierType());
            }
        }

        Supplier supplier = Supplier.builder()
                .tenantId(request.getTenantId())
                .code(supplierCode)
                .name(request.getName())
                .contactPerson(request.getContactPerson())
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .gstNumber(request.getGstNumber())
                .panNumber(request.getPanNumber())
                .bankName(request.getBankName())
                .bankAccountNumber(request.getBankAccountNumber())
                .bankIfscCode(request.getBankIfscCode())
                .supplierType(type)
                .isActive(true)
                .notes(request.getNotes())
                .build();

        Supplier saved = supplierRepository.save(supplier);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public SupplierResponse getSupplierById(Long id, Long tenantId) {
        Supplier supplier = supplierRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found with ID: " + id));
        return mapToResponse(supplier);
    }

    @Transactional(readOnly = true)
    public SupplierResponse getSupplierByCode(String code, Long tenantId) {
        Supplier supplier = supplierRepository.findByCodeAndTenantId(code, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found with code: " + code));
        return mapToResponse(supplier);
    }

    @Transactional(readOnly = true)
    public List<SupplierResponse> getAllSuppliers(Long tenantId) {
        return supplierRepository.findByTenantIdAndIsActive(tenantId, true).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SupplierResponse> searchSuppliers(Long tenantId, String keyword) {
        return supplierRepository.searchByNameOrPhoneOrEmail(tenantId, keyword).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SupplierResponse updateSupplier(Long id, Long tenantId, UpdateSupplierRequest request) {
        Supplier supplier = supplierRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found with ID: " + id));

        if (request.getName() != null) {
            supplier.setName(request.getName());
        }
        if (request.getContactPerson() != null) {
            supplier.setContactPerson(request.getContactPerson());
        }
        if (request.getPhone() != null) {
            supplier.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            supplier.setEmail(request.getEmail());
        }
        if (request.getAddress() != null) {
            supplier.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            supplier.setCity(request.getCity());
        }
        if (request.getState() != null) {
            supplier.setState(request.getState());
        }
        if (request.getPostalCode() != null) {
            supplier.setPostalCode(request.getPostalCode());
        }
        if (request.getCountry() != null) {
            supplier.setCountry(request.getCountry());
        }
        if (request.getGstNumber() != null) {
            supplier.setGstNumber(request.getGstNumber());
        }
        if (request.getPanNumber() != null) {
            supplier.setPanNumber(request.getPanNumber());
        }
        if (request.getBankName() != null) {
            supplier.setBankName(request.getBankName());
        }
        if (request.getBankAccountNumber() != null) {
            supplier.setBankAccountNumber(request.getBankAccountNumber());
        }
        if (request.getBankIfscCode() != null) {
            supplier.setBankIfscCode(request.getBankIfscCode());
        }
        if (request.getSupplierType() != null) {
            try {
                supplier.setSupplierType(Supplier.SupplierType.valueOf(request.getSupplierType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid supplier type: " + request.getSupplierType());
            }
        }
        if (request.getIsActive() != null) {
            supplier.setIsActive(request.getIsActive());
        }
        if (request.getNotes() != null) {
            supplier.setNotes(request.getNotes());
        }

        Supplier updated = supplierRepository.save(supplier);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteSupplier(Long id, Long tenantId) {
        Supplier supplier = supplierRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found with ID: " + id));

        // Soft delete
        supplier.setIsActive(false);
        supplierRepository.save(supplier);
    }

    private String generateSupplierCode(Long tenantId) {
        LocalDate today = LocalDate.now();
        String dateStr = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String datePrefix = "SUP-" + dateStr + "-%";

        Integer lastSequence = supplierRepository.findLastSequenceForDate(tenantId, datePrefix);
        int nextSequence = (lastSequence != null ? lastSequence : 0) + 1;

        return String.format("SUP-%s-%03d", dateStr, nextSequence);
    }

    private SupplierResponse mapToResponse(Supplier supplier) {
        return SupplierResponse.builder()
                .id(supplier.getId())
                .tenantId(supplier.getTenantId())
                .code(supplier.getCode())
                .name(supplier.getName())
                .contactPerson(supplier.getContactPerson())
                .phone(supplier.getPhone())
                .email(supplier.getEmail())
                .address(supplier.getAddress())
                .city(supplier.getCity())
                .state(supplier.getState())
                .postalCode(supplier.getPostalCode())
                .country(supplier.getCountry())
                .gstNumber(supplier.getGstNumber())
                .panNumber(supplier.getPanNumber())
                .bankName(supplier.getBankName())
                .bankAccountNumber(supplier.getBankAccountNumber())
                .bankIfscCode(supplier.getBankIfscCode())
                .supplierType(supplier.getSupplierType().name())
                .isActive(supplier.getIsActive())
                .notes(supplier.getNotes())
                .createdAt(supplier.getCreatedAt())
                .updatedAt(supplier.getUpdatedAt())
                .build();
    }
}

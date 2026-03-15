package com.atozshop.service;

import com.atozshop.dto.request.CreateDiscountRequest;
import com.atozshop.dto.request.UpdateDiscountRequest;
import com.atozshop.dto.response.DiscountResponse;
import com.atozshop.entity.Discount;
import com.atozshop.exception.ResourceNotFoundException;
import com.atozshop.repository.DiscountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiscountService {

    private final DiscountRepository discountRepository;

    @Transactional
    public DiscountResponse createDiscount(CreateDiscountRequest request) {
        // Validate discount code uniqueness
        if (discountRepository.existsByDiscountCodeAndTenantId(request.getDiscountCode(), request.getTenantId())) {
            throw new IllegalArgumentException("Discount code already exists: " + request.getDiscountCode());
        }

        Discount discount = Discount.builder()
            .tenantId(request.getTenantId())
            .discountCode(request.getDiscountCode())
            .name(request.getName())
            .description(request.getDescription())
            .discountType(Discount.DiscountType.valueOf(request.getDiscountType()))
            .discountValue(request.getDiscountValue())
            .minPurchaseAmount(request.getMinPurchaseAmount())
            .maxDiscountAmount(request.getMaxDiscountAmount())
            .applicableOn(Discount.ApplicableOn.valueOf(request.getApplicableOn()))
            .validFrom(request.getValidFrom())
            .validTo(request.getValidTo())
            .isActive(true)
            .build();

        discount = discountRepository.save(discount);
        return mapToResponse(discount);
    }

    @Transactional(readOnly = true)
    public DiscountResponse getDiscountById(Long id, Long tenantId) {
        Discount discount = discountRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Discount not found with id: " + id));

        return mapToResponse(discount);
    }

    @Transactional(readOnly = true)
    public DiscountResponse getDiscountByCode(String code, Long tenantId) {
        Discount discount = discountRepository.findByDiscountCodeAndTenantId(code, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Discount not found with code: " + code));

        return mapToResponse(discount);
    }

    @Transactional(readOnly = true)
    public List<DiscountResponse> getAllActiveDiscounts(Long tenantId) {
        return discountRepository.findAllActiveDiscounts(tenantId, LocalDate.now())
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DiscountResponse> getAllDiscounts(Long tenantId) {
        return discountRepository.findByTenantIdAndIsActive(tenantId, true)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public DiscountResponse updateDiscount(Long id, UpdateDiscountRequest request, Long tenantId) {
        Discount discount = discountRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Discount not found with id: " + id));

        if (request.getName() != null) discount.setName(request.getName());
        if (request.getDescription() != null) discount.setDescription(request.getDescription());
        if (request.getDiscountType() != null)
            discount.setDiscountType(Discount.DiscountType.valueOf(request.getDiscountType()));
        if (request.getDiscountValue() != null) discount.setDiscountValue(request.getDiscountValue());
        if (request.getMinPurchaseAmount() != null) discount.setMinPurchaseAmount(request.getMinPurchaseAmount());
        if (request.getMaxDiscountAmount() != null) discount.setMaxDiscountAmount(request.getMaxDiscountAmount());
        if (request.getApplicableOn() != null)
            discount.setApplicableOn(Discount.ApplicableOn.valueOf(request.getApplicableOn()));
        if (request.getValidFrom() != null) discount.setValidFrom(request.getValidFrom());
        if (request.getValidTo() != null) discount.setValidTo(request.getValidTo());
        if (request.getIsActive() != null) discount.setIsActive(request.getIsActive());

        discount = discountRepository.save(discount);
        return mapToResponse(discount);
    }

    @Transactional
    public void deleteDiscount(Long id, Long tenantId) {
        Discount discount = discountRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Discount not found with id: " + id));

        discount.setIsActive(false);
        discountRepository.save(discount);
    }

    private DiscountResponse mapToResponse(Discount discount) {
        return DiscountResponse.builder()
            .id(discount.getId())
            .tenantId(discount.getTenantId())
            .discountCode(discount.getDiscountCode())
            .name(discount.getName())
            .description(discount.getDescription())
            .discountType(discount.getDiscountType().name())
            .discountValue(discount.getDiscountValue())
            .minPurchaseAmount(discount.getMinPurchaseAmount())
            .maxDiscountAmount(discount.getMaxDiscountAmount())
            .applicableOn(discount.getApplicableOn().name())
            .validFrom(discount.getValidFrom())
            .validTo(discount.getValidTo())
            .isActive(discount.getIsActive())
            .createdAt(discount.getCreatedAt())
            .updatedAt(discount.getUpdatedAt())
            .build();
    }
}

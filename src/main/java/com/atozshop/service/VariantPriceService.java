package com.atozshop.service;

import com.atozshop.entity.VariantPrice;
import com.atozshop.repository.VariantPriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class VariantPriceService {

    private final VariantPriceRepository variantPriceRepository;

    @Transactional
    public void createOrUpdatePrice(Long tenantId, Long variantId, Long storeId,
                                     BigDecimal costPrice, BigDecimal sellingPrice, BigDecimal mrp) {
        LocalDate today = LocalDate.now();

        // Check if current price exists
        variantPriceRepository.findCurrentPrice(tenantId, variantId, storeId, today)
                .ifPresentOrElse(
                        existingPrice -> {
                            // Update only if prices changed
                            if (!existingPrice.getCostPrice().equals(costPrice) ||
                                !existingPrice.getSellingPrice().equals(sellingPrice)) {

                                // Close existing price
                                existingPrice.setEffectiveTo(today.minusDays(1));
                                variantPriceRepository.save(existingPrice);

                                // Create new price record
                                createPriceRecord(tenantId, variantId, storeId, costPrice, sellingPrice, mrp, today);
                            }
                        },
                        () -> {
                            // No existing price, create new
                            createPriceRecord(tenantId, variantId, storeId, costPrice, sellingPrice, mrp, today);
                        }
                );
    }

    private void createPriceRecord(Long tenantId, Long variantId, Long storeId,
                                    BigDecimal costPrice, BigDecimal sellingPrice, BigDecimal mrp,
                                    LocalDate effectiveFrom) {
        VariantPrice newPrice = VariantPrice.builder()
                .tenantId(tenantId)
                .variantId(variantId)
                .storeId(storeId)
                .costPrice(costPrice)
                .sellingPrice(sellingPrice)
                .mrp(mrp)
                .effectiveFrom(effectiveFrom)
                .build();

        variantPriceRepository.save(newPrice);
    }

    public VariantPrice getCurrentPrice(Long tenantId, Long variantId, Long storeId) {
        return variantPriceRepository.findCurrentPrice(tenantId, variantId, storeId, LocalDate.now())
                .orElse(null);
    }
}

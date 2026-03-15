package com.atozshop.service;

import com.atozshop.dto.request.IncomingStockItemRequest;
import com.atozshop.dto.request.IncomingStockRequest;
import com.atozshop.dto.response.CurrentStockResponse;
import com.atozshop.dto.response.LowStockAlertResponse;
import com.atozshop.dto.response.StockLedgerResponse;
import com.atozshop.dto.response.StockTransactionItemResponse;
import com.atozshop.dto.response.StockTransactionResponse;
import com.atozshop.entity.*;
import com.atozshop.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockTransactionRepository transactionRepository;
    private final StockTransactionItemRepository transactionItemRepository;
    private final StockLedgerRepository ledgerRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public StockTransactionResponse createIncomingStock(IncomingStockRequest request) {
        // Validate store exists
        storeRepository.findByIdAndTenantId(request.getStoreId(), request.getTenantId())
                .orElseThrow(() -> new RuntimeException("Store not found"));

        // Generate transaction number
        LocalDateTime transactionDate = request.getTransactionDate() != null ?
                request.getTransactionDate() : LocalDateTime.now();
        String transactionNumber = generateTransactionNumber(request.getTenantId(), transactionDate);

        // Calculate totals
        int totalQuantity = 0;
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal expectedRevenue = BigDecimal.ZERO;

        for (IncomingStockItemRequest item : request.getItems()) {
            // Validate variant exists
            variantRepository.findByIdAndTenantId(item.getVariantId(), request.getTenantId())
                    .orElseThrow(() -> new RuntimeException("Variant not found: " + item.getVariantId()));

            totalQuantity += item.getQuantity();
            BigDecimal itemCost = item.getCostPrice().multiply(new BigDecimal(item.getQuantity()));
            BigDecimal itemRevenue = item.getSellingPrice().multiply(new BigDecimal(item.getQuantity()));

            totalCost = totalCost.add(itemCost);
            expectedRevenue = expectedRevenue.add(itemRevenue);
        }

        BigDecimal expectedProfit = expectedRevenue.subtract(totalCost);

        // Create transaction
        StockTransaction transaction = StockTransaction.builder()
                .tenantId(request.getTenantId())
                .storeId(request.getStoreId())
                .transactionNumber(transactionNumber)
                .transactionDate(transactionDate)
                .supplierName(request.getSupplierName())
                .totalQuantity(totalQuantity)
                .totalCost(totalCost)
                .expectedRevenue(expectedRevenue)
                .expectedProfit(expectedProfit)
                .status(StockTransaction.Status.DRAFT)
                .notes(request.getNotes())
                .createdBy(request.getCreatedBy())
                .build();

        StockTransaction savedTransaction = transactionRepository.save(transaction);

        // Create transaction items
        List<StockTransactionItem> items = request.getItems().stream()
                .map(item -> {
                    BigDecimal itemCost = item.getCostPrice().multiply(new BigDecimal(item.getQuantity()));
                    BigDecimal itemRevenue = item.getSellingPrice().multiply(new BigDecimal(item.getQuantity()));
                    BigDecimal itemProfit = itemRevenue.subtract(itemCost);

                    return StockTransactionItem.builder()
                            .transactionId(savedTransaction.getId())
                            .variantId(item.getVariantId())
                            .quantity(item.getQuantity())
                            .costPrice(item.getCostPrice())
                            .sellingPrice(item.getSellingPrice())
                            .totalCost(itemCost)
                            .expectedRevenue(itemRevenue)
                            .expectedProfit(itemProfit)
                            .remarks(item.getRemarks())
                            .build();
                })
                .collect(Collectors.toList());

        transactionItemRepository.saveAll(items);

        return getTransactionById(savedTransaction.getId(), request.getTenantId());
    }

    @Transactional
    public StockTransactionResponse confirmIncomingStock(Long transactionId, Long tenantId) {
        StockTransaction transaction = transactionRepository.findByIdAndTenantId(transactionId, tenantId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (transaction.getStatus() != StockTransaction.Status.DRAFT) {
            throw new RuntimeException("Only DRAFT transactions can be confirmed");
        }

        // Get transaction items
        List<StockTransactionItem> items = transactionItemRepository.findByTransactionId(transactionId);

        // Create stock ledger entries for each item
        for (StockTransactionItem item : items) {
            recordStockMovement(
                    tenantId,
                    transaction.getStoreId(),
                    item.getVariantId(),
                    StockLedger.TransactionType.INCOMING,
                    transactionId,
                    item.getQuantity(),
                    item.getCostPrice(),
                    item.getSellingPrice(),
                    item.getRemarks(),
                    transaction.getTransactionDate(),
                    transaction.getCreatedBy()
            );
        }

        // Update transaction status
        transaction.setStatus(StockTransaction.Status.CONFIRMED);
        transactionRepository.save(transaction);

        return getTransactionById(transactionId, tenantId);
    }

    @Transactional
    public StockTransactionResponse cancelTransaction(Long transactionId, Long tenantId) {
        StockTransaction transaction = transactionRepository.findByIdAndTenantId(transactionId, tenantId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (transaction.getStatus() == StockTransaction.Status.CONFIRMED) {
            throw new RuntimeException("Cannot cancel confirmed transactions");
        }

        transaction.setStatus(StockTransaction.Status.CANCELLED);
        transactionRepository.save(transaction);

        return getTransactionById(transactionId, tenantId);
    }

    @Transactional
    public void recordStockMovement(Long tenantId, Long storeId, Long variantId,
                                     StockLedger.TransactionType transactionType,
                                     Long transactionId, Integer quantityChange,
                                     BigDecimal costPrice, BigDecimal sellingPrice,
                                     String remarks, LocalDateTime transactionDate,
                                     Long createdBy) {
        // Get current stock balance
        Integer currentBalance = ledgerRepository.getCurrentStock(variantId, storeId, tenantId);
        if (currentBalance == null) {
            currentBalance = 0;
        }

        // Calculate new balance
        Integer newBalance = currentBalance + quantityChange;

        // Create ledger entry
        StockLedger ledgerEntry = StockLedger.builder()
                .tenantId(tenantId)
                .storeId(storeId)
                .variantId(variantId)
                .transactionType(transactionType)
                .transactionId(transactionId)
                .quantityChange(quantityChange)
                .balanceAfter(newBalance)
                .costPriceSnapshot(costPrice)
                .sellingPriceSnapshot(sellingPrice)
                .remarks(remarks)
                .transactionDate(transactionDate)
                .createdBy(createdBy)
                .build();

        ledgerRepository.save(ledgerEntry);
    }

    public StockTransactionResponse getTransactionById(Long id, Long tenantId) {
        StockTransaction transaction = transactionRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        String storeName = storeRepository.findById(transaction.getStoreId())
                .map(Store::getName)
                .orElse(null);

        List<StockTransactionItem> items = transactionItemRepository.findByTransactionId(id);

        List<StockTransactionItemResponse> itemResponses = items.stream()
                .map(item -> {
                    ProductVariant variant = variantRepository.findById(item.getVariantId()).orElse(null);
                    String variantName = variant != null ? variant.getVariantName() : null;
                    String sku = variant != null ? variant.getSku() : null;

                    return StockTransactionItemResponse.builder()
                            .id(item.getId())
                            .variantId(item.getVariantId())
                            .variantName(variantName)
                            .sku(sku)
                            .quantity(item.getQuantity())
                            .costPrice(item.getCostPrice())
                            .sellingPrice(item.getSellingPrice())
                            .totalCost(item.getTotalCost())
                            .expectedRevenue(item.getExpectedRevenue())
                            .expectedProfit(item.getExpectedProfit())
                            .remarks(item.getRemarks())
                            .build();
                })
                .collect(Collectors.toList());

        return StockTransactionResponse.builder()
                .id(transaction.getId())
                .tenantId(transaction.getTenantId())
                .storeId(transaction.getStoreId())
                .storeName(storeName)
                .transactionNumber(transaction.getTransactionNumber())
                .transactionDate(transaction.getTransactionDate())
                .supplierName(transaction.getSupplierName())
                .totalQuantity(transaction.getTotalQuantity())
                .totalCost(transaction.getTotalCost())
                .expectedRevenue(transaction.getExpectedRevenue())
                .expectedProfit(transaction.getExpectedProfit())
                .status(transaction.getStatus().name())
                .notes(transaction.getNotes())
                .createdBy(transaction.getCreatedBy())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .items(itemResponses)
                .build();
    }

    public Page<StockTransactionResponse> getAllTransactions(Long tenantId, Pageable pageable) {
        return transactionRepository.findByTenantIdOrderByTransactionDateDesc(tenantId, pageable)
                .map(transaction -> getTransactionById(transaction.getId(), tenantId));
    }

    public List<StockLedgerResponse> getVariantLedger(Long variantId, Long storeId, Long tenantId) {
        List<StockLedger> ledgerEntries = ledgerRepository
                .findByVariantIdAndStoreIdAndTenantIdOrderByTransactionDateDesc(variantId, storeId, tenantId);

        return ledgerEntries.stream()
                .map(entry -> {
                    ProductVariant variant = variantRepository.findById(entry.getVariantId()).orElse(null);
                    String variantName = variant != null ? variant.getVariantName() : null;

                    String transactionNumber = null;
                    if (entry.getTransactionId() != null) {
                        transactionNumber = transactionRepository.findById(entry.getTransactionId())
                                .map(StockTransaction::getTransactionNumber)
                                .orElse(null);
                    }

                    return StockLedgerResponse.builder()
                            .id(entry.getId())
                            .tenantId(entry.getTenantId())
                            .storeId(entry.getStoreId())
                            .variantId(entry.getVariantId())
                            .variantName(variantName)
                            .transactionType(entry.getTransactionType().name())
                            .transactionId(entry.getTransactionId())
                            .transactionNumber(transactionNumber)
                            .quantityChange(entry.getQuantityChange())
                            .balanceAfter(entry.getBalanceAfter())
                            .costPriceSnapshot(entry.getCostPriceSnapshot())
                            .sellingPriceSnapshot(entry.getSellingPriceSnapshot())
                            .remarks(entry.getRemarks())
                            .transactionDate(entry.getTransactionDate())
                            .createdBy(entry.getCreatedBy())
                            .createdAt(entry.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    public List<CurrentStockResponse> getCurrentStockLevels(Long tenantId, Long storeId) {
        List<Map<String, Object>> stockBalances = ledgerRepository.getAllStockBalances(tenantId, storeId);

        return stockBalances.stream()
                .map(balance -> {
                    Long variantId = ((Number) balance.get("variantId")).longValue();
                    Integer currentStock = ((Number) balance.get("currentStock")).intValue();

                    ProductVariant variant = variantRepository.findById(variantId).orElse(null);
                    if (variant == null) {
                        return null;
                    }

                    String productName = productRepository.findById(variant.getProductId())
                            .map(Product::getName)
                            .orElse(null);

                    String stockStatus;
                    if (currentStock == 0) {
                        stockStatus = "OUT_OF_STOCK";
                    } else if (currentStock <= variant.getMinStockThreshold()) {
                        stockStatus = "LOW";
                    } else if (variant.getMaxStockThreshold() != null && currentStock >= variant.getMaxStockThreshold()) {
                        stockStatus = "OVERSTOCK";
                    } else {
                        stockStatus = "NORMAL";
                    }

                    return CurrentStockResponse.builder()
                            .variantId(variantId)
                            .sku(variant.getSku())
                            .variantName(variant.getVariantName())
                            .productName(productName)
                            .currentStock(currentStock)
                            .minStockThreshold(variant.getMinStockThreshold())
                            .maxStockThreshold(variant.getMaxStockThreshold())
                            .stockStatus(stockStatus)
                            .build();
                })
                .filter(response -> response != null)
                .collect(Collectors.toList());
    }

    public Page<StockLedgerResponse> getStockLedger(Long tenantId, Long storeId, Pageable pageable) {
        Page<StockLedger> ledgerPage = ledgerRepository
                .findByTenantIdAndStoreIdOrderByTransactionDateDesc(tenantId, storeId, pageable);

        List<StockLedgerResponse> responses = ledgerPage.getContent().stream()
                .map(entry -> {
                    ProductVariant variant = variantRepository.findById(entry.getVariantId()).orElse(null);
                    String variantName = variant != null ? variant.getVariantName() : null;

                    String transactionNumber = null;
                    if (entry.getTransactionId() != null) {
                        transactionNumber = transactionRepository.findById(entry.getTransactionId())
                                .map(StockTransaction::getTransactionNumber)
                                .orElse(null);
                    }

                    return StockLedgerResponse.builder()
                            .id(entry.getId())
                            .tenantId(entry.getTenantId())
                            .storeId(entry.getStoreId())
                            .variantId(entry.getVariantId())
                            .variantName(variantName)
                            .transactionType(entry.getTransactionType().name())
                            .transactionId(entry.getTransactionId())
                            .transactionNumber(transactionNumber)
                            .quantityChange(entry.getQuantityChange())
                            .balanceAfter(entry.getBalanceAfter())
                            .costPriceSnapshot(entry.getCostPriceSnapshot())
                            .sellingPriceSnapshot(entry.getSellingPriceSnapshot())
                            .remarks(entry.getRemarks())
                            .transactionDate(entry.getTransactionDate())
                            .createdBy(entry.getCreatedBy())
                            .createdAt(entry.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, ledgerPage.getTotalElements());
    }

    public List<LowStockAlertResponse> getLowStockAlerts(Long tenantId, Long storeId) {
        List<Map<String, Object>> stockBalances = ledgerRepository.getAllStockBalances(tenantId, storeId);

        return stockBalances.stream()
                .map(balance -> {
                    Long variantId = ((Number) balance.get("variantId")).longValue();
                    Integer currentStock = ((Number) balance.get("currentStock")).intValue();

                    ProductVariant variant = variantRepository.findById(variantId).orElse(null);
                    if (variant == null) {
                        return null;
                    }

                    // Only include items below minimum threshold
                    if (currentStock > variant.getMinStockThreshold()) {
                        return null;
                    }

                    String productName = productRepository.findById(variant.getProductId())
                            .map(Product::getName)
                            .orElse(null);

                    return LowStockAlertResponse.builder()
                            .variantId(variantId)
                            .sku(variant.getSku())
                            .variantName(variant.getVariantName())
                            .productName(productName)
                            .currentStock(currentStock)
                            .minStockThreshold(variant.getMinStockThreshold())
                            .shortfall(variant.getMinStockThreshold() - currentStock)
                            .build();
                })
                .filter(response -> response != null)
                .collect(Collectors.toList());
    }

    private String generateTransactionNumber(Long tenantId, LocalDateTime transactionDate) {
        LocalDate date = transactionDate.toLocalDate();
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        Integer maxSequence = transactionRepository.findMaxSequenceForDate(tenantId, startOfDay, endOfDay);
        int nextSequence = (maxSequence != null ? maxSequence : 0) + 1;

        return String.format("ST-%s-%03d", dateStr, nextSequence);
    }
}

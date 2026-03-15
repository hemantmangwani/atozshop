# API Fixes Required - Action Plan

**Date:** March 3, 2026
**Current Status:** 36% APIs working, 64% broken
**Target:** 100% APIs working

---

## 🔴 CRITICAL FIXES (Must Do First)

### Fix 1: Extract tenantId and storeId from JWT automatically
**Impact:** Affects ALL endpoints
**Current Issue:** Every endpoint requires manual `tenantId` parameter
**Solution:** Create `@CurrentUser` annotation to auto-extract from JWT

```java
// Create CurrentUser annotation
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {}

// Create UserPrincipal class to hold user context
public class UserPrincipal {
    private Long userId;
    private Long tenantId;
    private Long storeId;
    private String email;
    private List<String> roles;
}

// Update controllers to use @CurrentUser
@GetMapping
public List<Category> getCategories(@CurrentUser UserPrincipal user) {
    return categoryService.getAllByTenant(user.getTenantId());
}
```

**Files to Modify:**
- Create: `CurrentUser.java` annotation
- Create: `UserPrincipal.java`
- Create: `CurrentUserArgumentResolver.java`
- Update: `WebMvcConfig.java` to register resolver
- Update: ALL controllers to use `@CurrentUser`

**Estimated Time:** 2 hours

---

### Fix 2: Implement Missing Stock APIs (3 endpoints)
**Impact:** Phase 1 - Inventory Management
**Current Status:** All failing with 500 errors

#### 2.1 GET `/api/v1/stock/current`
**Expected:** Return current stock levels for all products
```java
@GetMapping("/current")
public List<CurrentStockResponse> getCurrentStock(@CurrentUser UserPrincipal user) {
    return stockService.getCurrentStock(user.getTenantId());
}
```

#### 2.2 GET `/api/v1/stock/ledger`
**Expected:** Return stock movement history
```java
@GetMapping("/ledger")
public Page<StockLedgerResponse> getStockLedger(
    @CurrentUser UserPrincipal user,
    Pageable pageable) {
    return stockService.getStockLedger(user.getTenantId(), pageable);
}
```

#### 2.3 GET `/api/v1/stock/low-stock`
**Expected:** Return products below reorder level
```java
@GetMapping("/low-stock")
public List<LowStockAlertResponse> getLowStockAlerts(@CurrentUser UserPrincipal user) {
    return stockService.getLowStockAlerts(user.getTenantId());
}
```

**Files to Check/Fix:**
- `StockController.java`
- `StockService.java`
- `StockLedgerRepository.java`

**Estimated Time:** 1.5 hours

---

### Fix 3: Fix Bills APIs (3 endpoints)
**Impact:** Phase 2 - POS System
**Current Issue:** Missing `storeId` parameter

#### 3.1 GET `/api/v1/bills`
**Fix:** Extract storeId from UserPrincipal
```java
@GetMapping
public Page<BillResponse> getAllBills(
    @CurrentUser UserPrincipal user,
    Pageable pageable) {
    return billService.getAllBills(user.getTenantId(), user.getStoreId(), pageable);
}
```

#### 3.2 GET `/api/v1/bills/summary`
```java
@GetMapping("/summary")
public BillSummaryResponse getBillsSummary(@CurrentUser UserPrincipal user) {
    return billService.getSummary(user.getTenantId(), user.getStoreId());
}
```

#### 3.3 GET `/api/v1/bills/date-range`
```java
@GetMapping("/date-range")
public List<BillResponse> getBillsByDateRange(
    @CurrentUser UserPrincipal user,
    @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate startDate,
    @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate endDate) {
    return billService.getByDateRange(user.getTenantId(), user.getStoreId(), startDate, endDate);
}
```

**Files to Fix:**
- `BillController.java`

**Estimated Time:** 30 minutes

---

### Fix 4: Implement Order APIs (3 endpoints)
**Impact:** Phase 3 - E-commerce (CRITICAL - 0% working)
**Current Status:** All failing

#### 4.1 GET `/api/v1/orders`
**Expected:** Customer's own orders
```java
@GetMapping
public Page<OrderResponse> getMyOrders(
    @CurrentUser UserPrincipal user,
    Pageable pageable) {
    return orderService.getCustomerOrders(user.getCustomerId(), pageable);
}
```

#### 4.2 GET `/api/v1/admin/orders`
**Expected:** All orders (admin only)
```java
@GetMapping
public Page<OrderResponse> getAllOrders(
    @CurrentUser UserPrincipal user,
    Pageable pageable) {
    return orderService.getAllOrders(user.getTenantId(), pageable);
}
```

#### 4.3 GET `/api/v1/admin/orders/status/{status}`
**Expected:** Orders by status
```java
@GetMapping("/status/{status}")
public Page<OrderResponse> getOrdersByStatus(
    @CurrentUser UserPrincipal user,
    @PathVariable OrderStatus status,
    Pageable pageable) {
    return orderService.getByStatus(user.getTenantId(), status, pageable);
}
```

**Files to Check/Create:**
- `OrderController.java` - May not exist
- `AdminOrderController.java` - May not exist
- `OrderService.java` - Check implementation
- `OrderRepository.java` - Add custom queries if needed

**Estimated Time:** 2 hours

---

### Fix 5: Fix Sales Report APIs (4 endpoints)
**Impact:** Phase 2 - Reporting
**Current Status:** All failing with 500 errors

#### 5.1 POST `/api/v1/sales/daily-report`
**Fix:** Check service implementation
```java
@PostMapping("/daily-report")
public DailySalesReportResponse getDailyReport(
    @CurrentUser UserPrincipal user,
    @RequestBody @Valid SalesReportRequest request) {
    return salesReportService.getDailyReport(
        user.getTenantId(),
        request.getStoreId() != null ? request.getStoreId() : user.getStoreId(),
        request.getReportDate()
    );
}
```

#### 5.2-5.4 Similar fixes for other report endpoints

**Files to Fix:**
- `SalesReportController.java`
- `SalesReportService.java` - Check implementations
- Verify database queries work

**Estimated Time:** 2 hours

---

### Fix 6: Fix Category Root Endpoint
**Impact:** Phase 1 - Categories
**Current Issue:** Route conflict `/categories/root` vs `/categories/{id}`

**Solution A - Recommended:**
```java
// Change to use query parameter
@GetMapping
public List<CategoryResponse> getCategories(
    @CurrentUser UserPrincipal user,
    @RequestParam(required = false) Boolean rootOnly) {
    if (Boolean.TRUE.equals(rootOnly)) {
        return categoryService.getRootCategories(user.getTenantId());
    }
    return categoryService.getAllByTenant(user.getTenantId());
}
```

**Solution B - Alternative:**
```java
// Use different path
@GetMapping("/hierarchy/root")
public List<CategoryResponse> getRootCategories(@CurrentUser UserPrincipal user) {
    return categoryService.getRootCategories(user.getTenantId());
}
```

**Files to Fix:**
- `CategoryController.java`

**Estimated Time:** 15 minutes

---

### Fix 7: Fix Public Products Endpoint
**Impact:** E-commerce public catalog
**Current Status:** 500 error

**Files to Check:**
- `PublicProductController.java`
- Verify it doesn't require authentication
- Check service implementation

**Estimated Time:** 30 minutes

---

### Fix 8: Fix Payment Summary API
**Impact:** Phase 2 - Payments
**Current Status:** 500 error

```java
@GetMapping("/summary")
public PaymentSummaryResponse getPaymentsSummary(
    @CurrentUser UserPrincipal user,
    @RequestParam(required = false) LocalDate startDate,
    @RequestParam(required = false) LocalDate endDate) {
    return paymentService.getSummary(user.getTenantId(), user.getStoreId(), startDate, endDate);
}
```

**Files to Fix:**
- `PaymentController.java`
- `PaymentService.java`

**Estimated Time:** 30 minutes

---

### Fix 9: Fix Product Variants API
**Impact:** Phase 1 - Product management
**Current Status:** Missing tenantId parameter

```java
@GetMapping
public List<VariantResponse> getAllVariants(@CurrentUser UserPrincipal user) {
    return variantService.getAllByTenant(user.getTenantId());
}
```

**Files to Fix:**
- `ProductVariantController.java`

**Estimated Time:** 15 minutes

---

## 📋 IMPLEMENTATION ORDER

### Phase 1: Foundation (2 hours)
1. ✅ Create `@CurrentUser` annotation system
2. ✅ Create `UserPrincipal` class
3. ✅ Create argument resolver
4. ✅ Register in WebMvcConfig

### Phase 2: Quick Wins (1.5 hours)
5. ✅ Fix Categories root endpoint (15 min)
6. ✅ Fix Product Variants API (15 min)
7. ✅ Fix Bills APIs (30 min)
8. ✅ Fix Payment Summary (30 min)

### Phase 3: Core Features (4 hours)
9. ✅ Fix Stock Management APIs (1.5 hours)
10. ✅ Implement Order APIs (2 hours)
11. ✅ Fix Public Products (30 min)

### Phase 4: Reporting (2 hours)
12. ✅ Fix all Sales Report APIs (2 hours)

**Total Estimated Time: 9.5 hours**

---

## 🧪 TESTING CHECKLIST

After each fix:
- [ ] Test endpoint with curl
- [ ] Verify response structure
- [ ] Check error handling
- [ ] Update Postman collection
- [ ] Document in Swagger

Final verification:
- [ ] Run complete API test suite
- [ ] Verify 100% endpoints working
- [ ] Test from React frontend
- [ ] Create end-to-end flow test

---

## 📝 NOTES

### Important Considerations:
1. **UserPrincipal needs storeId** - User table may not have default storeId
2. **Multiple stores per tenant** - User may need to select store
3. **Customer orders** - User needs customerId field
4. **Error handling** - Add proper exception handlers
5. **Pagination** - Ensure all list endpoints support pagination

### Database Changes Needed:
- Users table might need `default_store_id` column
- Or implement store selection in frontend

---

## 🎯 SUCCESS CRITERIA

✅ All 25+ endpoints return 200/201
✅ No 500 errors
✅ No missing parameter errors
✅ All responses match expected structure
✅ Frontend can call all APIs successfully
✅ Complete e-commerce flow works end-to-end

---

**Ready to start fixing?** Let me know which approach you prefer:

**Option A:** Fix everything systematically (9.5 hours, 100% complete)
**Option B:** Create minimal working flow first (3 hours, core features only)
**Option C:** Focus on specific phase (your choice)

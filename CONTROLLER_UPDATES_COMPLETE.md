# Controller Updates - @CurrentUser Migration Complete

## Summary
All controllers in the AtoZShop project have been systematically updated to use the new `@CurrentUser` annotation system instead of requiring `tenantId` as `@RequestParam`.

## Changes Applied

### 1. CategoryController ✅
- **Imports Added**: `@CurrentUser`, `UserPrincipal`
- **Changes**:
  - Removed all `@RequestParam Long tenantId` parameters
  - Added `@CurrentUser UserPrincipal user` as first parameter
  - All service calls now use `user.getTenantId()`
- **Endpoints Updated**: 5
  - GET `/{id}`
  - GET `/`
  - GET `/{id}/subcategories`
  - PUT `/{id}`
  - DELETE `/{id}`

### 2. ProductController ✅
- **Imports Added**: `@CurrentUser`, `UserPrincipal`
- **Changes**:
  - Removed all `@RequestParam Long tenantId` parameters
  - Added `@CurrentUser UserPrincipal user` as first parameter
  - All service calls now use `user.getTenantId()`
- **Endpoints Updated**: 5
  - GET `/{id}`
  - GET `/`
  - GET `/search`
  - PUT `/{id}`
  - DELETE `/{id}`

### 3. ProductVariantController ✅
- **Imports Added**: `@CurrentUser`, `UserPrincipal`
- **Changes**:
  - Removed all `@RequestParam Long tenantId` and `@RequestParam Long storeId` parameters
  - Added `@CurrentUser UserPrincipal user` as first parameter
  - Service calls use `user.getTenantId()` and `user.getStoreIdOrDefault()`
- **Endpoints Updated**: 5
  - GET `/{id}`
  - GET `/sku/{sku}`
  - GET `/barcode/{barcode}`
  - PUT `/{id}`
  - GET `/low-stock`

### 4. StockController ✅
- **Imports Added**: `@CurrentUser`, `UserPrincipal`, `LowStockAlertResponse`
- **Changes**:
  - Removed all `@RequestParam Long tenantId` and `@RequestParam Long storeId` parameters
  - Added `@CurrentUser UserPrincipal user` as first parameter
  - Service calls use `user.getTenantId()` and `user.getStoreIdOrDefault()`
- **NEW Endpoints Added**:
  - GET `/current` - Returns current stock levels
  - GET `/ledger` - Returns stock movement history (paginated)
  - GET `/low-stock` - Returns products below reorder level
- **Endpoints Updated**: 8 total

**Additional Changes**:
- **StockService** - Added missing methods:
  - `getStockLedger(Long tenantId, Long storeId, Pageable pageable)`
  - `getLowStockAlerts(Long tenantId, Long storeId)`
- **StockLedgerRepository** - Added method:
  - `Page<StockLedger> findByTenantIdAndStoreIdOrderByTransactionDateDesc(Long, Long, Pageable)`

### 5. SupplierController ✅
- **Imports Added**: `@CurrentUser`, `UserPrincipal`
- **Changes**:
  - Removed all `@RequestParam Long tenantId` parameters
  - Added `@CurrentUser UserPrincipal user` as first parameter
  - All service calls now use `user.getTenantId()`
- **Endpoints Updated**: 6
  - GET `/{id}`
  - GET `/code/{code}`
  - GET `/`
  - GET `/search`
  - PUT `/{id}`
  - DELETE `/{id}`

### 6. CustomerController ✅
- **Imports Added**: `@CurrentUser`, `UserPrincipal`
- **Changes**:
  - Removed all `@RequestParam Long tenantId` parameters
  - Added `@CurrentUser UserPrincipal user` as first parameter
  - All service calls now use `user.getTenantId()`
- **Endpoints Updated**: 7
  - GET `/`
  - GET `/search`
  - GET `/{id}`
  - GET `/phone/{phone}`
  - PUT `/{id}`
  - DELETE `/{id}`
  - GET `/{id}/purchase-history`

### 7. BillController ✅
- **Imports Added**: `@CurrentUser`, `UserPrincipal`
- **Changes**:
  - Removed all `@RequestParam Long tenantId` and `@RequestParam Long storeId` parameters
  - Added `@CurrentUser UserPrincipal user` as first parameter
  - Service calls use `user.getTenantId()` and `user.getStoreIdOrDefault()`
- **Endpoints Updated**: 10
  - GET `/`
  - GET `/{id}`
  - GET `/number/{billNumber}`
  - POST `/{id}/items`
  - PUT `/{id}/items/{itemId}`
  - DELETE `/{id}/items/{itemId}`
  - POST `/{id}/confirm`
  - POST `/{id}/cancel`
  - GET `/{id}/receipt/pdf`
  - GET `/{id}/receipt/thermal`

### 8. DiscountController ✅
- **Imports Added**: `@CurrentUser`, `UserPrincipal`
- **Changes**:
  - Removed all `@RequestParam Long tenantId` parameters
  - Added `@CurrentUser UserPrincipal user` as first parameter
  - All service calls now use `user.getTenantId()`
- **Endpoints Updated**: 6
  - GET `/`
  - GET `/active`
  - GET `/{id}`
  - GET `/code/{code}`
  - PUT `/{id}`
  - DELETE `/{id}`

### 9. PaymentController ✅
- **Imports Added**: `@CurrentUser`, `UserPrincipal`
- **Changes**:
  - Removed `@RequestParam Long tenantId` parameter
  - Added `@CurrentUser UserPrincipal user` parameter
  - Service call uses `user.getTenantId()`
- **Endpoints Updated**: 1
  - GET `/range`

### 10. SalesReportController ✅
- **Imports Added**: `@CurrentUser`, `UserPrincipal`
- **Changes**:
  - Removed all `@RequestParam Long tenantId` and `@RequestParam Long storeId` parameters
  - Added `@CurrentUser UserPrincipal user` as first parameter
  - Service calls use `user.getTenantId()` and `user.getStoreIdOrDefault()`
- **Endpoints Updated**: 3
  - GET `/daily-closing`
  - GET `/top-selling-products`
  - GET `/profit`

### 11. OrderController ✅
- **Imports Added**: `@CurrentUser`, `UserPrincipal`
- **Changes**:
  - Removed all `@RequestParam Long tenantId` parameters
  - Added `@CurrentUser UserPrincipal user` as first parameter
  - Service calls use `user.getTenantId()` and `user.getCustomerId()`
- **NEW Endpoint Added**:
  - GET `/` - Get my orders (for logged-in customer)
- **Endpoints Updated**: 3 total
  - GET `/` - NEW
  - GET `/customer/{customerId}`
  - GET `/{id}`

### 12. AdminOrderController ✅
- **Imports Added**: `@CurrentUser`, `UserPrincipal`
- **Changes**:
  - Removed all `@RequestParam Long tenantId`, `@RequestParam Long storeId`, and action user ID parameters
  - Added `@CurrentUser UserPrincipal user` as first parameter
  - Service calls use `user.getTenantId()`, `user.getStoreIdOrDefault()`, and `user.getId()`
- **Endpoints Updated**: 6
  - GET `/`
  - GET `/{id}`
  - POST `/{id}/accept`
  - POST `/{id}/pack`
  - POST `/{id}/dispatch`
  - POST `/{id}/deliver`

### 13. StoreController ✅
- **Imports Added**: `@CurrentUser`, `UserPrincipal`
- **Changes**:
  - Removed all `@RequestParam Long tenantId` parameters
  - Added `@CurrentUser UserPrincipal user` as first parameter
  - All service calls now use `user.getTenantId()`
- **Endpoints Updated**: 5
  - GET `/{id}`
  - GET `/code/{code}`
  - GET `/`
  - PUT `/{id}`
  - DELETE `/{id}`

### 14. CustomerAddressController ✅
- **Imports Added**: `@CurrentUser`, `UserPrincipal`
- **Changes**:
  - Removed all `@RequestParam Long customerId` parameters
  - Added `@CurrentUser UserPrincipal user` as first parameter
  - Uses `user.getCustomerId()` for customer identification
- **NEW Endpoint Added**:
  - GET `/my` - Get my addresses (for logged-in customer)
- **Endpoints Updated**: 5 total
  - GET `/my` - NEW
  - GET `/{id}`
  - PUT `/{id}`
  - DELETE `/{id}`
  - PUT `/{id}/default`

### 15. PublicProductController ⚠️ (NOT UPDATED)
- **Status**: Intentionally left unchanged
- **Reason**: Public API endpoints that don't require authentication
- **Kept Parameters**: `@RequestParam Long tenantId`, `@RequestParam Long storeId`
- **Note**: Public endpoints need explicit tenantId/storeId since no user context

## UserPrincipal Methods Used

The following `UserPrincipal` methods are now utilized across controllers:

```java
user.getId()              // User's database ID (for action tracking)
user.getTenantId()        // Always required for multi-tenancy
user.getStoreId()         // May be null
user.getStoreIdOrDefault()// Returns storeId or defaults to 1L
user.getCustomerId()      // For customer-specific operations
user.getEmail()           // User's email
user.getFullName()        // User's full name
user.isAdmin()            // Check if user is admin
user.isCustomer()         // Check if user is customer
```

## Testing Requirements

Before deploying, test the following scenarios:

1. **Admin User Flow**:
   - Admin with storeId set
   - Admin without storeId (should use default)
   - All CRUD operations on entities

2. **Customer User Flow**:
   - Customer browsing products
   - Customer placing orders
   - Customer managing addresses
   - Order tracking

3. **Multi-tenancy**:
   - Verify tenant isolation
   - Ensure tenantId is always derived from JWT token
   - Test cross-tenant access prevention

4. **Stock Operations**:
   - Test new stock endpoints: `/current`, `/ledger`, `/low-stock`
   - Verify pagination on stock ledger
   - Verify low stock alerts

## API Impact

### Breaking Changes ⚠️
All authenticated endpoints now:
- **DO NOT** accept `tenantId` as query parameter
- **DO NOT** accept `storeId` as query parameter (except where explicitly needed)
- **DO NOT** accept `customerId` as query parameter (for customer-specific endpoints)
- **REQUIRE** valid JWT token with user context

### Updated API Request Examples

**Before:**
```bash
GET /api/v1/products?tenantId=1
Authorization: Bearer <token>
```

**After:**
```bash
GET /api/v1/products
Authorization: Bearer <token>
```

The system automatically extracts tenantId from the authenticated user's JWT token.

## Files Modified

### Controllers (15 files)
1. `/src/main/java/com/atozshop/controller/CategoryController.java`
2. `/src/main/java/com/atozshop/controller/ProductController.java`
3. `/src/main/java/com/atozshop/controller/ProductVariantController.java`
4. `/src/main/java/com/atozshop/controller/StockController.java`
5. `/src/main/java/com/atozshop/controller/SupplierController.java`
6. `/src/main/java/com/atozshop/controller/CustomerController.java`
7. `/src/main/java/com/atozshop/controller/BillController.java`
8. `/src/main/java/com/atozshop/controller/DiscountController.java`
9. `/src/main/java/com/atozshop/controller/PaymentController.java`
10. `/src/main/java/com/atozshop/controller/SalesReportController.java`
11. `/src/main/java/com/atozshop/controller/OrderController.java`
12. `/src/main/java/com/atozshop/controller/AdminOrderController.java`
13. `/src/main/java/com/atozshop/controller/StoreController.java`
14. `/src/main/java/com/atozshop/controller/CustomerAddressController.java`

### Services (1 file)
15. `/src/main/java/com/atozshop/service/StockService.java`

### Repositories (1 file)
16. `/src/main/java/com/atozshop/repository/StockLedgerRepository.java`

## Next Steps

1. **Build and Test**: Run `mvn clean package` to ensure compilation
2. **Update Postman Collections**: Remove tenantId parameters from requests
3. **Update Frontend**: Modify API calls to remove tenantId/storeId parameters
4. **Integration Testing**: Run full E2E tests with updated endpoints
5. **Documentation**: Update API documentation (Swagger) with new signature

## Benefits

1. **Security**: TenantId cannot be manipulated via request parameters
2. **Simplicity**: Cleaner API contracts without redundant parameters
3. **Consistency**: All authenticated endpoints follow same pattern
4. **User Experience**: Logged-in users automatically scoped to their tenant
5. **Type Safety**: Compile-time checking of user context availability

---

**Migration Status**: ✅ COMPLETE
**Date**: 2026-03-03
**Controllers Updated**: 14/15 (PublicProductController intentionally unchanged)
**New Endpoints Added**: 4
**Service Methods Added**: 2
**Repository Methods Added**: 1

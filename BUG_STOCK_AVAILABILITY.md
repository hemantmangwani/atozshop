# CRITICAL BUG: Stock Availability Always Shows 0

**Status:** 🔴 CRITICAL BUG FOUND
**Impact:** Add to Cart functionality broken
**Date Discovered:** March 2, 2026

---

## Symptoms

When users try to add products to cart:
- Frontend shows "Only 0 items available in stock"
- Products show as "Out of Stock" even though stock exists in database
- Cart Context throws error: "Only 0 items available in stock"
- Cannot add any products to cart

## Root Cause Analysis

### Bug Location
**File:** `src/main/java/com/atozshop/controller/PublicProductController.java`
**Method:** `checkAvailability()` (lines 114-125)
**Line:** 117

### The Bug

```java
// LINE 114-122 (CURRENT - INCORRECT)
Integer currentStock = stockLedgerRepository.getCurrentStock(variantId, storeId, tenantId);
if (currentStock == null) currentStock = 0;

Integer reservedStock = reservationService.getAvailableStock(variantId, storeId, tenantId);  // ❌ BUG!
Integer soldStock = currentStock - reservedStock;

StockAvailabilityResponse response = StockAvailabilityResponse.from(
        variantId, variant.getSku(), currentStock, soldStock, reservedStock
);
```

### What's Wrong

**Line 117:**
```java
Integer reservedStock = reservationService.getAvailableStock(variantId, storeId, tenantId);
```

**Problem:**
- `reservationService.getAvailableStock()` returns `currentStock - reservedStock` (available units)
- This value is then assigned to variable named `reservedStock`
- But it's NOT the reserved stock - it's the AVAILABLE stock!

**Example with real data:**
- Current stock in ledger: 50 units
- Actual reserved stock: 0 units (no active reservations)
- `getAvailableStock()` returns: 50 - 0 = 50
- Line 117 assigns: `reservedStock = 50` ❌
- Line 118 calculates: `soldStock = 50 - 50 = 0`
- Line 120-122 passes to response: `totalStock=50, soldStock=0, reservedStock=50`
- Response calculates: `available = 50 - 0 - 50 = 0` ❌

### The Correct Logic Should Be

```java
// CORRECT VERSION
Integer currentStock = stockLedgerRepository.getCurrentStock(variantId, storeId, tenantId);
if (currentStock == null) currentStock = 0;

// Get ACTUAL reserved stock from repository
Integer reservedStock = reservationRepository.getTotalReservedStock(variantId, storeId, tenantId);  // ✅ CORRECT
if (reservedStock == null) reservedStock = 0;

// soldStock is NOT used in calculation, but for info only
Integer soldStock = 0;  // Or calculate from sales transactions if needed

StockAvailabilityResponse response = StockAvailabilityResponse.from(
        variantId, variant.getSku(), currentStock, soldStock, reservedStock
);
```

**With correct code:**
- Current stock: 50
- Reserved stock from DB: 0
- Response: `totalStock=50, soldStock=0, reservedStock=0`
- Available: `50 - 0 - 0 = 50` ✅

---

## Impact Assessment

### Affected Features
1. ✅ **Add to Cart** - BROKEN
   - CartContext calls `checkStockAvailability()`
   - Gets `availableStock: 0`
   - Throws error: "Only 0 items available in stock"

2. ✅ **Product Listing** - WORKS
   - Uses different code path (`buildPublicProductResponse`)
   - Calls `reservationService.getAvailableStock()` directly
   - Works correctly

3. ✅ **Stock Display on Products** - WORKS
   - Same as product listing
   - Shows "50 units available"

4. ❌ **Checkout/Order** - BROKEN
   - Cannot add to cart, so cannot checkout

### Why Product Listing Works But Cart Doesn't

**Product Listing** (PublicProductController line 145):
```java
Integer availableStock = reservationService.getAvailableStock(...);  // ✅ Correct usage
```
- Calls method correctly
- Assigns to correctly named variable
- Shows "50 units available"

**Stock Availability API** (PublicProductController line 117):
```java
Integer reservedStock = reservationService.getAvailableStock(...);  // ❌ Wrong usage
```
- Calls wrong method
- Assigns to wrong variable name
- Returns 0 available

---

## Database Verification

### Stock Ledger (Correct ✅)
```sql
SELECT variant_id, store_id, SUM(quantity_change) as stock
FROM stock_ledger
WHERE variant_id = 2 AND store_id = 1 AND tenant_id = 1;
```
Result: **50 units**

### Stock Reservations (Correct ✅)
```sql
SELECT COALESCE(SUM(reserved_quantity), 0)
FROM stock_reservations
WHERE variant_id = 2 AND store_id = 1 AND tenant_id = 1 AND status = 'ACTIVE';
```
Result: **0 units** (no active reservations)

### Calculation Should Be
- Available = 50 (current) - 0 (reserved) = **50 units** ✅

---

## Fixing the Bug

### Option 1: Fix PublicProductController (RECOMMENDED)

**File:** `src/main/java/com/atozshop/controller/PublicProductController.java`

**Change line 117 from:**
```java
Integer reservedStock = reservationService.getAvailableStock(variantId, storeId, tenantId);
```

**To:**
```java
Integer reservedStock = reservationRepository.getTotalReservedStock(variantId, storeId, tenantId);
if (reservedStock == null) reservedStock = 0;
```

**Also update line 118:**
```java
Integer soldStock = 0;  // Or implement proper sold stock calculation if needed
```

### Option 2: Add New Method to StockReservationService

**File:** `src/main/java/com/atozshop/service/StockReservationService.java`

Add method:
```java
public Integer getReservedStock(Long variantId, Long storeId, Long tenantId) {
    Integer reserved = reservationRepository.getTotalReservedStock(variantId, storeId, tenantId);
    return reserved != null ? reserved : 0;
}
```

Then in PublicProductController line 117:
```java
Integer reservedStock = reservationService.getReservedStock(variantId, storeId, tenantId);
```

---

## Workaround (Temporary)

### Frontend Workaround

**File:** `atozshop-frontend/src/context/CartContext.tsx`

Comment out stock check temporarily:

```typescript
const addToCart = async (item: Omit<CartItem, 'totalPrice'>) => {
    // TEMPORARY: Skip stock check due to backend bug
    // const stock = await productService.checkStockAvailability(item.variantId);
    // if (stock.availableStock < item.quantity) {
    //     throw new Error(`Only ${stock.availableStock} items available in stock`);
    // }

    // Use product's availableStock from listing instead
    // (This works because product listing uses correct method)

    setCart((prevCart) => {
        // ... rest of the code
    });
};
```

**Risk:** This bypasses stock validation. Use only for testing!

### Backend Workaround (Quick Fix)

Edit `PublicProductController.java` line 117-125:

```java
@GetMapping("/variant/{variantId}/availability")
public ResponseEntity<StockAvailabilityResponse> checkAvailability(
        @PathVariable Long variantId,
        @RequestParam Long tenantId,
        @RequestParam Long storeId
) {
    ProductVariant variant = variantRepository.findById(variantId)
            .orElseThrow(() -> new RuntimeException("Variant not found"));

    Integer currentStock = stockLedgerRepository.getCurrentStock(variantId, storeId, tenantId);
    if (currentStock == null) currentStock = 0;

    // FIX: Get actual reserved stock
    Integer reservedStock = reservationRepository.getTotalReservedStock(variantId, storeId, tenantId);
    if (reservedStock == null) reservedStock = 0;

    Integer soldStock = 0;  // Not calculated for now

    StockAvailabilityResponse response = StockAvailabilityResponse.from(
            variantId, variant.getSku(), currentStock, soldStock, reservedStock
    );

    return ResponseEntity.ok(response);
}
```

---

## Testing After Fix

### Test API Endpoint
```bash
curl -X GET "http://localhost:8080/api/v1/public/products/variant/2/availability?tenantId=1&storeId=1" \
  -H "Authorization: Bearer <token>"
```

**Expected Response:**
```json
{
  "variantId": 2,
  "sku": "IPH15P-TIT-256",
  "totalStock": 50,
  "soldStock": 0,
  "reservedStock": 0,
  "availableStock": 50,
  "stockStatus": "In Stock",
  "isAvailable": true,
  "message": "Available"
}
```

### Test Frontend Cart
1. Login to frontend
2. Click "Add to Cart" on any product
3. Should succeed without error
4. Cart badge should show (1)
5. Cart page should show item

---

## Related Files

- `src/main/java/com/atozshop/controller/PublicProductController.java` (BUG HERE)
- `src/main/java/com/atozshop/service/StockReservationService.java`
- `src/main/java/com/atozshop/repository/StockReservationRepository.java`
- `src/main/java/com/atozshop/dto/response/StockAvailabilityResponse.java`
- `atozshop-frontend/src/context/CartContext.tsx` (calls buggy endpoint)
- `atozshop-frontend/src/services/productService.ts` (calls buggy endpoint)

---

## Why This Wasn't Caught Earlier

1. **Product listing works** because it uses different code path
2. **Initial testing** focused on product display, not cart
3. **Variable naming confusion** - method returns "available" but assigned to "reserved"
4. **No stock reservations exist** - if there were active reservations, bug would be more obvious

---

## Fix Priority

**Priority:** 🔴 CRITICAL - Must fix before any user testing

**Recommended Action:** Apply Option 1 fix immediately

**Estimated Fix Time:** 5 minutes

**Testing Time:** 10 minutes

---

**Reported By:** Claude Opus 4.6
**Date:** March 2, 2026, 7:45 AM IST

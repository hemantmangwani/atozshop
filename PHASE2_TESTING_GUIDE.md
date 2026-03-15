# Phase 2 POS Billing - Testing Guide

## Prerequisites

1. **Application Running**: Port 8080
2. **Database**: PostgreSQL with Phase 0 and Phase 1 data
3. **Auth Token**: JWT token from Phase 0 authentication
4. **Test Data**: At least one product variant with stock from Phase 1

---

## Test Scenario: Complete POS Transaction Flow

### Step 1: Create a Customer

```bash
POST http://localhost:8080/api/v1/customers
Content-Type: application/json
Authorization: Bearer YOUR_JWT_TOKEN

{
  "tenantId": 1,
  "name": "John Doe",
  "phone": "9876543210",
  "email": "john.doe@example.com",
  "address": "123 Main Street",
  "city": "Mumbai",
  "state": "Maharashtra",
  "postalCode": "400001"
}
```

**Expected Response**:
```json
{
  "id": 1,
  "customerCode": "CUST-20260228-001",
  "name": "John Doe",
  "phone": "9876543210",
  "loyaltyPoints": 0,
  "totalPurchases": 0.00,
  "isActive": true
}
```

---

### Step 2: Find Customer by Phone

```bash
GET http://localhost:8080/api/v1/customers/phone/9876543210?tenantId=1
Authorization: Bearer YOUR_JWT_TOKEN
```

---

### Step 3: Get Product Variant (from Phase 1)

```bash
GET http://localhost:8080/api/v1/products?tenantId=1&storeId=1
Authorization: Bearer YOUR_JWT_TOKEN
```

**Copy a `variantId` from the response** (e.g., variant ID 1)

---

### Step 4: Check Current Stock

```bash
GET http://localhost:8080/api/v1/stock/current?tenantId=1&storeId=1
Authorization: Bearer YOUR_JWT_TOKEN
```

**Note the available quantity for your chosen variant**

---

### Step 5: Create a Bill (DRAFT)

```bash
POST http://localhost:8080/api/v1/bills
Content-Type: application/json
Authorization: Bearer YOUR_JWT_TOKEN

{
  "tenantId": 1,
  "storeId": 1,
  "customerId": 1,
  "billType": "SALES",
  "cashierId": 1,
  "items": [
    {
      "variantId": 1,
      "quantity": 2,
      "discountPercent": 0,
      "discountAmount": 0
    },
    {
      "variantId": 2,
      "quantity": 1,
      "discountPercent": 10,
      "discountAmount": 0
    }
  ],
  "notes": "First POS sale"
}
```

**Expected Response**:
```json
{
  "id": 1,
  "billNumber": "BIL-20260228-001",
  "status": "DRAFT",
  "paymentStatus": "UNPAID",
  "customerName": "John Doe",
  "items": [...],
  "totalItems": 2,
  "totalQuantity": 3,
  "subtotal": 500.00,
  "totalAmount": 500.00,
  "balanceAmount": 500.00
}
```

**Copy the `billId` from response** (e.g., 1)

---

### Step 6: Add Another Item to Bill

```bash
POST http://localhost:8080/api/v1/bills/1/items?tenantId=1&storeId=1
Content-Type: application/json
Authorization: Bearer YOUR_JWT_TOKEN

{
  "variantId": 3,
  "quantity": 1,
  "discountPercent": 0,
  "discountAmount": 0
}
```

---

### Step 7: Get Bill Details

```bash
GET http://localhost:8080/api/v1/bills/1?tenantId=1
Authorization: Bearer YOUR_JWT_TOKEN
```

---

### Step 8: Confirm Bill (Critical - Stock Deduction)

```bash
POST http://localhost:8080/api/v1/bills/1/confirm?tenantId=1
Authorization: Bearer YOUR_JWT_TOKEN
```

**Expected Response**:
```json
{
  "id": 1,
  "status": "CONFIRMED",
  "paymentStatus": "UNPAID",
  ...
}
```

**This will**:
- Change bill status to CONFIRMED
- Create SALE entries in stock_ledger (Phase 1)
- Deduct quantities from current stock
- Update customer total purchases

---

### Step 9: Verify Stock Deduction

```bash
GET http://localhost:8080/api/v1/stock/ledger/variant/1?tenantId=1&storeId=1
Authorization: Bearer YOUR_JWT_TOKEN
```

**Look for SALE transaction** with negative quantity

```bash
GET http://localhost:8080/api/v1/stock/current?tenantId=1&storeId=1
Authorization: Bearer YOUR_JWT_TOKEN
```

**Verify quantity decreased**

---

### Step 10: Process Payment (Cash)

```bash
POST http://localhost:8080/api/v1/payments
Content-Type: application/json
Authorization: Bearer YOUR_JWT_TOKEN

{
  "billId": 1,
  "tenantId": 1,
  "paymentMethod": "CASH",
  "amount": 300.00,
  "createdBy": 1
}
```

**Expected**:
- paymentStatus changes to PARTIAL
- balanceAmount = 200.00

---

### Step 11: Process Remaining Payment (Card)

```bash
POST http://localhost:8080/api/v1/payments
Content-Type: application/json
Authorization: Bearer YOUR_JWT_TOKEN

{
  "billId": 1,
  "tenantId": 1,
  "paymentMethod": "CARD",
  "amount": 200.00,
  "referenceNumber": "TXN123456",
  "cardLast4": "1234",
  "bankName": "HDFC Bank",
  "createdBy": 1
}
```

**Expected**:
- paymentStatus changes to PAID
- balanceAmount = 0.00

---

### Step 12: Get Bill Payments

```bash
GET http://localhost:8080/api/v1/payments/bill/1
Authorization: Bearer YOUR_JWT_TOKEN
```

---

### Step 13: Get Customer Purchase History

```bash
GET http://localhost:8080/api/v1/customers/1/purchase-history?tenantId=1
Authorization: Bearer YOUR_JWT_TOKEN
```

**Verify**:
- totalPurchases updated
- recentBills shows the sale

---

### Step 14: Get Daily Sales Report

```bash
GET http://localhost:8080/api/v1/reports/daily-sales?tenantId=1&storeId=1&date=2026-02-28
Authorization: Bearer YOUR_JWT_TOKEN
```

**Expected**:
```json
{
  "reportDate": "2026-02-28",
  "totalTransactions": 1,
  "totalItemsSold": 3,
  "totalSales": 500.00,
  "paymentMethodBreakdown": {
    "CASH": 300.00,
    "CARD": 200.00
  },
  "averageTransactionValue": 500.00
}
```

---

## Additional Test Cases

### Test Discount Management

```bash
# Create Discount
POST http://localhost:8080/api/v1/discounts
{
  "tenantId": 1,
  "discountCode": "WELCOME10",
  "name": "Welcome Discount",
  "discountType": "PERCENTAGE",
  "discountValue": 10,
  "applicableOn": "BILL",
  "validFrom": "2026-02-01",
  "validTo": "2026-03-31",
  "minPurchaseAmount": 200
}
```

```bash
# Get Active Discounts
GET http://localhost:8080/api/v1/discounts/active?tenantId=1
```

---

### Test Search Customers

```bash
GET http://localhost:8080/api/v1/customers/search?keyword=John&tenantId=1
```

---

### Test Bill Cancellation

```bash
# Create another DRAFT bill first
POST http://localhost:8080/api/v1/bills
{...}

# Cancel it (only DRAFT bills can be cancelled)
POST http://localhost:8080/api/v1/bills/2/cancel?tenantId=1
```

---

## Validation Test Cases

### Test Insufficient Stock

Create a bill with quantity > available stock, then try to confirm:

```bash
POST http://localhost:8080/api/v1/bills/X/confirm?tenantId=1
```

**Expected**: `InsufficientStockException`

---

### Test Payment Exceeding Balance

Try to pay more than balance amount:

```bash
POST http://localhost:8080/api/v1/payments
{
  "billId": 1,
  "amount": 99999.00,
  ...
}
```

**Expected**: `PaymentException`

---

### Test Confirming Non-DRAFT Bill

Try to confirm an already CONFIRMED bill:

```bash
POST http://localhost:8080/api/v1/bills/1/confirm?tenantId=1
```

**Expected**: `IllegalStateException`

---

## Integration Verification Checklist

- [ ] Customer created with auto-generated code
- [ ] Bill created in DRAFT status
- [ ] Bill number auto-generated correctly
- [ ] Item prices fetched from Phase 1 price table
- [ ] Bill confirmation changes status to CONFIRMED
- [ ] Stock ledger shows SALE transactions
- [ ] Current stock quantities decreased
- [ ] Stock validation prevents overselling
- [ ] Split payments work correctly
- [ ] Payment status updates (UNPAID→PARTIAL→PAID)
- [ ] Customer total purchases updated
- [ ] Daily sales report shows correct data
- [ ] Phase 1 APIs still work (product, category, stock)

---

## Performance Notes

- Bill confirmation is transactional (atomic)
- Stock validation happens before stock deduction
- Failed stock deduction rolls back entire bill confirmation
- Payment processing updates bill in same transaction

---

## Swagger UI

Access interactive API documentation:

```
http://localhost:8080/swagger-ui/index.html
```

Navigate to:
- Customer Management
- Bill Management
- Payment Management
- Discount Management
- Sales Reports

---

## Troubleshooting

### "Variant not found"
- Ensure you have product variants from Phase 1
- Check variantId exists in database

### "Price not found for variant"
- Ensure variant has a price record in variant_prices
- Check effectiveFrom/effectiveTo dates

### "Insufficient stock"
- Check current stock via `/api/v1/stock/current`
- Ensure PURCHASE transactions exist from Phase 1

### "Bill is already CONFIRMED"
- Cannot modify confirmed bills
- Create a new bill for testing

---

**Testing Status**: Ready ✅
**Integration**: Phase 1 + Phase 2
**Environment**: Development

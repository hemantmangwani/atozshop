# AtoZShop - Complete API Postman Guide

## Overview

This guide covers **all APIs** from Phase 0 (Authentication), Phase 1 (Inventory), and Phase 2 (POS Billing).

**Collection**: `AtoZShop_Complete_API_Collection.postman_collection.json`
**Environment**: `AtoZShop_Complete.postman_environment.json`

---

## Quick Start

### 1. Import Files into Postman

1. Open Postman
2. Click **Import** button
3. Import both files:
   - `AtoZShop_Complete_API_Collection.postman_collection.json`
   - `AtoZShop_Complete.postman_environment.json`
4. Select the **AtoZShop - Complete Environment** from the environment dropdown

### 2. First-Time Setup

Run these requests in order:

1. **Phase 0 → Register New User** - Create your test account
2. **Phase 0 → Login** - Get JWT token (auto-saved to environment)
3. Now you can use any API!

---

## Collection Structure (90+ Requests)

### 📁 Phase 0 - Authentication & Authorization (3 requests)
- Register New User
- Login (auto-saves JWT token)
- Get Home (test auth)

### 📁 Phase 1 - Category Management (5 requests)
- Create Category
- Get All Categories
- Get Category by ID
- Update Category
- Delete Category

### 📁 Phase 1 - Product Management (5 requests)
- Create Product
- Get All Products
- Get Product by ID
- Search Products by Category
- Update Product

### 📁 Phase 1 - Product Variant Management (4 requests)
- Create Product Variant (with barcode & pricing)
- Get Variant by ID
- Get Variant by Barcode (POS integration)
- Update Variant

### 📁 Phase 1 - Stock Management (3 requests)
- Record Incoming Stock (PURCHASE)
- Get Stock Ledger for Variant (event history)
- Get Low Stock Alerts

### 📁 Phase 2 - Customer Management (7 requests)
- Create Customer (auto customer code)
- Get All Customers
- Search Customers (by name/phone)
- Get Customer by Phone (quick lookup)
- Get Customer Purchase History
- Update Customer
- Delete Customer (soft delete)

### 📁 Phase 2 - POS Billing (9 requests)
- Create Bill (DRAFT status)
- Get All Bills
- Get Bill by ID
- Get Bill by Number
- Add Item to Bill
- Update Bill Item
- Remove Item from Bill
- **Confirm Bill (Deduct Stock)** ⚡ CRITICAL
- Cancel Bill

### 📁 Phase 2 - Payment Processing (5 requests)
- Process Payment (Cash)
- Process Payment (Card)
- Process Payment (UPI)
- Get Payments by Bill
- Get Payments by Date Range

### 📁 Phase 2 - Discount Management (5 requests)
- Create Discount
- Get All Active Discounts
- Get Discount by Code
- Update Discount
- Delete Discount

### 📁 Phase 2 - Sales Reports (1 request)
- Daily Sales Report

### 📁 Complete POS Transaction Flow (7 requests)
Pre-configured workflow demonstrating complete transaction

---

## Environment Variables

The environment file includes these variables:

| Variable | Default Value | Description |
|----------|---------------|-------------|
| `base_url` | http://localhost:8080 | API base URL |
| `jwt_token` | (empty) | Auto-populated on login |
| `tenant_id` | 1 | Your tenant/organization ID |
| `store_id` | 1 | Store location ID |
| `user_id` | 1 | Current user ID |
| `test_email` | test@atozshop.com | Test account email |
| `test_password` | Test@123 | Test account password |

**Note**: The `jwt_token` is automatically saved when you use the Login request.

---

## Complete POS Transaction Workflow

### Step-by-Step Guide

Use the **"Complete POS Transaction Flow"** folder for end-to-end testing:

#### 1. Login
```
POST /api/v1/auth/login
```
- Authenticates user
- Auto-saves JWT token to environment

#### 2. Find/Create Customer
```
GET /api/v1/customers/phone/9876543210
```
- Quick customer lookup by phone
- If not found, create using "Create Customer" request

#### 3. Scan Product Barcode
```
GET /api/v1/variants/barcode/8801234567890
```
- Simulates barcode scanning
- Returns variant with current price

#### 4. Create Bill
```
POST /api/v1/bills
```
- Creates bill in DRAFT status
- Add multiple items
- Bill number auto-generated (BIL-YYYYMMDD-XXX)

#### 5. Confirm Bill (Stock Deduct) ⚡
```
POST /api/v1/bills/1/confirm
```
**CRITICAL STEP**:
- Validates stock availability
- Creates SALE transactions in Phase 1 ledger
- Deducts stock automatically
- Makes bill immutable

#### 6. Process Payment
```
POST /api/v1/payments
```
- Accept cash, card, UPI, etc.
- Split payments supported
- Updates payment status automatically

#### 7. Verify Stock Ledger
```
GET /api/v1/stock/ledger/variant/1
```
- Verify SALE transaction created
- Check negative quantity_change
- Confirm stock deducted

---

## Key Features Demonstrated

### 🔐 Authentication
- JWT-based authentication
- Token auto-saved and reused
- Multi-tenant support

### 📦 Inventory Management (Phase 1)
- Product catalog
- Variant management with barcodes
- Event-sourced stock ledger
- Real-time stock tracking

### 🛒 POS Billing (Phase 2)
- Quick customer lookup
- Barcode scanning
- Shopping cart management
- Auto bill number generation
- Stock validation
- **Automatic stock deduction**
- Split payment support
- Payment method tracking

### 📊 Reporting
- Daily sales reports
- Payment method breakdown
- Customer purchase history

---

## Sample Data Examples

### Create Category
```json
{
  "tenantId": 1,
  "name": "Electronics",
  "description": "Electronic items and gadgets",
  "slug": "electronics"
}
```

### Create Product
```json
{
  "tenantId": 1,
  "categoryId": 1,
  "name": "Samsung Galaxy S23",
  "slug": "samsung-galaxy-s23",
  "description": "Latest Samsung flagship",
  "brand": "Samsung"
}
```

### Create Product Variant
```json
{
  "tenantId": 1,
  "productId": 1,
  "storeId": 1,
  "variantName": "128GB Black",
  "sku": "SGS23-128-BLK",
  "unit": "PCS",
  "barcodeValue": "8801234567890",
  "costPrice": 45000,
  "sellingPrice": 55000,
  "mrp": 59999,
  "minStockThreshold": 5
}
```

### Record Stock Purchase
```json
{
  "tenantId": 1,
  "storeId": 1,
  "supplierId": 1,
  "receivedDate": "2026-02-28",
  "items": [
    {
      "variantId": 1,
      "quantity": 50,
      "costPrice": 45000,
      "sellingPrice": 55000
    }
  ]
}
```

### Create Customer
```json
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

### Create Bill
```json
{
  "tenantId": 1,
  "storeId": 1,
  "customerId": 1,
  "billType": "SALES",
  "cashierId": 1,
  "items": [
    {
      "variantId": 1,
      "quantity": 2
    },
    {
      "variantId": 2,
      "quantity": 1
    }
  ],
  "notes": "First sale via POS"
}
```

### Process Payment (Split Payment Example)
```json
// First payment - Cash
{
  "billId": 1,
  "tenantId": 1,
  "paymentMethod": "CASH",
  "amount": 30000,
  "createdBy": 1
}

// Second payment - Card
{
  "billId": 1,
  "tenantId": 1,
  "paymentMethod": "CARD",
  "amount": 80000,
  "referenceNumber": "TXN123456",
  "cardLast4": "1234",
  "bankName": "HDFC Bank",
  "createdBy": 1
}
```

### Create Discount
```json
{
  "tenantId": 1,
  "discountCode": "WELCOME10",
  "name": "Welcome Discount 10%",
  "description": "First purchase discount",
  "discountType": "PERCENTAGE",
  "discountValue": 10,
  "minPurchaseAmount": 1000,
  "maxDiscountAmount": 5000,
  "applicableOn": "BILL",
  "validFrom": "2026-02-01",
  "validTo": "2026-03-31"
}
```

---

## Testing Scenarios

### Scenario 1: Basic Sale
1. Login
2. Create/Find Customer
3. Create Bill with 2 items
4. Confirm Bill
5. Process Cash Payment
6. Check stock ledger - verify SALE entries

### Scenario 2: Split Payment
1. Create Bill for ₹110,000
2. Confirm Bill
3. Pay ₹50,000 cash (status → PARTIAL)
4. Pay ₹60,000 card (status → PAID)

### Scenario 3: Customer Purchase History
1. Create Customer
2. Create and confirm 3 bills
3. Get customer purchase history
4. Verify totalPurchases updated

### Scenario 4: Stock Management
1. Record incoming stock (50 units)
2. Create bill for 10 units
3. Confirm bill
4. Check stock ledger - see PURCHASE + SALE
5. Verify current stock = 40

### Scenario 5: Discount Application
1. Create discount "SAVE20" (20% off)
2. Create bill
3. Apply discount code
4. Verify discount calculated correctly

---

## Common API Patterns

### Query Parameters
Most GET requests use:
- `tenantId` - Required for multi-tenant filtering
- `storeId` - For store-specific data
- `page` & `size` - For pagination

### Response Format
All successful responses return appropriate status codes:
- `200 OK` - Success
- `201 Created` - Resource created
- `204 No Content` - Successful deletion
- `400 Bad Request` - Validation error
- `401 Unauthorized` - Missing/invalid JWT
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

### Authentication
All requests (except Register/Login) require JWT:
```
Authorization: Bearer {{jwt_token}}
```
This is automatically handled by the collection-level auth.

---

## Advanced Features

### Bill Status Workflow
```
DRAFT → CONFIRMED → (optional) CANCELLED
        ↓
   Stock Deducted
```

- **DRAFT**: Can modify items, not yet committed
- **CONFIRMED**: Immutable, stock deducted, ready for payment
- **CANCELLED**: Only DRAFT bills can be cancelled

### Payment Status Transitions
```
UNPAID → PARTIAL → PAID
```

Automatically updated based on:
- `paidAmount == 0` → UNPAID
- `paidAmount < totalAmount` → PARTIAL
- `paidAmount >= totalAmount` → PAID

### Phase 1 Integration
When you confirm a bill:
1. Stock availability validated
2. SALE transactions created in stock_ledger
3. Negative quantity for each item
4. Price snapshots stored
5. Current stock updated automatically

**No manual stock adjustment needed!**

---

## Troubleshooting

### "Unauthorized" Error
**Solution**: Run the Login request first to get JWT token

### "Validation Failed"
**Solution**: Check request body matches required fields

### "Stock not available"
**Solution**:
1. Check current stock via stock ledger
2. Add stock via "Record Incoming Stock"
3. Retry bill confirmation

### "Bill is already CONFIRMED"
**Solution**: Cannot modify confirmed bills, create new one

### "Customer already exists"
**Solution**: Use "Get Customer by Phone" to find existing

---

## API Endpoint Summary

| Category | Endpoints | Purpose |
|----------|-----------|---------|
| Authentication | 3 | User registration and login |
| Categories | 5 | Product categorization |
| Products | 5 | Product catalog |
| Variants | 4 | SKUs, barcodes, pricing |
| Stock | 3 | Inventory tracking (event-sourced) |
| Customers | 7 | Customer management |
| Bills | 9 | POS billing and invoicing |
| Payments | 5 | Payment processing |
| Discounts | 5 | Promotional offers |
| Reports | 1 | Sales analytics |
| **Total** | **47** | **Complete API coverage** |

---

## Best Practices

1. **Always login first** - JWT token required for all protected endpoints
2. **Use customer phone lookup** - Faster than searching by name
3. **Confirm bills only when ready** - Cannot undo stock deduction
4. **Check stock before confirming** - Prevents validation errors
5. **Use barcode scanning** - Faster than manual variant selection
6. **Record split payments separately** - Better tracking
7. **Apply discounts before confirmation** - Cannot modify after

---

## Quick Reference

### Auto-Generated Codes
- Customer Code: `CUST-20260228-001`
- Bill Number: `BIL-20260228-001`
- Format: `TYPE-YYYYMMDD-XXX`

### Payment Methods
- CASH
- CARD
- UPI
- WALLET
- CHEQUE

### Bill Types
- SALES
- SALES_RETURN

### Discount Types
- PERCENTAGE (e.g., 10%)
- FIXED_AMOUNT (e.g., ₹500)

### Stock Transaction Types (Phase 1)
- PURCHASE (incoming stock)
- SALE (from POS billing)
- ADJUSTMENT (manual correction)
- TRANSFER (inter-store)
- DAMAGE (loss/wastage)

---

## Support

For issues or questions:
1. Check the error message in response
2. Verify JWT token is valid
3. Confirm request body format
4. Check Phase 2 test results: `PHASE2_TEST_RESULTS.md`
5. Review API documentation: `API_QUICK_REFERENCE.md`

---

**Collection Version**: 2.0.0
**Last Updated**: February 28, 2026
**Total Requests**: 47+ organized requests
**Coverage**: Phase 0, Phase 1, Phase 2 - Complete System

**Ready to use!** 🚀

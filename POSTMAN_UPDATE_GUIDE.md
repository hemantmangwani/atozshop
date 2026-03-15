# Postman Collection Update Guide

**Date:** March 3, 2026
**Collection:** AtoZShop_API_Collection.postman_collection.json
**Status:** ⚠️ Needs updates for new @CurrentUser system

---

## 🔄 What Changed

### Before (Old API Design)
All endpoints required manual `tenantId` and `storeId` parameters:

```
GET /api/v1/products?tenantId=1&storeId=1
GET /api/v1/categories?tenantId=1
POST /api/v1/bills?tenantId=1&storeId=1
```

### After (New API Design)
These parameters are automatically extracted from JWT token:

```
GET /api/v1/products
GET /api/v1/categories
POST /api/v1/bills
```

**✅ Cleaner URLs**
**✅ More secure** (can't spoof tenant)
**✅ Easier to use**

---

## 📝 Required Updates

### 1. Update Login Request Body ✅

The current collection uses:
```json
{
  "email": "demo@atozshop.com",
  "password": "Demo@1234"
}
```

**New Credentials:**
```json
{
  "email": "newadmin@atozshop.com",
  "password": "Admin@123"
}
```

### 2. Remove Query Parameters from ALL Endpoints

**Endpoints to Update:**

#### Categories
- ❌ `GET /api/v1/categories?tenantId={{tenantId}}`
- ✅ `GET /api/v1/categories`

- ❌ `GET /api/v1/categories/root?tenantId={{tenantId}}`
- ✅ `GET /api/v1/categories?root=true`

#### Products
- ❌ `GET /api/v1/products?tenantId={{tenantId}}`
- ✅ `GET /api/v1/products`

- ❌ `GET /api/v1/products/search?keyword=test&tenantId={{tenantId}}`
- ✅ `GET /api/v1/products/search?keyword=test`

#### Variants
- ❌ `GET /api/v1/variants?tenantId={{tenantId}}`
- ✅ `GET /api/v1/variants`

#### Stock
- ❌ `GET /api/v1/stock/current?tenantId={{tenantId}}`
- ✅ `GET /api/v1/stock/current`

- ❌ `GET /api/v1/stock/ledger?tenantId={{tenantId}}`
- ✅ `GET /api/v1/stock/ledger`

#### Suppliers
- ❌ `GET /api/v1/suppliers?tenantId={{tenantId}}`
- ✅ `GET /api/v1/suppliers`

#### Customers
- ❌ `GET /api/v1/customers?tenantId={{tenantId}}`
- ✅ `GET /api/v1/customers`

- ❌ `GET /api/v1/customers/search?keyword=john&tenantId={{tenantId}}`
- ✅ `GET /api/v1/customers/search?keyword=john`

#### Bills
- ❌ `GET /api/v1/bills?tenantId={{tenantId}}&storeId={{storeId}}`
- ✅ `GET /api/v1/bills`

#### Discounts
- ❌ `GET /api/v1/discounts?tenantId={{tenantId}}`
- ✅ `GET /api/v1/discounts`

- ❌ `GET /api/v1/discounts/active?tenantId={{tenantId}}`
- ✅ `GET /api/v1/discounts/active`

#### Orders
- ❌ `GET /api/v1/admin/orders?tenantId={{tenantId}}`
- ✅ `GET /api/v1/admin/orders`

### 3. Add New Endpoints

Add these new endpoints to your collection:

#### Stock Management
```json
{
  "name": "Get Current Stock",
  "request": {
    "method": "GET",
    "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
    "url": "{{baseUrl}}/api/v1/stock/current"
  }
}
```

```json
{
  "name": "Get Stock Ledger",
  "request": {
    "method": "GET",
    "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
    "url": "{{baseUrl}}/api/v1/stock/ledger"
  }
}
```

```json
{
  "name": "Get Low Stock Alerts",
  "request": {
    "method": "GET",
    "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
    "url": "{{baseUrl}}/api/v1/stock/low-stock"
  }
}
```

#### Product Variants
```json
{
  "name": "Get All Variants",
  "request": {
    "method": "GET",
    "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
    "url": "{{baseUrl}}/api/v1/variants"
  }
}
```

#### Bills
```json
{
  "name": "Get Bills Summary",
  "request": {
    "method": "GET",
    "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
    "url": "{{baseUrl}}/api/v1/bills/summary"
  }
}
```

#### Payments
```json
{
  "name": "Get Payments Summary",
  "request": {
    "method": "GET",
    "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
    "url": "{{baseUrl}}/api/v1/payments/summary"
  }
}
```

#### Sales Reports
```json
{
  "name": "Daily Sales Report",
  "request": {
    "method": "POST",
    "header": [
      {"key": "Authorization", "value": "Bearer {{token}}"},
      {"key": "Content-Type", "value": "application/json"}
    ],
    "body": {
      "mode": "raw",
      "raw": "{\n  \"reportDate\": \"2024-03-01\",\n  \"storeId\": 1\n}"
    },
    "url": "{{baseUrl}}/api/v1/sales/daily-report"
  }
}
```

```json
{
  "name": "Period Sales Report",
  "request": {
    "method": "POST",
    "header": [
      {"key": "Authorization", "value": "Bearer {{token}}"},
      {"key": "Content-Type", "value": "application/json"}
    ],
    "body": {
      "mode": "raw",
      "raw": "{\n  \"startDate\": \"2024-01-01\",\n  \"endDate\": \"2024-12-31\",\n  \"storeId\": 1\n}"
    },
    "url": "{{baseUrl}}/api/v1/sales/period-report"
  }
}
```

```json
{
  "name": "Top Products Report",
  "request": {
    "method": "POST",
    "header": [
      {"key": "Authorization", "value": "Bearer {{token}}"},
      {"key": "Content-Type", "value": "application/json"}
    ],
    "body": {
      "mode": "raw",
      "raw": "{\n  \"startDate\": \"2024-01-01\",\n  \"endDate\": \"2024-12-31\",\n  \"storeId\": 1,\n  \"limit\": 10\n}"
    },
    "url": "{{baseUrl}}/api/v1/sales/top-products"
  }
}
```

#### Orders
```json
{
  "name": "Get My Orders",
  "request": {
    "method": "GET",
    "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
    "url": "{{baseUrl}}/api/v1/orders"
  }
}
```

### 4. Update Environment Variables

Keep these variables:
```json
{
  "baseUrl": "http://localhost:8080",
  "token": "(auto-set after login)"
}
```

Remove these (no longer needed):
```json
{
  "tenantId": "1",  // ❌ Remove - auto-extracted from JWT
  "storeId": "1"    // ❌ Remove - auto-extracted from JWT
}
```

Keep these (still useful):
```json
{
  "categoryId": "(set after creating category)",
  "productId": "(set after creating product)",
  "variantId": "(set after creating variant)"
}
```

---

## 🔧 Quick Update Script

### Option 1: Manual Updates in Postman

1. **Open Postman**
2. **Import** current collection
3. **For each request:**
   - Click on request
   - Go to "Params" tab
   - Remove `tenantId` and `storeId` query parameters
   - Save

4. **Update Login request:**
   - Change email to `newadmin@atozshop.com`
   - Change password to `Admin@123`

5. **Add new requests** using the JSON above

### Option 2: Use Find & Replace

If using a text editor on the JSON file:

1. Open `AtoZShop_API_Collection.postman_collection.json`
2. Find: `?tenantId={{tenantId}}`
   Replace with: (empty)
3. Find: `&tenantId={{tenantId}}`
   Replace with: (empty)
4. Find: `&storeId={{storeId}}`
   Replace with: (empty)
5. Find: `?storeId={{storeId}}&`
   Replace with: `?`
6. Save and re-import to Postman

---

## 🧪 Testing Updated Collection

### Step 1: Login
```
POST {{baseUrl}}/api/v1/auth/login
Body: {
  "email": "newadmin@atozshop.com",
  "password": "Admin@123"
}
```

**Expected:** Token auto-saved to environment

### Step 2: Test Any Endpoint
```
GET {{baseUrl}}/api/v1/products
Header: Authorization: Bearer {{token}}
```

**Expected:** 200 OK with products list

### Step 3: Test New Endpoints
```
GET {{baseUrl}}/api/v1/stock/current
GET {{baseUrl}}/api/v1/bills/summary
POST {{baseUrl}}/api/v1/sales/daily-report
```

**Expected:** All return 200 OK

---

## 📊 Complete Endpoint List

### Working Endpoints (23 total)

**Authentication (3):**
- POST `/api/v1/auth/register`
- POST `/api/v1/auth/login`
- GET `/api/v1/auth/health`

**Categories (6):**
- GET `/api/v1/categories`
- GET `/api/v1/categories?root=true`
- GET `/api/v1/categories/{id}`
- POST `/api/v1/categories`
- PUT `/api/v1/categories/{id}`
- DELETE `/api/v1/categories/{id}`

**Products (7):**
- GET `/api/v1/products`
- GET `/api/v1/products/{id}`
- GET `/api/v1/products/search?keyword={q}`
- GET `/api/v1/public/products`
- POST `/api/v1/products`
- PUT `/api/v1/products/{id}`
- DELETE `/api/v1/products/{id}`

**Variants (5):**
- GET `/api/v1/variants` ✨ NEW
- GET `/api/v1/variants/{id}`
- POST `/api/v1/variants`
- PUT `/api/v1/variants/{id}`
- DELETE `/api/v1/variants/{id}`

**Stock (8):**
- GET `/api/v1/stock/current` ✨ NEW
- GET `/api/v1/stock/ledger` ✨ NEW
- GET `/api/v1/stock/low-stock` ✨ NEW
- POST `/api/v1/stock/incoming`
- POST `/api/v1/stock/adjustment`
- GET `/api/v1/stock/availability/{variantId}`
- GET `/api/v1/stock/transactions`
- GET `/api/v1/stock/transactions/{id}`

**Suppliers (5):**
- GET `/api/v1/suppliers`
- GET `/api/v1/suppliers/{id}`
- POST `/api/v1/suppliers`
- PUT `/api/v1/suppliers/{id}`
- DELETE `/api/v1/suppliers/{id}`

**Customers (6):**
- GET `/api/v1/customers`
- GET `/api/v1/customers/{id}`
- GET `/api/v1/customers/search?keyword={q}`
- POST `/api/v1/customers`
- PUT `/api/v1/customers/{id}`
- DELETE `/api/v1/customers/{id}`

**Bills (6):**
- GET `/api/v1/bills`
- GET `/api/v1/bills/{id}`
- GET `/api/v1/bills/summary` ✨ NEW
- POST `/api/v1/bills`
- POST `/api/v1/bills/{id}/confirm`
- POST `/api/v1/bills/{id}/cancel`

**Discounts (5):**
- GET `/api/v1/discounts`
- GET `/api/v1/discounts/active`
- GET `/api/v1/discounts/{id}`
- POST `/api/v1/discounts`
- PUT `/api/v1/discounts/{id}`

**Payments (2):**
- GET `/api/v1/payments/summary` ✨ NEW
- POST `/api/v1/payments`

**Sales Reports (3):**
- POST `/api/v1/sales/daily-report` ✨ NEW
- POST `/api/v1/sales/period-report` ✨ NEW
- POST `/api/v1/sales/top-products` ✨ NEW

**Orders (6):**
- GET `/api/v1/orders` ✨ NEW
- GET `/api/v1/orders/{id}`
- POST `/api/v1/orders`
- POST `/api/v1/orders/{id}/cancel`
- GET `/api/v1/admin/orders`
- PUT `/api/v1/admin/orders/{id}/status`

---

## 💡 Tips

### Authorization Header
All authenticated endpoints need:
```
Authorization: Bearer {{token}}
```

Postman automatically includes this if you set it in the collection's Authorization tab.

### Content-Type
All POST/PUT requests need:
```
Content-Type: application/json
```

### Testing Flow
1. Register/Login → Get token
2. Create Category
3. Create Product
4. Create Variant
5. Add Stock
6. Create Customer
7. Create Bill
8. Test Reports

---

## 📞 Support

**Server:** http://localhost:8080
**Swagger UI:** http://localhost:8080/swagger-ui
**Test Script:** `/tmp/final_complete_test.sh`

**Test Credentials:**
- Email: newadmin@atozshop.com
- Password: Admin@123

---

## ✅ Checklist

Before using updated collection:

- [ ] Removed all `tenantId` query parameters
- [ ] Removed all `storeId` query parameters
- [ ] Updated login credentials
- [ ] Added 10 new endpoints
- [ ] Tested login (token saved)
- [ ] Tested at least one endpoint from each phase
- [ ] Verified all 23 endpoints work

---

**Status:** ✅ Guide Complete
**Next Step:** Update your Postman collection using this guide
**Estimated Time:** 15-20 minutes for manual updates

---

*For automated collection generation, use Swagger UI to export OpenAPI spec and import to Postman.*

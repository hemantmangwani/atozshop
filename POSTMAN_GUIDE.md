# Postman API Testing Guide

Complete guide to testing all A to Z Shop Management APIs using Postman.

---

## 📥 Import Files into Postman

### Step 1: Import Collection
1. Open Postman
2. Click **Import** button (top left)
3. Select file: `AtoZShop_API_Collection.postman_collection.json`
4. Click **Import**

### Step 2: Import Environment
1. Click **Import** again
2. Select file: `AtoZShop_Environment.postman_environment.json`
3. Click **Import**
4. Select environment **"AtoZShop - Local Development"** from dropdown (top right)

---

## 🚀 Quick Start - Test Flow

### Prerequisites
1. Application running on `http://localhost:8080`
2. Database accessible
3. At least one tenant exists (ID: 1)
4. At least one store exists (ID: 1)

### Test Sequence

#### 1. **Authentication** (Register & Login)

**A. Register User** (First time only)
```
POST /api/v1/auth/register

Body:
{
  "tenantId": 1,
  "storeId": 1,
  "email": "demo@atozshop.com",
  "password": "Demo@1234",
  "username": "demouser",
  "firstName": "Demo",
  "lastName": "User",
  "phone": "9876543210"
}
```

**B. Login** (Get JWT Token)
```
POST /api/v1/auth/login

Body:
{
  "email": "demo@atozshop.com",
  "password": "Demo@1234"
}

✅ Token automatically saved to {{token}} variable
✅ TenantId automatically saved to {{tenantId}} variable
```

**C. Test Authentication**
```
GET /api/v1/home

Headers:
Authorization: Bearer {{token}}

Expected: Welcome message
```

---

#### 2. **Category Management**

**A. Create Category**
```
POST /api/v1/categories

Body:
{
  "tenantId": 1,
  "name": "Electronics",
  "description": "Electronic items and gadgets",
  "sortOrder": 1,
  "isActive": true
}

✅ Category ID automatically saved to {{categoryId}}
✅ Slug auto-generated: "electronics"
```

**B. Create Subcategory**
```
POST /api/v1/categories

Body:
{
  "tenantId": 1,
  "parentId": {{categoryId}},
  "name": "Mobile Phones",
  "description": "Smartphones and feature phones"
}
```

**C. List All Categories**
```
GET /api/v1/categories?tenantId=1
```

**D. Get Subcategories**
```
GET /api/v1/categories/{{categoryId}}/subcategories?tenantId=1
```

---

#### 3. **Product Management**

**A. Create Product**
```
POST /api/v1/products

Body:
{
  "tenantId": 1,
  "categoryId": {{categoryId}},
  "name": "iPhone 15 Pro",
  "brand": "Apple",
  "description": "Latest flagship iPhone"
}

✅ Product ID automatically saved to {{productId}}
✅ Slug auto-generated: "iphone-15-pro"
```

**B. Search Products**
```
GET /api/v1/products/search?keyword=iPhone&tenantId=1
```

**C. List All Products**
```
GET /api/v1/products?tenantId=1&page=0&size=20
```

---

#### 4. **Product Variants** (with Barcode)

**A. Create Variant**
```
POST /api/v1/variants

Body:
{
  "tenantId": 1,
  "productId": {{productId}},
  "sku": "IPH15P-TIT-256",
  "variantName": "Natural Titanium 256GB",
  "unit": "piece",
  "barcodeValue": "0194253484981",
  "costPrice": 115000,
  "sellingPrice": 134900,
  "minStockThreshold": 3,
  "maxStockThreshold": 20
}

✅ Variant ID automatically saved to {{variantId}}
✅ Initial price record created automatically
```

**B. Barcode Scan Lookup** ⭐
```
GET /api/v1/variants/barcode/0194253484981?tenantId=1

Use this for POS barcode scanning!
```

**C. SKU Lookup**
```
GET /api/v1/variants/sku/IPH15P-TIT-256?tenantId=1
```

**D. Low Stock Alerts** ⭐
```
GET /api/v1/variants/low-stock?tenantId=1

Returns variants where current stock <= minStockThreshold
```

---

#### 5. **Stock Management** (Event Sourcing)

**A. Create Incoming Stock** ⭐
```
POST /api/v1/stock/incoming

Body:
{
  "tenantId": 1,
  "storeId": 1,
  "supplierName": "Apple Authorized Distributor",
  "items": [
    {
      "variantId": {{variantId}},
      "quantity": 10,
      "costPrice": 115000,
      "sellingPrice": 134900,
      "remarks": "First batch"
    }
  ],
  "notes": "Stock received in good condition"
}

✅ Transaction ID saved to {{transactionId}}
✅ Status: DRAFT
✅ Profit calculated automatically:
   - Total Cost: ₹1,150,000
   - Expected Revenue: ₹1,349,000
   - Expected Profit: ₹199,000
```

**B. Confirm Transaction** ⭐⭐⭐
```
POST /api/v1/stock/incoming/{{transactionId}}/confirm?tenantId=1

This is the KEY action that:
✅ Creates stock ledger entries (event sourcing)
✅ Updates inventory levels
✅ Changes status to CONFIRMED
✅ Records price snapshots for historical accuracy
```

**C. Get Stock Ledger History** ⭐
```
GET /api/v1/stock/ledger/variant/{{variantId}}?tenantId=1&storeId=1

Shows complete audit trail:
- All INCOMING transactions
- All SALE transactions (when implemented)
- All RETURN transactions
- All ADJUSTMENT transactions
- Price snapshots at each transaction
- Running balance after each transaction
```

**D. Get Current Stock Levels** ⭐
```
GET /api/v1/stock/levels?tenantId=1&storeId=1

Returns current stock for all variants
Stock = SUM(quantity_change) from ledger (event sourcing)
```

**E. List All Transactions**
```
GET /api/v1/stock/incoming?tenantId=1&page=0&size=20
```

---

## 🔑 Environment Variables

The collection automatically manages these variables:

| Variable | Description | Auto-Set? |
|----------|-------------|-----------|
| `baseUrl` | API base URL | ✅ Default: http://localhost:8080 |
| `token` | JWT authentication token | ✅ After login |
| `tenantId` | Current tenant ID | ✅ After login (or default: 1) |
| `storeId` | Current store ID | Manual (default: 1) |
| `categoryId` | Last created category ID | ✅ After creating category |
| `productId` | Last created product ID | ✅ After creating product |
| `variantId` | Last created variant ID | ✅ After creating variant |
| `transactionId` | Last created transaction ID | ✅ After creating transaction |

---

## 📊 API Endpoint Summary

### Phase 0 - Authentication (3 endpoints)
- ✅ POST `/api/v1/auth/register` - Register user
- ✅ POST `/api/v1/auth/login` - Login (get JWT)
- ✅ GET `/api/v1/home` - Test auth

### Phase 1 - Categories (7 endpoints)
- ✅ POST `/api/v1/categories` - Create
- ✅ GET `/api/v1/categories` - List all
- ✅ GET `/api/v1/categories/{id}` - Get by ID
- ✅ GET `/api/v1/categories/{id}/subcategories` - Get children
- ✅ PUT `/api/v1/categories/{id}` - Update
- ✅ DELETE `/api/v1/categories/{id}` - Delete

### Phase 1 - Products (6 endpoints)
- ✅ POST `/api/v1/products` - Create
- ✅ GET `/api/v1/products` - List all (paginated)
- ✅ GET `/api/v1/products/search` - Search by keyword
- ✅ GET `/api/v1/products/{id}` - Get by ID
- ✅ PUT `/api/v1/products/{id}` - Update
- ✅ DELETE `/api/v1/products/{id}` - Delete

### Phase 1 - Variants (6 endpoints)
- ✅ POST `/api/v1/variants` - Create
- ✅ GET `/api/v1/variants/{id}` - Get by ID
- ✅ GET `/api/v1/variants/sku/{sku}` - Lookup by SKU
- ✅ GET `/api/v1/variants/barcode/{barcode}` - **Barcode scan**
- ✅ PUT `/api/v1/variants/{id}` - Update
- ✅ GET `/api/v1/variants/low-stock` - **Low stock alerts**

### Phase 1 - Stock Management (7 endpoints)
- ✅ POST `/api/v1/stock/incoming` - Create transaction
- ✅ GET `/api/v1/stock/incoming` - List all
- ✅ GET `/api/v1/stock/incoming/{id}` - Get by ID
- ✅ POST `/api/v1/stock/incoming/{id}/confirm` - **Confirm (update inventory)**
- ✅ POST `/api/v1/stock/incoming/{id}/cancel` - Cancel
- ✅ GET `/api/v1/stock/ledger/variant/{id}` - **Ledger history**
- ✅ GET `/api/v1/stock/levels` - **Current stock levels**

**Total: 32 API endpoints**

---

## 🎯 Key Features to Test

### 1. Auto-Generated Slugs
- Create category "Electronics & Gadgets"
- ✅ Slug becomes: "electronics-gadgets"
- Create product "iPhone 15 Pro Max"
- ✅ Slug becomes: "iphone-15-pro-max"

### 2. Profit Calculation
- Create incoming stock:
  - 10 units × Cost ₹115,000 = ₹1,150,000
  - 10 units × Selling ₹134,900 = ₹1,349,000
  - ✅ Profit: ₹199,000 (automatically calculated)

### 3. Event Sourcing (Stock Ledger)
- Create transaction → Status: DRAFT (no ledger entry yet)
- Confirm transaction → Creates ledger entry
- Check ledger history → See complete audit trail
- Check stock levels → Stock = SUM of all ledger entries
- ✅ Append-only log (never updates, only adds)

### 4. Barcode Scanning
- Create variant with barcode: "0194253484981"
- Scan barcode: GET `/variants/barcode/0194253484981`
- ✅ Returns variant details instantly (for POS)

### 5. Low Stock Alerts
- Create variant with minStockThreshold: 5
- Add 3 units of stock
- GET `/variants/low-stock`
- ✅ Returns variant in alert list (3 < 5)

### 6. Multi-Tenancy
- All requests require tenantId
- Data isolated by tenant
- User can only see their tenant's data
- ✅ Tenant-aware queries enforced

---

## 🧪 Complete Test Scenario

### Scenario: Set up inventory and receive stock

```bash
1. Register & Login
   → POST /auth/register
   → POST /auth/login
   ✅ Get JWT token

2. Create Category Hierarchy
   → POST /categories (Electronics)
   → POST /categories (Mobile Phones - child of Electronics)
   ✅ Categories created with auto-slugs

3. Create Product
   → POST /products (iPhone 15 Pro)
   ✅ Product created with auto-slug

4. Create Multiple Variants
   → POST /variants (Natural Titanium 256GB - Barcode: 0194253484981)
   → POST /variants (Blue Titanium 256GB - Barcode: 0194253484998)
   → POST /variants (White Titanium 512GB - Barcode: 0194253485001)
   ✅ 3 variants created with barcodes

5. Test Barcode Scanning
   → GET /variants/barcode/0194253484981
   ✅ Returns Natural Titanium variant

6. Receive Stock
   → POST /stock/incoming (10 units of each variant)
   ✅ Transaction created (DRAFT)
   ✅ Profit: ₹597,000 on ₹3,450,000 cost

7. Confirm Stock
   → POST /stock/incoming/{id}/confirm
   ✅ Status changed to CONFIRMED
   ✅ Ledger entries created (3 entries)

8. Verify Stock Levels
   → GET /stock/levels
   ✅ Each variant shows 10 units

9. Check Low Stock
   → GET /variants/low-stock
   ✅ Returns empty (all variants above threshold)

10. View Audit Trail
    → GET /stock/ledger/variant/{id}
    ✅ Shows complete history with price snapshots
```

---

## 🔍 Testing Tips

### 1. Check Response Status Codes
- `200 OK` - Successful GET, PUT, DELETE
- `201 Created` - Successful POST
- `400 Bad Request` - Validation error
- `401 Unauthorized` - Missing/invalid token
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

### 2. Automatic Variable Setting
The collection automatically saves IDs from responses:
- After creating category → `{{categoryId}}`
- After creating product → `{{productId}}`
- After creating variant → `{{variantId}}`
- After creating transaction → `{{transactionId}}`

Use these in subsequent requests!

### 3. Token Expiration
JWT tokens expire after 24 hours. If you get 401:
- Run "Login" request again
- Token will be refreshed automatically

### 4. View Console
Click "Console" (bottom left) to see:
- Request/response details
- Auto-saved variable values
- Any errors

### 5. Swagger Alternative
You can also test APIs at:
```
http://localhost:8080/swagger-ui.html
```

---

## 📝 Sample Data

### Test Categories
```json
[
  {"name": "Electronics", "description": "Electronic items"},
  {"name": "Mobile Phones", "parentId": 1, "description": "Smartphones"},
  {"name": "Laptops", "parentId": 1, "description": "Laptops & Notebooks"},
  {"name": "Clothing", "description": "Apparel and accessories"},
  {"name": "Men's Wear", "parentId": 4, "description": "Men's clothing"}
]
```

### Test Products
```json
[
  {"categoryId": 2, "name": "iPhone 15 Pro", "brand": "Apple"},
  {"categoryId": 2, "name": "Samsung Galaxy S24", "brand": "Samsung"},
  {"categoryId": 3, "name": "MacBook Pro M3", "brand": "Apple"},
  {"categoryId": 5, "name": "Men's Cotton T-Shirt", "brand": "Nike"}
]
```

### Test Variants
```json
[
  {
    "productId": 1,
    "sku": "IPH15P-TIT-256",
    "variantName": "Natural Titanium 256GB",
    "barcodeValue": "0194253484981",
    "costPrice": 115000,
    "sellingPrice": 134900,
    "minStockThreshold": 3
  },
  {
    "productId": 2,
    "sku": "SAM-S24-BLK-256",
    "variantName": "Phantom Black 256GB",
    "barcodeValue": "8806095171784",
    "costPrice": 75000,
    "sellingPrice": 89999,
    "minStockThreshold": 5
  }
]
```

---

## 🐛 Troubleshooting

### Issue: "Unauthorized" error
**Solution:**
1. Run "Login" request first
2. Check that token is saved (View → Environment)
3. Verify Authorization header has `Bearer {{token}}`

### Issue: "Tenant ID is required"
**Solution:**
- Ensure all request bodies include `"tenantId": 1`
- Check environment variable `{{tenantId}}` is set

### Issue: "Store not found"
**Solution:**
- Verify store exists: Run SQL query
  ```sql
  SELECT * FROM stores WHERE tenant_id = 1;
  ```
- If empty, create store manually in database
- Update `{{storeId}}` environment variable

### Issue: "Category not found"
**Solution:**
- Run "Create Category" request first
- Check `{{categoryId}}` is set in environment
- Use the saved ID in product creation

### Issue: Transaction not confirming
**Solution:**
- Check transaction status is DRAFT (not already CONFIRMED)
- Verify `tenantId` query parameter is included
- Check variants exist in the transaction items

---

## 📚 Additional Resources

- **API Documentation:** http://localhost:8080/swagger-ui.html
- **Database:** Connect with DBeaver/pgAdmin
  - Host: localhost:5432
  - Database: atozshop
  - Username: atozshop
  - Password: atozshop123
- **Implementation Guide:** See `PHASE1_COMPLETE.md`
- **Project Plan:** See `PROJECT_PLAN.md`

---

**Happy Testing! 🚀**

All 32 APIs are ready to test. Follow the test flow above for best results!

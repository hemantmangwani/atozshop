# API Quick Reference Card

Quick cheat sheet for A to Z Shop Management APIs

---

## 🔐 Authentication

```bash
# Register
POST /api/v1/auth/register
Body: {tenantId, storeId, email, password, firstName, lastName}

# Login (get JWT)
POST /api/v1/auth/login
Body: {email, password}
Response: {token, tenantId, ...}

# Use token in all requests
Authorization: Bearer {token}
```

---

## 📁 Categories

```bash
# Create
POST /api/v1/categories
Body: {tenantId, name, description, parentId?}

# List
GET /api/v1/categories?tenantId={id}

# Get by ID
GET /api/v1/categories/{id}?tenantId={tenantId}

# Get children
GET /api/v1/categories/{id}/subcategories?tenantId={tenantId}

# Update
PUT /api/v1/categories/{id}
Body: {tenantId, name, description}

# Delete
DELETE /api/v1/categories/{id}?tenantId={tenantId}
```

---

## 📦 Products

```bash
# Create
POST /api/v1/products
Body: {tenantId, categoryId, name, brand, description}

# List (paginated)
GET /api/v1/products?tenantId={id}&page=0&size=20

# Search
GET /api/v1/products/search?keyword={text}&tenantId={id}

# Get by ID
GET /api/v1/products/{id}?tenantId={tenantId}

# Update
PUT /api/v1/products/{id}
Body: {tenantId, categoryId, name, brand}

# Delete
DELETE /api/v1/products/{id}?tenantId={tenantId}
```

---

## 🏷️ Variants (with Barcode)

```bash
# Create
POST /api/v1/variants
Body: {
  tenantId, productId, sku, variantName, unit,
  barcodeValue, costPrice, sellingPrice,
  minStockThreshold, maxStockThreshold
}

# Get by ID
GET /api/v1/variants/{id}?tenantId={tenantId}

# Lookup by SKU
GET /api/v1/variants/sku/{sku}?tenantId={tenantId}

# 🔍 Barcode Scan (POS Integration)
GET /api/v1/variants/barcode/{barcode}?tenantId={tenantId}

# Update
PUT /api/v1/variants/{id}
Body: {tenantId, productId, sku, costPrice, sellingPrice...}

# ⚠️ Low Stock Alerts
GET /api/v1/variants/low-stock?tenantId={tenantId}
```

---

## 📊 Stock Management

```bash
# Create Incoming Stock (DRAFT)
POST /api/v1/stock/incoming
Body: {
  tenantId, storeId, supplierName,
  items: [{variantId, quantity, costPrice, sellingPrice}],
  notes
}
Response: {id, transactionNumber, totalCost, expectedProfit}

# List Transactions
GET /api/v1/stock/incoming?tenantId={id}&page=0&size=20

# Get by ID
GET /api/v1/stock/incoming/{id}?tenantId={tenantId}

# ✅ Confirm (Update Inventory)
POST /api/v1/stock/incoming/{id}/confirm?tenantId={tenantId}

# Cancel
POST /api/v1/stock/incoming/{id}/cancel?tenantId={tenantId}

# 📜 Ledger History (Audit Trail)
GET /api/v1/stock/ledger/variant/{variantId}?tenantId={id}&storeId={id}

# 📈 Current Stock Levels
GET /api/v1/stock/levels?tenantId={id}&storeId={id}
```

---

## 💡 Key Concepts

### Auto-Generated Slugs
```
"Electronics & Gadgets" → slug: "electronics-gadgets"
"iPhone 15 Pro" → slug: "iphone-15-pro"
```

### Profit Calculation
```
Per Item:
  totalCost = costPrice × quantity
  expectedRevenue = sellingPrice × quantity
  expectedProfit = expectedRevenue - totalCost

Transaction:
  totalCost = SUM(all items)
  expectedRevenue = SUM(all items)
  expectedProfit = expectedRevenue - totalCost
```

### Event Sourcing (Stock Ledger)
```
Stock Ledger = Append-only log
Current Stock = SUM(quantity_change) from all entries
Never update existing entries, only add new ones

Transaction Types:
- INCOMING (stock received)
- SALE (sold to customer)
- RETURN (customer return)
- ADJUSTMENT (manual correction)
```

### Transaction Workflow
```
CREATE → Status: DRAFT
         (can edit/cancel)
         ↓
CONFIRM → Status: CONFIRMED
          ✅ Creates ledger entries
          ✅ Updates inventory
          ✅ Records price snapshots
          ↓
COMPLETE → Inventory updated
```

---

## 🎯 Common Workflows

### Setup New Product
```
1. POST /categories (Electronics)
2. POST /products (iPhone 15 Pro)
3. POST /variants (256GB Titanium)
   → Returns variantId + initial price
4. GET /variants/barcode/{code}
   → Test barcode lookup
```

### Receive Stock
```
1. POST /stock/incoming
   → Returns transactionId, profit calculation
   → Status: DRAFT
2. POST /stock/incoming/{id}/confirm
   → Creates ledger entries
   → Status: CONFIRMED
3. GET /stock/levels
   → Verify stock updated
4. GET /stock/ledger/variant/{id}
   → View audit trail
```

### Check Low Stock
```
1. GET /variants/low-stock
   → Returns variants below threshold
2. For each variant:
   → GET /stock/ledger/variant/{id}
   → See movement history
3. Create replenishment:
   → POST /stock/incoming
```

---

## 📊 Response Status Codes

```
200 OK - Success (GET, PUT, DELETE)
201 Created - Resource created (POST)
400 Bad Request - Validation error
401 Unauthorized - Invalid/missing token
404 Not Found - Resource not found
500 Internal Server Error - Server error
```

---

## 🔑 Environment Variables (Postman)

```javascript
{{baseUrl}}        = http://localhost:8080
{{token}}          = JWT token (auto-set after login)
{{tenantId}}       = Tenant ID (auto-set or default: 1)
{{storeId}}        = Store ID (default: 1)
{{categoryId}}     = Last created category
{{productId}}      = Last created product
{{variantId}}      = Last created variant
{{transactionId}}  = Last created transaction
```

---

## 🗄️ Database

```bash
# Connection
jdbc:postgresql://localhost:5432/atozshop
Username: atozshop
Password: atozshop123

# Useful Queries
SELECT * FROM categories WHERE tenant_id = 1;
SELECT * FROM products WHERE tenant_id = 1;
SELECT * FROM product_variants WHERE tenant_id = 1;
SELECT * FROM stock_ledger WHERE tenant_id = 1 ORDER BY transaction_date DESC;
SELECT * FROM stock_transactions WHERE tenant_id = 1;

# Current Stock
SELECT
  v.sku,
  v.variant_name,
  SUM(sl.quantity_change) as current_stock,
  v.min_stock_threshold
FROM product_variants v
LEFT JOIN stock_ledger sl ON v.id = sl.variant_id
WHERE v.tenant_id = 1
GROUP BY v.id, v.sku, v.variant_name, v.min_stock_threshold;
```

---

## 🚀 Quick Test Sequence

```bash
# 1. Get token
POST /auth/login → Save {{token}}

# 2. Create hierarchy
POST /categories → Save {{categoryId}}
POST /products → Save {{productId}}
POST /variants → Save {{variantId}}

# 3. Test barcode
GET /variants/barcode/{code}

# 4. Add stock
POST /stock/incoming → Save {{transactionId}}
POST /stock/incoming/{{transactionId}}/confirm

# 5. Verify
GET /stock/levels
GET /stock/ledger/variant/{{variantId}}
GET /variants/low-stock
```

---

## 📱 POS Integration Points

```bash
# Barcode Scanning
GET /variants/barcode/{scanned_code}
→ Returns variant with current stock

# Stock Check
GET /stock/levels?tenantId={id}&storeId={id}
→ Returns all variants with stock levels

# Low Stock Warning
GET /variants/low-stock?tenantId={id}
→ Show alerts during sales
```

---

## 🔧 Troubleshooting

```bash
# Token expired?
→ POST /auth/login again

# 401 Unauthorized?
→ Check Authorization header: Bearer {{token}}

# Tenant ID required?
→ Add tenantId to body/query params

# Can't find resource?
→ Verify tenant isolation (wrong tenantId?)
→ Check resource exists in database
```

---

## 📖 Documentation

- **Postman Collection:** `AtoZShop_API_Collection.postman_collection.json`
- **Environment:** `AtoZShop_Environment.postman_environment.json`
- **Full Guide:** `POSTMAN_GUIDE.md`
- **Swagger UI:** http://localhost:8080/swagger-ui.html

---

**Total APIs: 32 endpoints** (Phase 0 + Phase 1)

Print this for quick reference! 📄

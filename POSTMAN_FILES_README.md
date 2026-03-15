# Postman Collection - Complete AtoZShop API

## 📦 Files Included

### 1. Collection File
**`AtoZShop_Complete_API_Collection.postman_collection.json`**
- 47+ API requests organized into folders
- Pre-configured authentication
- Sample request bodies
- Test scripts for auto-saving JWT tokens

### 2. Environment File
**`AtoZShop_Complete.postman_environment.json`**
- Pre-configured variables (base_url, tenant_id, etc.)
- Auto-populated JWT token
- Ready to use immediately

### 3. Documentation
**`POSTMAN_COMPLETE_GUIDE.md`**
- Complete usage guide
- Sample data examples
- Testing scenarios
- Troubleshooting tips

---

## 🚀 Quick Start (30 seconds)

1. **Import into Postman**
   - Open Postman
   - Click **Import**
   - Drag & drop both JSON files

2. **Select Environment**
   - Choose "AtoZShop - Complete Environment" from dropdown

3. **Run First Requests**
   - Phase 0 → Register New User
   - Phase 0 → Login (JWT auto-saved)
   - You're ready! 🎉

---

## 📁 Collection Structure

```
AtoZShop - Complete API Collection (Phase 0-2)
│
├── 📂 Phase 0 - Authentication & Authorization (3)
│   ├── Register New User
│   ├── Login (auto-saves JWT)
│   └── Get Home (test auth)
│
├── 📂 Phase 1 - Category Management (5)
│   ├── Create Category
│   ├── Get All Categories
│   ├── Get Category by ID
│   ├── Update Category
│   └── Delete Category
│
├── 📂 Phase 1 - Product Management (5)
│   ├── Create Product
│   ├── Get All Products
│   ├── Get Product by ID
│   ├── Search Products by Category
│   └── Update Product
│
├── 📂 Phase 1 - Product Variant Management (4)
│   ├── Create Product Variant
│   ├── Get Variant by ID
│   ├── Get Variant by Barcode ⚡ (POS)
│   └── Update Variant
│
├── 📂 Phase 1 - Stock Management (3)
│   ├── Record Incoming Stock
│   ├── Get Stock Ledger (event history)
│   └── Get Low Stock Alerts
│
├── 📂 Phase 2 - Customer Management (7)
│   ├── Create Customer
│   ├── Get All Customers
│   ├── Search Customers
│   ├── Get Customer by Phone ⚡ (POS)
│   ├── Get Customer Purchase History
│   ├── Update Customer
│   └── Delete Customer
│
├── 📂 Phase 2 - POS Billing (9)
│   ├── Create Bill (DRAFT)
│   ├── Get All Bills
│   ├── Get Bill by ID
│   ├── Get Bill by Number
│   ├── Add Item to Bill
│   ├── Update Bill Item
│   ├── Remove Item from Bill
│   ├── Confirm Bill (Deduct Stock) ⚡⚡⚡ CRITICAL
│   └── Cancel Bill
│
├── 📂 Phase 2 - Payment Processing (5)
│   ├── Process Payment (Cash)
│   ├── Process Payment (Card)
│   ├── Process Payment (UPI)
│   ├── Get Payments by Bill
│   └── Get Payments by Date Range
│
├── 📂 Phase 2 - Discount Management (5)
│   ├── Create Discount
│   ├── Get All Active Discounts
│   ├── Get Discount by Code
│   ├── Update Discount
│   └── Delete Discount
│
├── 📂 Phase 2 - Sales Reports (1)
│   └── Daily Sales Report
│
└── 📂 Complete POS Transaction Flow (7)
    ├── 1. Login
    ├── 2. Find/Create Customer
    ├── 3. Scan Product Barcode
    ├── 4. Create Bill
    ├── 5. Confirm Bill (Stock Deduct) ⚡
    ├── 6. Process Payment
    └── 7. Verify Stock Ledger
```

**Total: 47+ requests**

---

## 🎯 Key Features

### Auto-Authentication
- JWT token automatically saved on login
- Collection-level bearer token auth
- No manual token copy/paste needed

### Pre-Configured Variables
```
{{base_url}}     → http://localhost:8080
{{jwt_token}}    → Auto-populated
{{tenant_id}}    → 1
{{store_id}}     → 1
{{user_id}}      → 1
```

### Sample Data Ready
All requests include sample request bodies:
- ✅ Valid JSON format
- ✅ All required fields
- ✅ Realistic data examples

### Test Scripts
Login request includes test script to auto-save JWT:
```javascript
var jsonData = pm.response.json();
pm.collectionVariables.set("jwt_token", jsonData.token);
```

---

## 🔥 Most Important Requests

### For Development
1. **Login** - Get JWT token
2. **Create Product Variant** - Setup inventory
3. **Record Incoming Stock** - Add stock
4. **Create Customer** - Customer database
5. **Create Bill** - Start transaction
6. **Confirm Bill** - Deduct stock ⚡

### For Testing Phase 2
1. **Complete POS Transaction Flow** folder
   - Pre-configured 7-step workflow
   - Tests entire POS system
   - Verifies Phase 1 integration

---

## 💡 Pro Tips

### 1. Use the "Complete POS Transaction Flow" Folder
Run all 7 requests in sequence for complete end-to-end test.

### 2. Barcode Scanning
Use "Get Variant by Barcode" for quick POS lookup:
```
GET /api/v1/variants/barcode/8801234567890
```

### 3. Customer Quick Lookup
Use phone number for instant customer search:
```
GET /api/v1/customers/phone/9876543210
```

### 4. Stock Verification
After confirming bill, check stock ledger:
```
GET /api/v1/stock/ledger/variant/1
```
Look for SALE transactions with negative quantities.

### 5. Split Payments
Process multiple payments for single bill:
```
POST /api/v1/payments (₹50,000 cash)
POST /api/v1/payments (₹60,000 card)
```
Payment status auto-updates: UNPAID → PARTIAL → PAID

---

## 🧪 Testing Scenarios

### Scenario 1: First Sale (5 minutes)
1. Login
2. Create Category → Product → Variant
3. Add stock (50 units)
4. Create customer
5. Create bill (2 units)
6. Confirm bill
7. Process payment
8. ✅ Check stock = 48 units

### Scenario 2: Complete POS Flow (2 minutes)
1. Use "Complete POS Transaction Flow" folder
2. Run all 7 requests
3. ✅ Verify stock deducted

### Scenario 3: Multi-Payment (3 minutes)
1. Create bill for ₹100,000
2. Pay ₹40,000 cash → Status: PARTIAL
3. Pay ₹60,000 UPI → Status: PAID
4. ✅ Verify payment breakdown

---

## 📊 API Coverage

| Phase | Category | Requests | Coverage |
|-------|----------|----------|----------|
| 0 | Authentication | 3 | 100% |
| 1 | Categories | 5 | 100% |
| 1 | Products | 5 | 100% |
| 1 | Variants | 4 | 100% |
| 1 | Stock | 3 | 100% |
| 2 | Customers | 7 | 100% |
| 2 | Bills | 9 | 100% |
| 2 | Payments | 5 | 100% |
| 2 | Discounts | 5 | 100% |
| 2 | Reports | 1 | 100% |
| **Total** | **10 categories** | **47** | **100%** |

---

## 🔧 Environment Variables

| Variable | Default | Editable | Description |
|----------|---------|----------|-------------|
| base_url | localhost:8080 | ✅ | API server URL |
| jwt_token | (auto) | ❌ | Auto-populated on login |
| tenant_id | 1 | ✅ | Your organization ID |
| store_id | 1 | ✅ | Store location |
| user_id | 1 | ✅ | Current user |
| test_email | test@atozshop.com | ✅ | Test account |
| test_password | Test@123 | ✅ | Test password |

---

## 🎬 Getting Started Video (Text Guide)

### Step 1: Import (30 seconds)
1. Open Postman Desktop/Web
2. Click "Import" (top-left)
3. Select both JSON files or drag & drop
4. Click "Import"

### Step 2: Select Environment (5 seconds)
1. Look for environment dropdown (top-right)
2. Select "AtoZShop - Complete Environment"

### Step 3: First Request (10 seconds)
1. Open "Phase 0 - Authentication"
2. Click "Register New User"
3. Update email if needed
4. Click "Send"

### Step 4: Login (5 seconds)
1. Click "Login"
2. Click "Send"
3. JWT token automatically saved!

### Step 5: Test (10 seconds)
1. Open any Phase 1 or Phase 2 folder
2. Click any request
3. Click "Send"
4. ✅ It works!

**Total: 60 seconds to fully working API collection!**

---

## 🆘 Troubleshooting

### Problem: "Unauthorized" on every request
**Solution**: Run "Login" request first

### Problem: "Validation Failed"
**Solution**: Check request body has all required fields

### Problem: "Stock not available"
**Solution**:
1. Run "Record Incoming Stock" first
2. Then create/confirm bill

### Problem: Environment variables not working
**Solution**: Ensure "AtoZShop - Complete Environment" is selected

### Problem: JWT token not saving
**Solution**: Check Login request has test script enabled

---

## 📖 Related Documentation

- **`POSTMAN_COMPLETE_GUIDE.md`** - Detailed usage guide
- **`PHASE2_COMPLETE.md`** - Phase 2 implementation details
- **`PHASE2_TESTING_GUIDE.md`** - Manual testing scenarios
- **`API_QUICK_REFERENCE.md`** - Quick API reference

---

## 🎯 What You Can Test

### ✅ Authentication Flow
- User registration
- JWT login
- Token-based auth

### ✅ Inventory Management
- Product catalog
- Variants with barcodes
- Price management
- Event-sourced stock tracking

### ✅ POS Billing System
- Customer management
- Shopping cart
- Bill creation
- **Automatic stock deduction**
- Payment processing
- Split payments
- Discount application

### ✅ Integration Testing
- Phase 1 + Phase 2 integration
- Stock ledger SALE transactions
- Customer purchase history
- Sales reporting

---

## 🚀 Next Steps

1. **Import Collection** (done in 30 seconds)
2. **Run "Complete POS Transaction Flow"** (2 minutes)
3. **Explore Individual Requests** (customize as needed)
4. **Create Your Own Test Scenarios**
5. **Share with Team**

---

## 📝 Version History

**v2.0.0** (Feb 28, 2026)
- ✅ Complete Phase 0, 1, 2 coverage
- ✅ 47+ requests organized
- ✅ Auto JWT token handling
- ✅ Sample data included
- ✅ Complete POS workflow
- ✅ Environment variables
- ✅ Test scripts

---

## 👥 Usage

**For Developers**: Test APIs during development
**For QA**: Comprehensive test coverage
**For Demo**: Complete POS workflow demo
**For Documentation**: API examples and formats

---

**Ready to Use!** 🎉

Import the collection and start testing your complete AtoZShop system in under 60 seconds!

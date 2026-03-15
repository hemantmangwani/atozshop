# Frontend Testing Guide - Phase 3

## Current Status

✅ **Frontend**: Running on http://localhost:5173
⚠️ **Backend**: Running on http://localhost:8080 (returns HTTP 500 on login endpoint)

---

## Step 1: Verify Backend is Running

### Check Backend Status

```bash
# Navigate to backend directory
cd /Users/hemant.mangwani/gitproject/20jan/atozshop

# Start the backend if not running
./mvnw spring-boot:run
```

Wait for: `Started AtozshopApplication in X seconds`

### Test Backend Health

```bash
# Test homepage endpoint (should work)
curl http://localhost:8080/api/v1/home

# Test login endpoint (should return 400 for missing credentials, not 500)
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test"}'
```

---

## Step 2: Test Login Flow

### Frontend Testing Steps

1. **Open Browser**: Navigate to http://localhost:5173

2. **You Should See**:
   - Redirect to `/login` page automatically (not authenticated)
   - Clean login form with username and password fields
   - Demo credentials shown at bottom

3. **Try Logging In**:
   - Use credentials from your Phase 0 setup
   - **Default Admin**:
     ```
     Username: admin@atozshop.com
     Password: admin123
     ```

4. **Expected Behavior**:
   - ✅ **Success**: Redirected to home page (`/`)
   - ✅ JWT token stored in localStorage
   - ✅ Header shows user info and cart icon
   - ❌ **Failure**: Error message displayed

### Check Browser Console

Open DevTools (F12) → Console tab to see:
- API request to `/api/v1/auth/login`
- Response status (200 = success, 401 = bad credentials, 500 = server error)
- Any error messages

### Check Network Tab

DevTools → Network tab:
1. Clear logs
2. Click "Sign in"
3. Look for `login` request
4. Click on it to see:
   - **Request Headers**: Should include `Content-Type: application/json`
   - **Request Payload**: Your username/password
   - **Response**: Token or error message

---

## Step 3: Test Product Catalog

After successful login:

### Home Page (`/`)

**What You Should See**:
- ✅ Header with AtoZShop logo
- ✅ Search bar
- ✅ Cart icon (with count badge if items in cart)
- ✅ User menu (My Orders, Logout, Admin Dashboard if admin)
- ✅ Product grid

**Product Cards Show**:
- Product name
- Brand (if available)
- Variant name
- Stock status badge:
  - 🟢 "In Stock" (green)
  - 🟡 "Only X left" (yellow for low stock)
  - 🔴 "Out of Stock" (red)
- Price (with MRP and discount if applicable)
- "Add to Cart" button

### Test Add to Cart

1. Click "Add to Cart" on any in-stock product
2. **Expected**:
   - Alert: "Added to cart!"
   - Cart icon badge updates (shows count)
   - Button shows "Adding..." while processing

3. **Check Browser DevTools**:
   - Network tab should show request to check stock availability
   - localStorage should have `atozshop_cart` key

### Test Stock Validation

1. Try adding out-of-stock product
2. **Expected**: Button is disabled

---

## Step 4: Test Cart Functionality

### View Cart

1. Click cart icon in header
2. Navigate to `/cart`
3. **Expected** (not yet implemented):
   - List of cart items
   - Quantity controls
   - Remove item button
   - Subtotal
   - Checkout button

**Current Status**: Cart page not yet created (Task #23 pending)

---

## Troubleshooting

### Issue: Login Returns 500 Error

**Cause**: Backend database connection or configuration issue

**Solution**:
1. Check backend console for error stack trace
2. Verify PostgreSQL is running:
   ```bash
   psql -U postgres -d atozshop
   ```
3. Check `application.properties`:
   ```
   spring.datasource.url=jdbc:postgresql://localhost:5432/atozshop
   spring.datasource.username=postgres
   spring.datasource.password=your_password
   ```

### Issue: CORS Error

**Symptom**: Browser console shows:
```
Access to XMLHttpRequest at 'http://localhost:8080/api/v1/auth/login'
from origin 'http://localhost:5173' has been blocked by CORS policy
```

**Solution**: Add CORS configuration to backend (should already be there from Phase 0)

### Issue: Products Don't Load

**Possible Causes**:
1. **Not authenticated**: Login first
2. **No products in database**: Add products via Phase 1 APIs
3. **Wrong tenant/store ID**: Check localStorage values

**Check**:
```javascript
// In browser console
localStorage.getItem('atozshop_tenant_id')  // Should be '1'
localStorage.getItem('atozshop_store_id')   // Should be '1'
```

### Issue: "Network Error" Message

**Cause**: Backend not running

**Solution**:
```bash
cd /Users/hemant.mangwani/gitproject/20jan/atozshop
./mvnw spring-boot:run
```

---

## API Endpoints Being Called

### Auth
- `POST /api/v1/auth/login` - Login

### Products
- `GET /api/v1/public/products?tenantId=1&storeId=1` - Get all products

### Stock Check (when adding to cart)
- `GET /api/v1/public/products/variant/{variantId}/availability?tenantId=1&storeId=1`

---

## Expected API Responses

### Successful Login
```json
{
  "token": "eyJhbGc...",
  "userId": 2,
  "username": "admin@atozshop.com",
  "role": "ADMIN",
  "tenantId": 1
}
```

### Products List
```json
[
  {
    "id": 1,
    "sku": "PROD-001-VARIANT-001",
    "name": "Sample Product",
    "description": "Product description",
    "categoryName": "Electronics",
    "brandName": "Samsung",
    "defaultVariantId": 1,
    "defaultVariantName": "500g",
    "sellingPrice": 99.99,
    "mrp": 129.99,
    "discountPercent": 23.08,
    "availableStock": 50,
    "stockStatus": "In Stock",
    "isAvailable": true,
    "variants": [...]
  }
]
```

---

## Test Checklist

### ✅ Phase 1: Authentication
- [ ] Login page displays correctly
- [ ] Can login with valid credentials
- [ ] Invalid credentials show error
- [ ] Token stored in localStorage
- [ ] Redirected to home after login
- [ ] Logout works (clears token, redirects to login)

### ✅ Phase 2: Product Catalog
- [ ] Home page loads with product grid
- [ ] Products display with correct info
- [ ] Stock status badges show correctly
- [ ] Prices display with MRP and discount
- [ ] Add to cart button works
- [ ] Cart count updates in header
- [ ] Stock validation prevents adding out-of-stock items

### 🔄 Phase 3: Shopping Cart (Pending - Task #23)
- [ ] Cart page displays
- [ ] Cart items list correctly
- [ ] Can update quantity
- [ ] Can remove items
- [ ] Subtotal calculates correctly
- [ ] Cart persists on page refresh

### 🔄 Phase 4: Checkout (Pending - Task #24)
- [ ] Can add delivery address
- [ ] Can select delivery slot
- [ ] Can choose payment method
- [ ] Can place order
- [ ] Order confirmation shown

### 🔄 Phase 5: Order Tracking (Pending - Task #25)
- [ ] Can view order history
- [ ] Can see order details
- [ ] Order status timeline displays
- [ ] Can cancel order

### 🔄 Phase 6: Admin Dashboard (Pending - Task #26)
- [ ] Admin can access dashboard
- [ ] Can view all orders
- [ ] Can filter orders
- [ ] Can accept orders
- [ ] Can pack/dispatch/deliver orders

---

## Current Implementation Status

### ✅ Completed
1. **Project Setup** - React + TypeScript + Vite + TailwindCSS
2. **API Services** - Complete type-safe API layer
3. **Authentication** - Login, logout, protected routes
4. **Product Catalog** - Home page with product grid, cards
5. **Layout** - Header, footer, main layout
6. **Cart Context** - Add to cart, stock validation

### 🚧 In Progress
- **Product Detail Page** (Task #22)
- **Search Functionality** (Task #22)

### 📋 Pending
- **Cart Page** (Task #23)
- **Checkout Flow** (Task #24)
- **Order Tracking** (Task #25)
- **Admin Dashboard** (Task #26)
- **Styling & Polish** (Task #28)

---

## Quick Test Commands

### Test Backend Endpoints

```bash
# Test login (use your actual credentials)
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin@atozshop.com","password":"admin123"}'

# Test products (replace with actual token)
curl http://localhost:8080/api/v1/public/products?tenantId=1&storeId=1 \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Check Frontend Build

```bash
cd /Users/hemant.mangwani/gitproject/20jan/atozshop/atozshop-frontend
npm run build
```

### Start Development

```bash
# Terminal 1: Backend
cd /Users/hemant.mangwani/gitproject/20jan/atozshop
./mvnw spring-boot:run

# Terminal 2: Frontend (already running)
cd /Users/hemant.mangwani/gitproject/20jan/atozshop/atozshop-frontend
npm run dev
```

---

## Next Steps

1. ✅ **Test Login** - Verify authentication works
2. ✅ **Test Product Display** - Ensure products load
3. 🔄 **Build Cart Page** - Task #23
4. 🔄 **Build Checkout** - Task #24
5. 🔄 **Build Order Tracking** - Task #25
6. 🔄 **Build Admin Dashboard** - Task #26

---

**Last Updated**: March 1, 2026
**Frontend Status**: ✅ Product Catalog Ready for Testing
**Backend Status**: ⚠️ Needs Verification

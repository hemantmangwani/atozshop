# Frontend Manual Testing Guide

## ✅ Backend API Test Results

**Status:** ALL SYSTEMS OPERATIONAL

### API Test Summary
- ✅ Login API working
- ✅ Products API returning 3 products
- ✅ All products have stock (50 units each)
- ✅ All products show "In Stock" status
- ✅ All "Add to Cart" buttons should be enabled

---

## 🌐 Manual Testing Steps

### Step 1: Open Frontend
1. Open your browser
2. Go to: **http://localhost:5173**
3. You should see the AtoZShop homepage

### Step 2: Login
1. Click "Login" or "Sign In"
2. Enter credentials:
   - **Email:** `customer@atozshop.com`
   - **Password:** `admin123`
3. Click "Login" button
4. ✅ **Expected:** Successfully logged in, redirected to homepage/products

### Step 3: Check Products Page

You should see **3 products** displayed:

#### Product 1: iPhone 15 Pro
- **Name:** iPhone 15 Pro
- **Variant:** Natural Titanium 256GB
- **Price:** ₹134,900
- **MRP:** ₹155,135 (strikethrough)
- **Discount:** 13.04% OFF badge
- **Stock:** "50 units available" or "In Stock"
- **Add to Cart Button:** ENABLED (clickable)

#### Product 2: Samsung Galaxy S23
- **Name:** Samsung Galaxy S23
- **Variant:** 128GB Black (default)
- **Price:** ₹55,000
- **MRP:** ₹59,999 (strikethrough)
- **Discount:** 8.33% OFF badge
- **Stock:** "50 units available" or "In Stock"
- **Variants:** Should show 2 variants when clicked
  - 128GB Black: ₹55,000
  - Black 128GB: ₹74,999
- **Add to Cart Button:** ENABLED (clickable)

#### Product 3: Samsung Galaxy S23 (256GB)
- **Name:** Samsung Galaxy S23
- **Variant:** 256GB Black
- **Price:** ₹60,000
- **MRP:** ₹64,999 (strikethrough)
- **Discount:** 7.69% OFF badge
- **Stock:** "50 units available" or "In Stock"
- **Add to Cart Button:** ENABLED (clickable)

---

## ❌ What You Should NOT See

- ❌ "Out of Stock" message
- ❌ "0 items available"
- ❌ Price showing as ₹0.00
- ❌ Disabled/grayed out "Add to Cart" buttons
- ❌ Error messages about stock availability

---

## 🛒 Test Add to Cart (Optional)

### Step 4: Add Product to Cart
1. Click "Add to Cart" on any product (e.g., iPhone 15 Pro)
2. ✅ **Expected:**
   - Success message appears
   - Cart icon shows count (1)
   - No errors

### Step 5: View Cart
1. Click on cart icon
2. ✅ **Expected:**
   - Product appears in cart
   - Correct price shown
   - Quantity controls working
   - Total price calculated correctly

### Step 6: Update Quantity
1. Try increasing quantity to 2
2. ✅ **Expected:**
   - Quantity updates
   - Total price updates
   - No stock errors (since we have 50 units)

---

## 📸 What to Look For

### ✅ Success Indicators
- Product cards with images
- Clear product names
- Visible prices (₹55,000 - ₹134,900)
- MRP with strikethrough showing original price
- Red/green discount badges (7-13% OFF)
- Stock status: "In Stock" with green indicator
- Stock count: "50 units available"
- "Add to Cart" buttons in primary color (blue/green)
- Buttons are clickable (not grayed out)

### ❌ Failure Indicators
- Products show "Out of Stock"
- Stock count shows "0 units"
- Prices show ₹0.00
- "Add to Cart" buttons are disabled
- Gray/faded product cards
- Error messages

---

## 🔧 Troubleshooting

### If Products Still Show "0 Stock"

1. **Check Browser Console:**
   - Press F12 to open DevTools
   - Go to Console tab
   - Look for any red errors
   - Check Network tab for API calls

2. **Verify API Request:**
   - In Network tab, find request to `/api/v1/public/products`
   - Check Request URL - should include `tenantId=1&storeId=1`
   - Check Response - should show products with stock

3. **Clear Browser Cache:**
   - Press Ctrl+Shift+R (or Cmd+Shift+R on Mac)
   - Or clear browser cache completely
   - Refresh page

4. **Check localStorage:**
   - In DevTools Console, type: `localStorage.getItem('atozshop_tenant_id')`
   - Should return "1"
   - Type: `localStorage.getItem('atozshop_store_id')`
   - Should return "1" or "3"

### If Login Fails

1. **Verify Credentials:**
   - Email: `customer@atozshop.com` (exact spelling)
   - Password: `admin123` (NOT customer123)

2. **Check Backend:**
   - Backend should be running on http://localhost:8080
   - Test: `curl http://localhost:8080/api/v1/auth/login` should not give "connection refused"

3. **Check Browser Console:**
   - Look for CORS errors
   - Look for 401 Unauthorized errors

---

## 📊 Expected API Calls (Check in Network Tab)

### On Login:
```
POST http://localhost:8080/api/v1/auth/login
Request Body: {"email":"customer@atozshop.com","password":"admin123"}
Response: 200 OK with token
```

### On Products Page:
```
GET http://localhost:8080/api/v1/public/products?tenantId=1&storeId=1
Headers: Authorization: Bearer <token>
Response: 200 OK with array of 3 products
```

### Sample Product Response:
```json
{
  "id": 2,
  "name": "iPhone 15 Pro",
  "sellingPrice": 134900,
  "mrp": 155135,
  "availableStock": 50,
  "stockStatus": "In Stock",
  "isAvailable": true,
  "discountPercent": 13.04
}
```

---

## ✅ Success Criteria Checklist

- [ ] Frontend loads at http://localhost:5173
- [ ] Login with customer@atozshop.com / admin123 works
- [ ] 3 products are displayed
- [ ] All products show prices (₹55,000 - ₹134,900)
- [ ] All products show "In Stock" or "50 units available"
- [ ] All products have discount badges (7-13% OFF)
- [ ] All "Add to Cart" buttons are enabled (not grayed out)
- [ ] Clicking "Add to Cart" works without errors
- [ ] Cart icon shows item count after adding
- [ ] No "Out of Stock" or "0 items" messages

---

## 🎯 If Everything Works

You should see:
- ✅ 3 products fully functional
- ✅ All prices visible and correct
- ✅ All products showing 50 units in stock
- ✅ Add to cart functionality working
- ✅ No errors in browser console

**Next Steps:**
- Test checkout flow
- Test order placement
- Test order tracking
- Admin dashboard (Task #26)

---

## 📝 Report Results

After testing, please confirm:

1. **Products Display:** Do all 3 products show stock?
2. **Stock Count:** Does it show "50 units" or "In Stock"?
3. **Add to Cart:** Are the buttons enabled and working?
4. **Any Errors:** Any error messages in UI or console?

---

**Test Date:** March 2, 2026
**Backend Status:** ✅ Running on port 8080
**Frontend Status:** ✅ Running on port 5173
**Database Status:** ✅ All products have stock
**API Test Status:** ✅ All endpoints returning correct data

# Phase 1 & Phase 2 Admin UI - Testing Checklist

**Date:** March 2, 2026
**Status:** Ready for Testing
**Total Pages:** 19 admin pages

---

## 🎯 Testing Objectives

1. Verify all pages load without errors
2. Test all CRUD operations
3. Validate form submissions
4. Check data flow between pages
5. Test error handling
6. Verify responsive design
7. Test navigation and routing

---

## ✅ Pre-Testing Setup

- [ ] Backend server running on port 8080
- [ ] Frontend server running on port 5173
- [ ] Database seeded with test data
- [ ] Admin user credentials ready
- [ ] Browser DevTools console open
- [ ] Network tab monitoring enabled

---

## 📋 Page-by-Page Testing

### 1. Admin Dashboard (`/admin`)

**Load Test:**
- [ ] Page loads without errors
- [ ] All 10 action cards displayed
- [ ] Stats cards show correct data
- [ ] Recent orders section visible
- [ ] All icons render correctly

**Navigation Test:**
- [ ] Click each action card
- [ ] Verify correct page opens
- [ ] Back navigation works
- [ ] No broken links

**Expected Result:** Dashboard displays correctly, all navigation works

---

### 2. POS Billing (`/admin/pos`)

**Load Test:**
- [ ] Page loads without errors
- [ ] Product search bar visible
- [ ] Cart section empty initially
- [ ] Payment methods visible

**Product Search:**
- [ ] Search by product name works
- [ ] Search by SKU works
- [ ] Barcode search works (if implemented)
- [ ] Results display correctly

**Cart Operations:**
- [ ] Add product to cart
- [ ] Increase quantity
- [ ] Decrease quantity
- [ ] Remove item from cart
- [ ] Cart total calculates correctly

**Customer Selection:**
- [ ] Search customer by phone
- [ ] Search customer by name
- [ ] Select existing customer
- [ ] Create walk-in bill (no customer)

**Payment Processing:**
- [ ] Select payment method (Cash)
- [ ] Enter payment amount
- [ ] Change calculation correct
- [ ] Select payment method (Card/UPI)
- [ ] Process split payment

**Bill Confirmation:**
- [ ] Confirm bill creates bill record
- [ ] Stock automatically deducted
- [ ] Receipt displayed/printable
- [ ] Redirect to success/bills page

**Error Handling:**
- [ ] Insufficient stock error shown
- [ ] Validation errors displayed
- [ ] Network error handling

**Expected Result:** Complete POS workflow successful

---

### 3. Customers List (`/admin/customers`)

**Load Test:**
- [ ] Page loads without errors
- [ ] Customer table displays
- [ ] Stats cards show correct counts
- [ ] Search bar visible

**List Operations:**
- [ ] Search by name works
- [ ] Search by phone works
- [ ] Search by customer code works
- [ ] Active/inactive filter works

**Actions:**
- [ ] View customer details (eye icon)
- [ ] Edit customer (pencil icon)
- [ ] Delete customer (trash icon)
- [ ] Delete confirmation dialog shown

**Expected Result:** All customers list correctly, actions work

---

### 4. Create Customer (`/admin/customers/new`)

**Load Test:**
- [ ] Form loads without errors
- [ ] All fields visible
- [ ] Default values set correctly

**Form Validation:**
- [ ] Name required validation
- [ ] Phone required validation
- [ ] Phone format validation (10 digits)
- [ ] Email format validation
- [ ] Postal code format validation
- [ ] GSTIN format validation

**Submit:**
- [ ] Valid form submits successfully
- [ ] Customer code auto-generated
- [ ] Success toast displayed
- [ ] Redirect to customers list
- [ ] New customer appears in list

**Error Handling:**
- [ ] Duplicate phone number error
- [ ] Network error handling
- [ ] Validation errors inline

**Expected Result:** Customer creation successful

---

### 5. Edit Customer (`/admin/customers/:id/edit`)

**Load Test:**
- [ ] Form pre-filled with customer data
- [ ] Customer code displayed (read-only)
- [ ] All fields editable

**Update:**
- [ ] Update name
- [ ] Update phone
- [ ] Update address
- [ ] Toggle active status
- [ ] Save changes successful
- [ ] Redirect to customers list
- [ ] Changes reflected in list

**Expected Result:** Customer update successful

---

### 6. Customer Detail (`/admin/customers/:id`)

**Load Test:**
- [ ] Customer info displayed
- [ ] Stats cards show totals
- [ ] Purchase history table visible

**Data Display:**
- [ ] Customer code shown
- [ ] Contact info correct
- [ ] Address formatted correctly
- [ ] Total purchases accurate
- [ ] Loyalty points displayed
- [ ] Purchase history complete

**Actions:**
- [ ] Edit button navigates to edit page
- [ ] View bill links work

**Expected Result:** All customer data displayed correctly

---

### 7. Stock Dashboard (`/admin/stock`)

**Load Test:**
- [ ] Dashboard loads without errors
- [ ] Stats cards show totals
- [ ] Low stock alerts visible
- [ ] Current stock table displayed

**Stats:**
- [ ] Total stock value calculated
- [ ] Total units count correct
- [ ] Low stock count accurate
- [ ] Critical stock count shown

**Alerts:**
- [ ] Low stock items highlighted
- [ ] Critical items (< 50% reorder) in red
- [ ] Alert severity badges correct

**Actions:**
- [ ] Add incoming stock button works
- [ ] View ledger link works

**Expected Result:** Stock overview displayed accurately

---

### 8. Add Incoming Stock (`/admin/stock/add-incoming`)

**Load Test:**
- [ ] Form loads without errors
- [ ] Product search works
- [ ] Items list empty initially

**Add Items:**
- [ ] Search product by name
- [ ] Search by SKU
- [ ] Add variant to list
- [ ] Cannot add duplicate variant
- [ ] Enter quantity
- [ ] Enter cost price
- [ ] Enter selling price
- [ ] Total calculated correctly

**Submit:**
- [ ] Valid form submits
- [ ] Stock ledger entries created
- [ ] Current stock updated
- [ ] Success notification shown
- [ ] Redirect to stock dashboard

**Error Handling:**
- [ ] Quantity validation (min 1)
- [ ] Price validation (min 0)
- [ ] Network error handling

**Expected Result:** Stock receipt successful

---

### 9. Stock Ledger (`/admin/stock/ledger`)

**Load Test:**
- [ ] Ledger table loads
- [ ] Date range filters visible
- [ ] Transaction type filter works

**Filters:**
- [ ] Date range filtering
- [ ] Filter by INCOMING
- [ ] Filter by SALE
- [ ] Filter by ADJUSTMENT
- [ ] Filter by RETURN

**Data Display:**
- [ ] All transactions listed
- [ ] Quantity change shown (+/-)
- [ ] Balance after correct
- [ ] Transaction types color-coded
- [ ] Reference IDs displayed

**Stats:**
- [ ] Total transactions count
- [ ] Stock in total
- [ ] Stock out total
- [ ] Net change calculated

**Expected Result:** Complete audit trail displayed

---

### 10. Products List (`/admin/products`)

**Load Test:**
- [ ] Products grid/list loads
- [ ] View toggle (grid/list) works
- [ ] Search bar functional
- [ ] Category filter works

**Display:**
- [ ] Grid view shows product cards
- [ ] List view shows table
- [ ] Product count accurate
- [ ] Variant count per product shown
- [ ] Active status badges correct

**Actions:**
- [ ] Add product button works
- [ ] View details (eye icon)
- [ ] Edit product (pencil icon)
- [ ] Delete product (trash icon)

**Expected Result:** All products displayed correctly

---

### 11. Create Product (`/admin/products/new`)

**Load Test:**
- [ ] Form loads without errors
- [ ] Category dropdown populated
- [ ] Variant section visible

**Form:**
- [ ] Enter product name
- [ ] Select category
- [ ] Enter SKU
- [ ] Enter description
- [ ] Set active status

**Variants:**
- [ ] Add first variant (default)
- [ ] Add additional variants
- [ ] Enter variant details (name, SKU, prices)
- [ ] Remove variant
- [ ] Cannot remove last variant

**Validation:**
- [ ] Product name required
- [ ] Category required
- [ ] SKU required
- [ ] Variant name required
- [ ] Variant SKU required
- [ ] Prices must be positive

**Submit:**
- [ ] Product created successfully
- [ ] All variants created
- [ ] Redirect to products list
- [ ] New product visible

**Expected Result:** Product with variants created

---

### 12. Edit Product (`/admin/products/:id/edit`)

**Load Test:**
- [ ] Form pre-filled with product data
- [ ] Existing variants shown (read-only)
- [ ] Note about variant management shown

**Update:**
- [ ] Update product name
- [ ] Change category
- [ ] Update SKU
- [ ] Update description
- [ ] Toggle active status
- [ ] Save changes successful

**Expected Result:** Product updated successfully

---

### 13. Categories (`/admin/categories`)

**Load Test:**
- [ ] Category hierarchy displayed
- [ ] Stats cards show counts
- [ ] Add category button works

**Hierarchy:**
- [ ] Root categories listed
- [ ] Subcategories expandable
- [ ] Expand/collapse works
- [ ] Indentation correct

**Actions:**
- [ ] Add category modal opens
- [ ] Edit category modal opens
- [ ] Delete category works
- [ ] Active/inactive toggle visible

**Expected Result:** Category tree displayed correctly

---

### 14. Category Form Modal

**Create:**
- [ ] Modal opens
- [ ] Form empty for new category
- [ ] Parent category dropdown
- [ ] Name required validation
- [ ] Submit creates category
- [ ] Modal closes on success
- [ ] List refreshes

**Edit:**
- [ ] Modal pre-filled with data
- [ ] Parent can be changed
- [ ] Update successful
- [ ] Changes reflected in list

**Expected Result:** Category CRUD successful

---

### 15. Suppliers (`/admin/suppliers`)

**Load Test:**
- [ ] Suppliers table loads
- [ ] Stats cards show counts
- [ ] Search bar functional

**List:**
- [ ] All suppliers displayed
- [ ] Search by name works
- [ ] Search by code works
- [ ] Supplier type badges correct
- [ ] Active/inactive status shown

**Actions:**
- [ ] Add supplier modal opens
- [ ] Edit supplier works
- [ ] Delete supplier works

**Expected Result:** Suppliers list correctly

---

### 16. Supplier Form Modal

**Create:**
- [ ] All form fields visible
- [ ] Supplier type dropdown
- [ ] Bank details section
- [ ] Submit creates supplier
- [ ] Supplier code auto-generated

**Edit:**
- [ ] Form pre-filled
- [ ] All fields editable
- [ ] Update successful

**Validation:**
- [ ] Name required
- [ ] Email format
- [ ] GST/PAN format (if entered)

**Expected Result:** Supplier CRUD successful

---

### 17. Bills History (`/admin/bills`)

**Load Test:**
- [ ] Bills table loads
- [ ] Stats cards show totals
- [ ] Filters visible

**Filters:**
- [ ] Search by bill number
- [ ] Search by customer
- [ ] Filter by status (Draft/Confirmed/Cancelled)
- [ ] Filter by payment status

**Display:**
- [ ] All bills listed
- [ ] Bill details shown (number, date, customer, amount)
- [ ] Status badges color-coded
- [ ] Payment status badges correct

**Actions:**
- [ ] View bill detail (eye icon)
- [ ] Create new bill (redirects to POS)

**Expected Result:** All bills displayed with filters working

---

### 18. Bill Detail (`/admin/bills/:id`)

**Load Test:**
- [ ] Bill information displayed
- [ ] Customer info shown
- [ ] Items table visible
- [ ] Payments list shown

**Data:**
- [ ] Bill number and date
- [ ] Status and payment status
- [ ] Customer details
- [ ] All items listed
- [ ] Item prices correct
- [ ] Discounts applied shown
- [ ] Total calculations accurate
- [ ] Payment breakdown correct

**Actions:**
- [ ] Print button works
- [ ] Download receipt works
- [ ] Back navigation works

**Expected Result:** Complete bill details displayed

---

### 19. Discounts (`/admin/discounts`)

**Load Test:**
- [ ] Discounts table loads
- [ ] Stats cards show counts
- [ ] Add discount button works

**List:**
- [ ] All discounts displayed
- [ ] Discount codes shown
- [ ] Type and value correct
- [ ] Validity dates shown
- [ ] Active/inactive status

**Actions:**
- [ ] Add discount modal opens
- [ ] Edit discount works
- [ ] Delete discount works
- [ ] Toggle active status works

**Expected Result:** Discounts list correctly

---

### 20. Discount Form Modal

**Create:**
- [ ] Discount type dropdown (Percentage/Fixed)
- [ ] Value input
- [ ] Applicable on dropdown
- [ ] Min purchase amount
- [ ] Max discount amount
- [ ] Validity dates
- [ ] Submit creates discount

**Validation:**
- [ ] Name required
- [ ] Code required
- [ ] Value required and positive
- [ ] Dates validation

**Expected Result:** Discount CRUD successful

---

### 21. Sales Reports (`/admin/reports`)

**Load Test:**
- [ ] Reports page loads
- [ ] Date range filters work
- [ ] All stats cards visible

**Today's Summary:**
- [ ] Total sales shown
- [ ] Transaction count
- [ ] Items sold
- [ ] Average order value

**Period Summary:**
- [ ] Total revenue
- [ ] Total transactions
- [ ] Average transaction value

**Charts/Breakdowns:**
- [ ] Payment methods breakdown
- [ ] Top selling products table
- [ ] Top customers table

**Filters:**
- [ ] Date range selection
- [ ] Data updates on filter change

**Expected Result:** All reports display correctly

---

## 🔄 Integration Testing

### Workflow 1: Complete Product Setup
1. [ ] Create category
2. [ ] Create product with variants
3. [ ] Add incoming stock
4. [ ] Verify stock dashboard shows new stock
5. [ ] Verify product appears in POS search

### Workflow 2: POS Sale
1. [ ] Create customer
2. [ ] Open POS
3. [ ] Search and add product
4. [ ] Select customer
5. [ ] Apply discount
6. [ ] Process payment
7. [ ] Confirm bill
8. [ ] Verify stock deducted
9. [ ] Verify bill in history
10. [ ] Verify customer purchase history updated

### Workflow 3: Inventory Management
1. [ ] Check stock dashboard
2. [ ] Identify low stock item
3. [ ] Add incoming stock
4. [ ] Verify stock ledger entry
5. [ ] Verify current stock updated
6. [ ] Verify low stock alert cleared

### Workflow 4: Sales Analysis
1. [ ] Process multiple sales
2. [ ] Open sales reports
3. [ ] Check today's summary
4. [ ] Check period summary
5. [ ] Verify top products
6. [ ] Verify payment breakdown

---

## 📱 Responsive Design Testing

### Mobile (< 768px)
- [ ] All pages load on mobile
- [ ] Navigation accessible
- [ ] Tables scroll horizontally
- [ ] Forms usable
- [ ] Buttons touchable
- [ ] Modal fits screen

### Tablet (768px - 1024px)
- [ ] Grid layouts adjust
- [ ] Tables readable
- [ ] Stats cards 2-column

### Desktop (> 1024px)
- [ ] Full layout displayed
- [ ] All features accessible
- [ ] Optimal spacing

---

## 🚨 Error Handling Testing

### Network Errors
- [ ] Offline mode handling
- [ ] Timeout errors
- [ ] 500 server errors
- [ ] 404 not found

### Validation Errors
- [ ] Required field errors
- [ ] Format validation errors
- [ ] Business logic errors
- [ ] Duplicate entry errors

### Permission Errors
- [ ] Non-admin access blocked
- [ ] Tenant isolation verified
- [ ] Store isolation verified

---

## ⚡ Performance Testing

- [ ] Page load time < 2 seconds
- [ ] Search results < 500ms
- [ ] Form submission < 1 second
- [ ] Large lists paginate/scroll smoothly
- [ ] No memory leaks (check DevTools)

---

## 🔒 Security Testing

- [ ] Admin routes require authentication
- [ ] Tenant ID always included in requests
- [ ] Store ID validated
- [ ] No sensitive data in URLs
- [ ] XSS protection verified
- [ ] CSRF tokens if applicable

---

## 🐛 Known Issues / Edge Cases

Document any issues found:

1. **Issue:** _______________
   - **Steps to reproduce:** _______________
   - **Expected:** _______________
   - **Actual:** _______________
   - **Severity:** Low / Medium / High / Critical

2. **Issue:** _______________
   - **Steps to reproduce:** _______________
   - **Expected:** _______________
   - **Actual:** _______________
   - **Severity:** Low / Medium / High / Critical

---

## ✅ Sign-Off

**Tester:** _______________
**Date:** _______________
**Overall Status:** Pass / Fail / Pass with Issues

**Summary:**
_______________________________________________
_______________________________________________
_______________________________________________

**Recommendations:**
_______________________________________________
_______________________________________________
_______________________________________________

---

## 📊 Test Coverage Summary

- **Total Pages:** 19
- **Pages Tested:** _____ / 19
- **Tests Passed:** _____ / _____
- **Tests Failed:** _____ / _____
- **Bugs Found:** _____
- **Critical Bugs:** _____

**Ready for Production:** ☐ Yes ☐ No ☐ Conditional

---

**Last Updated:** March 2, 2026
**Version:** 1.0

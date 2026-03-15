# Admin Pages - Quick Reference Guide

**Quick lookup for all admin pages, routes, and features**

---

## 🗺️ Route Reference

| Page | Route | Description |
|------|-------|-------------|
| **Dashboard** | `/admin` | Main admin landing page |
| **Orders** | `/admin/orders` | Manage online orders (Phase 3) |
| **POS Billing** | `/admin/pos` | In-store sales terminal |
| **Customers List** | `/admin/customers` | All customers |
| **Add Customer** | `/admin/customers/new` | Create new customer |
| **Customer Detail** | `/admin/customers/:id` | View customer profile |
| **Edit Customer** | `/admin/customers/:id/edit` | Update customer info |
| **Stock Dashboard** | `/admin/stock` | Stock overview & alerts |
| **Add Stock** | `/admin/stock/add-incoming` | Receive new stock |
| **Stock Ledger** | `/admin/stock/ledger` | Movement history |
| **Products List** | `/admin/products` | All products |
| **Add Product** | `/admin/products/new` | Create product + variants |
| **Edit Product** | `/admin/products/:id/edit` | Update product |
| **Categories** | `/admin/categories` | Category hierarchy |
| **Bills History** | `/admin/bills` | All POS bills |
| **Bill Detail** | `/admin/bills/:id` | View bill + receipt |
| **Discounts** | `/admin/discounts` | Manage offers |
| **Sales Reports** | `/admin/reports` | Analytics dashboard |

---

## 📦 Feature Matrix

| Feature | Page(s) | Key Actions |
|---------|---------|-------------|
| **Create Bill** | POS Billing | Search, Add to cart, Payment, Confirm |
| **Add Customer** | Customers → New | Form submit, Auto code generation |
| **View Customer** | Customer Detail | Profile, Purchase history |
| **Add Product** | Products → New | Basic info, Add variants, Set prices |
| **Receive Stock** | Stock → Add Incoming | Search products, Enter qty/prices |
| **View Stock** | Stock Dashboard | Current levels, Low stock alerts |
| **Stock History** | Stock Ledger | Filter by type, date range |
| **Add Category** | Categories | Modal form, Parent selection |
| **View Bills** | Bills History | Filter by status, payment, date |
| **Create Discount** | Discounts | Code, Type, Value, Validity |
| **View Analytics** | Sales Reports | Revenue, Top products, Payments |

---

## 🎯 Common Workflows

### Workflow: Setup New Product
1. `/admin/categories` → Create category (if needed)
2. `/admin/products/new` → Add product
3. Add variants (Small, Medium, Large)
4. Set prices for each variant
5. `/admin/stock/add-incoming` → Add initial stock
6. ✅ Product ready for sale!

### Workflow: Process Sale
1. `/admin/pos` → Open POS
2. Scan barcode or search product
3. Add to cart
4. Select customer (optional)
5. Apply discount (optional)
6. Process payment
7. Confirm → Stock auto-deducted
8. ✅ Sale complete!

### Workflow: Check Stock
1. `/admin/stock` → View dashboard
2. Check low stock alerts (red badges)
3. Click "View Stock Ledger" for details
4. `/admin/stock/add-incoming` if restock needed
5. ✅ Inventory managed!

### Workflow: Analyze Sales
1. `/admin/reports` → Sales dashboard
2. Select date range
3. View revenue, transactions
4. Check top products
5. Export report (optional)
6. ✅ Insights gained!

---

## 🔍 Search & Filters

| Page | Search By | Filters |
|------|-----------|---------|
| **Customers** | Name, Phone, Code | Active/Inactive |
| **Products** | Name, SKU | Category, Grid/List view |
| **Bills** | Bill Number, Customer | Status, Payment Status, Date |
| **Stock Ledger** | - | Transaction Type, Date Range |
| **Discounts** | - | Active/Inactive |

---

## 🎨 Page Components

### Common Elements Across Pages

**Header Section:**
- Page title
- Description
- Primary action button (Add New, Create, etc.)

**Stats Cards:**
- Summary metrics (total, active, counts)
- Color-coded (primary, green, red)

**Search/Filters Bar:**
- Search input with icon
- Filter dropdowns
- View mode toggles (where applicable)

**Data Table/Grid:**
- Sortable columns
- Action buttons (View, Edit, Delete)
- Status badges
- Responsive layout

**Action Buttons:**
- View (Eye icon) - Navigate to detail
- Edit (Pencil icon) - Navigate to edit form
- Delete (Trash icon) - Confirm dialog

---

## 💾 Data Operations

### Create Operations
- `POST` request to backend
- Form validation before submit
- Loading state during request
- Success toast notification
- Redirect to list page
- Query invalidation (refresh cache)

### Read Operations
- `GET` request with React Query
- Loading skeleton/spinner
- Error state handling
- Data caching
- Automatic refetch on window focus

### Update Operations
- Pre-fill form with existing data
- `PUT` request to backend
- Optimistic UI updates
- Success/error notifications
- Query invalidation

### Delete Operations
- Confirmation dialog
- `DELETE` request to backend
- Optimistic removal from list
- Success toast
- Query invalidation

---

## 🎨 Color Scheme

| Element | Color | Usage |
|---------|-------|-------|
| **Primary** | Blue-600 | Main actions, links |
| **Success** | Green-600 | Active status, paid |
| **Warning** | Yellow-600 | Low stock, partial payment |
| **Danger** | Red-600 | Inactive, unpaid, delete |
| **Info** | Purple-600 | Secondary info |
| **Neutral** | Gray-600 | Regular text |

---

## 📱 Responsive Breakpoints

| Breakpoint | Width | Layout Changes |
|------------|-------|----------------|
| **Mobile** | < 768px | Single column, stacked stats |
| **Tablet** | 768px - 1024px | 2-column grids, horizontal scrolling tables |
| **Desktop** | > 1024px | 3-4 column grids, full tables |

---

## ⌨️ Keyboard Shortcuts

Common shortcuts across pages:
- `Ctrl/Cmd + K` - Focus search (where applicable)
- `Esc` - Close modals
- `Enter` - Submit forms
- `Tab` - Navigate form fields

---

## 🔔 Notification Types

| Type | Color | Usage |
|------|-------|-------|
| **Success** | Green | Created, Updated, Deleted successfully |
| **Error** | Red | Failed operation, Validation error |
| **Warning** | Yellow | Low stock, Partial payment |
| **Info** | Blue | General information |

---

## 📊 Stats Dashboard Metrics

### POS Billing
- Today's sales amount
- Transaction count
- Items sold
- Average order value

### Customers
- Total customers
- Active customers
- Total purchases (₹)
- Total loyalty points

### Stock
- Total stock value (₹)
- Total units
- Low stock items count
- Critical stock count

### Products
- Total products
- Total variants
- Active products
- Categories count

### Bills
- Total bills
- Total sales (₹)
- Total paid (₹)
- Outstanding (₹)

### Sales Reports
- Daily revenue
- Period revenue
- Top products (qty & revenue)
- Top customers
- Payment method breakdown

---

## 🔑 Auto-Generated Codes

| Entity | Format | Example |
|--------|--------|---------|
| **Customer** | CUST-YYYYMMDD-XXX | CUST-20260302-001 |
| **Bill** | BIL-YYYYMMDD-XXX | BIL-20260302-042 |
| **Product SKU** | User defined | PROD-001, TSH-BLU-L |
| **Variant SKU** | User defined | PROD-001-S, TSH-BLU-L |

---

## 🎯 Quick Tips

1. **Always check stock before billing** - POS does this automatically
2. **Use customer codes for quick lookup** - Faster than searching by name
3. **Set reorder levels properly** - Get timely low stock alerts
4. **Apply discounts before payment** - Can't modify after confirmation
5. **Confirm bills promptly** - Stock only deducted after confirmation
6. **Use date filters for reports** - Better performance with smaller ranges
7. **Bookmark frequently used pages** - Direct URL access supported
8. **Use category hierarchy** - Better product organization
9. **Track purchase history** - Available in customer detail page
10. **Export reports regularly** - Coming soon feature placeholder

---

## 🆘 Troubleshooting

### Common Issues

**"Insufficient stock" error:**
- Check Stock Dashboard for current levels
- Add incoming stock if needed
- Verify variant selected is correct

**Customer not found:**
- Check if customer is active
- Try searching by phone instead of name
- Create new customer if needed

**Bill not confirming:**
- Ensure all items have stock
- Check payment amount matches total
- Verify network connection

**Product not appearing in search:**
- Check if product is active
- Verify category is selected
- Check SKU/barcode is correct

**Report showing no data:**
- Verify date range selection
- Check if any bills exist in period
- Ensure bills are confirmed (not draft)

---

## 📞 Support Checklist

Before asking for help:
1. ✅ Check this quick reference
2. ✅ Review error message carefully
3. ✅ Try refreshing the page
4. ✅ Check network console for errors
5. ✅ Verify you have admin permissions
6. ✅ Note the exact steps to reproduce

---

**Last Updated:** March 2, 2026
**Version:** 1.0
**Status:** Production Ready

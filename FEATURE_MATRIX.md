# Feature Matrix - A to Z Shop Management
## Phase vs Module vs Status Tracking

**Legend:**
- 🔴 Not Started
- 🟡 In Progress
- 🟢 Completed
- ⚪ Not Applicable

---

## Phase 0: Foundation (Week 1-2)

| Module | Feature | Priority | Status | Notes |
|--------|---------|----------|--------|-------|
| **Infrastructure** | Project setup (Spring Boot 3.x) | Critical | 🔴 | Maven/Gradle, Java 17+ |
| | Database setup (PostgreSQL) | Critical | 🔴 | Docker recommended |
| | Git repository init | Critical | 🔴 | GitHub/GitLab |
| | CI/CD pipeline setup | Medium | 🔴 | GitHub Actions |
| **Security** | User authentication (JWT) | Critical | 🔴 | Spring Security |
| | Role-based access control | Critical | 🔴 | Admin/Manager/Cashier/etc |
| | Password encryption (BCrypt) | Critical | 🔴 | |
| | Session management | Critical | 🔴 | |
| | 2FA (optional) | Low | 🔴 | Phase 8 candidate |
| **Core Tables** | Tenants table | Critical | 🔴 | Multi-tenancy ready |
| | Stores table | Critical | 🔴 | Multi-branch ready |
| | Users table | Critical | 🔴 | |
| | Roles & Permissions | Critical | 🔴 | |
| | Audit logs table | High | 🔴 | Track all changes |
| **API** | REST API structure | Critical | 🔴 | /api/v1/* |
| | API documentation (Swagger) | High | 🔴 | |
| | Error handling framework | Critical | 🔴 | |
| | Validation framework | Critical | 🔴 | Bean Validation |
| **Admin** | Admin login page | Critical | 🔴 | |
| | Basic dashboard structure | Medium | 🔴 | Empty for now |
| | User management UI | High | 🔴 | CRUD users |

**Exit Criteria:** Can create users with different roles, basic CRUD works, audit logs capture actions

---

## Phase 1: Inventory + Incoming Stock (Week 2-4)

| Module | Feature | Priority | Status | Notes |
|--------|---------|----------|--------|-------|
| **Catalog** | Categories CRUD | Critical | 🔴 | Main + subcategory |
| | Category hierarchy (parent-child) | High | 🔴 | Unlimited depth |
| | Category images | Medium | 🔴 | For website |
| | Products CRUD | Critical | 🔴 | |
| | Product variants | Critical | 🔴 | Size/color/etc |
| | Product images (multiple) | High | 🔴 | Main + gallery |
| | SKU auto-generation | Medium | 🔴 | Configurable pattern |
| | Barcode/QR value storage | Critical | 🔴 | **Your requirement** |
| | Brand management | Medium | 🔴 | |
| | Product search & filters | High | 🔴 | |
| | Bulk product import (CSV) | Medium | 🔴 | |
| | Bulk price update | Medium | 🔴 | |
| **Pricing** | Tax codes (GST/VAT) | Critical | 🔴 | Multiple rates |
| | Price lists (Retail/Wholesale) | High | 🔴 | |
| | Cost price | Critical | 🔴 | For profit calc |
| | Selling price | Critical | 🔴 | |
| | MRP | Medium | 🔴 | |
| | Store-specific pricing | Low | 🔴 | Phase 7 |
| | Time-based pricing | Low | 🔴 | Phase 7 |
| **Inventory Core** | Warehouses table | Critical | 🔴 | At least 1 per store |
| | Stock ledger (event log) | Critical | 🔴 | **Core pattern** |
| | Inventory balances (cache) | High | 🔴 | Performance |
| | Real-time stock calculation | Critical | 🔴 | From ledger |
| | Low stock alerts | High | 🔴 | Configurable threshold |
| | Stock adjustment screen | High | 🔴 | Damage/theft/correction |
| | Stock adjustment approval | Low | 🔴 | Phase 5 |
| **Incoming Stock** | Purchase receipts table | Critical | 🔴 | **Your "Incoming tab"** |
| | Incoming stock entry screen | Critical | 🔴 | Supplier, date, items |
| | Add items to receipt | Critical | 🔴 | Qty, cost, selling price |
| | Cost price capture | Critical | 🔴 | For profit |
| | Selling price snapshot | Critical | 🔴 | Expected revenue |
| | MRP snapshot | Medium | 🔴 | |
| | **End summary calculation** | Critical | 🔴 | **Your requirement** |
| | - Total qty received | Critical | 🔴 | Sum of all items |
| | - Total purchase value | Critical | 🔴 | Sum(qty × cost) |
| | - Expected revenue | Critical | 🔴 | Sum(qty × selling) |
| | - Expected gross profit | Critical | 🔴 | Revenue - Cost |
| | Post receipt (finalize) | Critical | 🔴 | Updates stock ledger |
| | Supplier field (optional) | Medium | 🔴 | Basic name only |
| | Receipt edit/delete | High | 🔴 | Before posting |
| | Receipt void (after posting) | Medium | 🔴 | Reverses stock |
| | Receipt list & filters | High | 🔴 | Date, supplier, status |
| **QR/Barcode** | Barcode value storage | Critical | 🔴 | In variant table |
| | QR value storage | Critical | 🔴 | In variant table |
| | Barcode scanner integration | High | 🔴 | Phase 2 (POS) |
| | Barcode label generation | Medium | 🔴 | Print templates |
| | Camera scanning (mobile) | Medium | 🔴 | Phase 3 |

**Exit Criteria:** Inventory counts match ledger 100%, incoming stock updates inventory correctly, expected profit calculated accurately

---

## Phase 2: POS Billing + Returns (Week 4-6)

| Module | Feature | Priority | Status | Notes |
|--------|---------|----------|--------|-------|
| **Customers** | Customer CRUD | High | 🔴 | Basic info |
| | Customer search | High | 🔴 | By phone/name |
| | Walk-in customer support | Critical | 🔴 | Nullable customer |
| | Customer addresses | Medium | 🔴 | For orders later |
| **POS Billing** | Fast billing screen | Critical | 🔴 | Keyboard shortcuts |
| | Search product (name/SKU) | Critical | 🔴 | Auto-complete |
| | **Barcode scan add to cart** | Critical | 🔴 | **Your requirement** |
| | Manual add to cart | Critical | 🔴 | |
| | Cart display (items list) | Critical | 🔴 | |
| | Qty editing | Critical | 🔴 | Keyboard input |
| | Remove item from cart | Critical | 🔴 | |
| | Item-level discount | High | 🔴 | Amount or % |
| | Bill-level discount | High | 🔴 | Amount or % |
| | **Auto tax calculation** | Critical | 🔴 | Per item + total |
| | Subtotal display | Critical | 🔴 | Before tax/discount |
| | Tax display | Critical | 🔴 | GST breakdown |
| | Grand total display | Critical | 🔴 | Final amount |
| | Rounding | Medium | 🔴 | Configurable |
| **Payments** | Cash payment | Critical | 🔴 | |
| | Card payment | Critical | 🔴 | Record only (MVP) |
| | UPI payment | Critical | 🔴 | Record only (MVP) |
| | Split payment | High | 🔴 | Cash + card |
| | Change calculation | Critical | 🔴 | Cash given - total |
| | Payment gateway integration | Low | 🔴 | Phase 8 |
| **Invoice** | Invoice number generation | Critical | 🔴 | Auto-increment |
| | Invoice template | Critical | 🔴 | With logo |
| | Print invoice (thermal) | High | 🔴 | 80mm/58mm |
| | Print invoice (A4) | Medium | 🔴 | For non-thermal |
| | Email invoice | Medium | 🔴 | |
| | WhatsApp invoice | Low | 🔴 | Phase 8 |
| | Invoice QR code | Medium | 🔴 | Verification |
| | GST invoice format | High | 🔴 | India compliance |
| **Stock Integration** | Stock decrease on bill | Critical | 🔴 | Auto ledger entry |
| | Stock check before bill | High | 🔴 | Prevent overselling |
| | Low stock warning | Medium | 🔴 | During billing |
| | Cost snapshot at sale | Critical | 🔴 | For profit calc |
| **Returns/Refunds** | Return by invoice number | High | 🔴 | |
| | Return items selection | High | 🔴 | Full or partial |
| | Restock returned items | High | 🔴 | Configurable |
| | Refund to cash/card/UPI | High | 🔴 | Record only |
| | Return reason tracking | Medium | 🔴 | Dropdown |
| | Refund approval | Low | 🔴 | Phase 5 |
| | Exchange flow | Medium | 🔴 | Return + new bill |
| **Cashier Session** | Open session | High | 🔴 | Start of day |
| | Opening cash entry | High | 🔴 | |
| | Close session | High | 🔴 | End of day |
| | Closing cash declared | High | 🔴 | |
| | Expected cash calculation | High | 🔴 | From bills |
| | Variance report | High | 🔴 | Expected vs declared |
| | Session-wise reports | Medium | 🔴 | |
| **Bill Management** | Bill list & filters | High | 🔴 | Date, customer, status |
| | Bill search | High | 🔴 | By number |
| | Void bill | Medium | 🔴 | Reverses stock |
| | Reprint invoice | High | 🔴 | |
| | Bill edit (before finalize) | Low | 🔴 | Not recommended |
| **Daily Calculations** | **Making bills** workflow | Critical | 🔴 | **Your requirement** |
| | **Calculating day sale** | Critical | 🔴 | **Your requirement** |
| | - Gross sales | Critical | 🔴 | Sum of all bills |
| | - Net sales | Critical | 🔴 | After discounts |
| | - Total discounts | Critical | 🔴 | |
| | - Tax collected | Critical | 🔴 | GST/VAT total |
| | - Payment method split | Critical | 🔴 | Cash/Card/UPI |
| | **- Profit calculation** | Critical | 🔴 | **Your requirement** |
| | - Cost of goods sold (COGS) | Critical | 🔴 | Sum(cost × qty) |
| | - Gross profit | Critical | 🔴 | Sales - COGS |
| | - Profit % | High | 🔴 | (Profit/Sales)×100 |

**Exit Criteria:** Day closing matches cash + digital payments, stock accurate after billing, profit calculated correctly

---

## Phase 3: Customer Website/App + Ordering (Week 6-9)

| Module | Feature | Priority | Status | Notes |
|--------|---------|----------|--------|-------|
| **Website - Customer Side** | Shop logo & branding | Critical | 🔴 | **Your requirement** |
| | Home page banners | High | 🔴 | Carousel |
| | Category navigation | Critical | 🔴 | Menu/sidebar |
| | Product listing page | Critical | 🔴 | Grid/list view |
| | Product search | Critical | 🔴 | |
| | Filters (category/price/brand) | High | 🔴 | |
| | Sort (price/name/new) | High | 🔴 | |
| | Product detail page | Critical | 🔴 | Images, price, desc |
| | **Availability display** | Critical | 🔴 | **Your requirement** |
| | - In stock / Out of stock | Critical | 🔴 | Real-time |
| | - "Only X left" warning | High | 🔴 | Low stock indicator |
| | - Expected restock date | Medium | 🔴 | Optional |
| | Product images gallery | High | 🔴 | Zoom |
| | Product reviews (optional) | Low | 🔴 | Phase 7 |
| | Related products | Medium | 🔴 | |
| **Shopping Cart** | Add to cart | Critical | 🔴 | |
| | Update qty | Critical | 🔴 | |
| | Remove from cart | Critical | 🔴 | |
| | Cart total | Critical | 🔴 | |
| | Stock check on add | High | 🔴 | Prevent overselling |
| | Wishlist | Low | 🔴 | Phase 7 |
| **Checkout** | Customer login/register | Critical | 🔴 | |
| | Guest checkout (optional) | Medium | 🔴 | |
| | Delivery address | Critical | 🔴 | Saved addresses |
| | Multiple addresses | High | 🔴 | Select default |
| | Delivery slot selection | Medium | 🔴 | Time windows |
| | Order notes | Medium | 🔴 | Special instructions |
| | Apply coupon | Low | 🔴 | Phase 5 |
| | Delivery fee calculation | High | 🔴 | By zone/distance |
| | Payment method selection | Critical | 🔴 | COD/Online |
| | Order summary | Critical | 🔴 | Review before submit |
| | Place order | Critical | 🔴 | |
| **Order Tracking** | Order confirmation page | High | 🔴 | |
| | Order confirmation email | Medium | 🔴 | |
| | My orders list | Critical | 🔴 | |
| | Order detail page | Critical | 🔴 | |
| | **Order status timeline** | High | 🔴 | Visual progress |
| | Order cancellation | High | 🔴 | Before packing |
| **Admin - Order Management** | Order list & filters | Critical | 🔴 | Status, date, customer |
| | Order detail view | Critical | 🔴 | All info |
| | **Order status flow** | Critical | 🔴 | **Your requirement** |
| | - NEW | Critical | 🔴 | Just placed |
| | - **ACCEPTED** | Critical | 🔴 | Admin accepts |
| | - PACKED | Critical | 🔴 | Ready to ship |
| | - OUT_FOR_DELIVERY | Critical | 🔴 | Dispatched |
| | - **DELIVERED** | Critical | 🔴 | Completed |
| | - CANCELLED | Critical | 🔴 | By customer/admin |
| | - RETURNED | Critical | 🔴 | After delivery |
| | Accept order button | Critical | 🔴 | NEW → ACCEPTED |
| | Mark as packed | Critical | 🔴 | ACCEPTED → PACKED |
| | Assign delivery agent | High | 🔴 | Phase 6 |
| | Mark as delivered | High | 🔴 | Manual (Phase 6 OTP) |
| | Cancel order | High | 🔴 | With reason |
| | Partial fulfillment | Medium | 🔴 | Some items missing |
| | Substitution workflow | Medium | 🔴 | Approve alternatives |
| **Stock Management** | **Stock reservation** | Critical | 🔴 | **Prevent overselling** |
| | Reserve on ACCEPT | High | 🔴 | Recommended |
| | OR reduce on DELIVER | Medium | 🔴 | Alternative |
| | Release on CANCEL | Critical | 🔴 | Return to available |
| | Reserved stock display | High | 🔴 | In inventory screen |
| **Website Features** | Contact page | Medium | 🔴 | |
| | About page | Low | 🔴 | |
| | FAQ page | Low | 🔴 | |
| | Store locator | Low | 🔴 | Multi-store |
| | Responsive design | Critical | 🔴 | Mobile-first |
| | SEO optimization | Medium | 🔴 | Meta tags |

**Exit Criteria:** End-to-end order flow works, no overselling, order status updates correctly

---

## Phase 4: Dashboard + Reports + Analytics (Week 9-11)

| Module | Feature | Priority | Status | Notes |
|--------|---------|----------|--------|-------|
| **Dashboard KPIs** | Today's sales | Critical | 🔴 | Gross amount |
| | Net sales | Critical | 🔴 | After discounts |
| | Today's orders | Critical | 🔴 | Count |
| | **Today's profit** | Critical | 🔴 | **Your requirement** |
| | Payment split | Critical | 🔴 | Cash/Card/UPI |
| | Low stock count | High | 🔴 | Alert items |
| | Pending orders | High | 🔴 | To be fulfilled |
| | New customers today | Medium | 🔴 | |
| | Date range filter | High | 🔴 | Today/Week/Month |
| **Graphs** | **Sales trend graph** | Critical | 🔴 | **Your requirement** |
| | - Hourly (for today) | High | 🔴 | Line chart |
| | - Daily (for week/month) | Critical | 🔴 | Bar chart |
| | - Monthly (for year) | High | 🔴 | Line chart |
| | **Profit trend graph** | Critical | 🔴 | **Your requirement** |
| | Gross margin % graph | High | 🔴 | Over time |
| | **Payment method split** | Critical | 🔴 | **Donut chart** |
| | **Category-wise sales** | Critical | 🔴 | **Bar chart** |
| | Orders funnel chart | High | 🔴 | Status pipeline |
| | New vs returning customers | Medium | 🔴 | Line chart |
| **Top Products** | **Top N most selling** | Critical | 🔴 | **YOUR KEY REQ** |
| | - By quantity sold | Critical | 🔴 | Units |
| | - By revenue | Critical | 🔴 | Amount |
| | - **By profit contribution** | Critical | 🔴 | Most profitable |
| | Configurable N (5/10/20) | High | 🔴 | |
| | Date range filter | Critical | 🔴 | |
| | Channel filter (POS/Online) | Medium | 🔴 | |
| | Store filter | Low | 🔴 | Multi-branch |
| | Export to Excel | Medium | 🔴 | |
| **Reports - Sales** | **Daily closing report** | Critical | 🔴 | **Your requirement** |
| | - Bill count | Critical | 🔴 | |
| | - Gross sales | Critical | 🔴 | |
| | - Total discounts | Critical | 🔴 | |
| | - Tax collected | Critical | 🔴 | |
| | - Returns/refunds | Critical | 🔴 | |
| | - Net sales | Critical | 🔴 | |
| | - **Payment summary** | Critical | 🔴 | Cash/Card/UPI |
| | - **Cash drawer** | High | 🔴 | Expected vs actual |
| | Sales summary report | High | 🔴 | Date range |
| | Product-wise sales | High | 🔴 | Drill-down |
| | Category-wise sales | High | 🔴 | |
| | Hourly sales report | Medium | 🔴 | Peak hours |
| | Cashier-wise sales | Medium | 🔴 | Performance |
| | Channel-wise sales (POS/Web) | High | 🔴 | |
| **Reports - Profit** | **Profit calculation** | Critical | 🔴 | **Your requirement** |
| | - Estimated profit (MVP) | Critical | 🔴 | Selling - Cost |
| | - FIFO profit (Phase 7) | Low | 🔴 | Accurate costing |
| | **Daily profit report** | Critical | 🔴 | |
| | Product-wise profit | High | 🔴 | Margins |
| | Category-wise profit | High | 🔴 | |
| | Profit margin % | High | 🔴 | |
| | Net profit (with expenses) | Low | 🔴 | Phase 7 |
| **Reports - Inventory** | **Stock valuation report** | High | 🔴 | Cost-based |
| | Stock ledger report | High | 🔴 | All movements |
| | **Incoming items report** | Critical | 🔴 | **Your requirement** |
| | - With expected profit | Critical | 🔴 | Selling - Cost |
| | Low stock report | High | 🔴 | Below threshold |
| | Dead stock report | Medium | 🔴 | No sales in X days |
| | Slow-moving items | Medium | 🔴 | Low velocity |
| | Stock aging report | Medium | 🔴 | 0-30/31-60/60+ days |
| | Expiry alerts report | Low | 🔴 | Phase 7 |
| **Reports - Orders** | Order summary report | High | 🔴 | By status |
| | Cancellation report | Medium | 🔴 | Reasons |
| | Delivery performance | Medium | 🔴 | Avg time |
| **Reports - Customers** | Customer list | High | 🔴 | |
| | Top customers by spend | Medium | 🔴 | |
| | Repeat purchase rate | Medium | 🔴 | |
| | Customer lifetime value | Low | 🔴 | Phase 7 |
| **Reports - Tax** | GST report | High | 🔴 | India |
| | Tax summary | High | 🔴 | |
| | HSN-wise summary | Medium | 🔴 | |
| **Report Features** | Date range picker | Critical | 🔴 | |
| | Export to PDF | High | 🔴 | |
| | Export to Excel | High | 🔴 | |
| | Export to CSV | Medium | 🔴 | |
| | Email report | Medium | 🔴 | |
| | Scheduled reports | Low | 🔴 | Phase 8 |
| | Custom report builder | Low | 🔴 | Phase 8 |

**Exit Criteria:** Dashboard loads < 2 sec, all reports match raw data, top-N accurate

---

## Phase 5: Purchases + Suppliers + Payables (Week 11-13)

| Module | Feature | Priority | Status | Notes |
|--------|---------|----------|--------|-------|
| **Suppliers** | Supplier CRUD | High | 🔴 | |
| | Supplier contact details | High | 🔴 | |
| | GST number | Medium | 🔴 | |
| | Supplier search | High | 🔴 | |
| **Purchase Orders** | PO creation | High | 🔴 | To supplier |
| | PO items | High | 🔴 | Variants + qty |
| | PO status workflow | High | 🔴 | DRAFT/SENT/PARTIAL/RECEIVED |
| | Send PO to supplier | Medium | 🔴 | Email/PDF |
| | Partial receiving | High | 🔴 | Against PO |
| | Close PO | High | 🔴 | When complete |
| | PO vs receipt reconciliation | Medium | 🔴 | Variance |
| **Purchase Receipts Upgrade** | Link to PO | High | 🔴 | Optional |
| | Supplier invoice upload | High | 🔴 | PDF/image |
| | Invoice reconciliation | Medium | 🔴 | PO vs Invoice |
| | Freight/other charges | Medium | 🔴 | Add to cost |
| | Landed cost calculation | Medium | 🔴 | Unit cost adjust |
| **Payables** | Supplier payable tracking | High | 🔴 | Outstanding |
| | Payables aging report | Medium | 🔴 | 0-30/31-60 etc |
| | Payment to supplier | Medium | 🔴 | Record |
| | Payment history | Medium | 🔴 | |
| **Analytics** | Supplier-wise purchase | Medium | 🔴 | Total spend |
| | Cost price history | High | 🔴 | Trend per item |
| | Best supplier by price | Low | 🔴 | |
| **Reorder** | Reorder level (min stock) | High | 🔴 | Per variant |
| | Auto reorder suggestions | Medium | 🔴 | Based on sales |
| | Generate PO from low stock | Medium | 🔴 | One-click |

**Exit Criteria:** Purchase workflow end-to-end, payables accurate

---

## Phase 6: Delivery Management (Week 13-15)

| Module | Feature | Priority | Status | Notes |
|--------|---------|----------|--------|-------|
| **Delivery Agents** | Agent CRUD | High | 🔴 | |
| | Agent role/permissions | High | 🔴 | Limited access |
| | Vehicle details | Medium | 🔴 | |
| **Delivery Assignment** | Assign order to agent | High | 🔴 | Manual |
| | Auto-assign (rule-based) | Medium | 🔴 | Zone/load |
| | Agent workload view | Medium | 🔴 | Orders count |
| | Route optimization | Low | 🔴 | Phase 8 |
| **Delivery Tracking** | Agent mobile app | High | 🔴 | Flutter |
| | Order list for agent | High | 🔴 | Today's deliveries |
| | Navigation to address | Medium | 🔴 | Maps integration |
| | **OTP delivery confirmation** | High | 🔴 | **Your requirement** |
| | Mark as delivered | High | 🔴 | |
| | Proof of delivery photo | Medium | 🔴 | Optional |
| | Customer signature | Low | 🔴 | Optional |
| | Failed delivery reason | Medium | 🔴 | |
| **COD Management** | COD collection tracking | High | 🔴 | Per order |
| | Agent daily settlement | High | 🔴 | COD handover |
| | Settlement report | High | 🔴 | |
| **Delivery Reports** | Delivery performance | Medium | 🔴 | Avg time |
| | Agent performance | Medium | 🔴 | Success rate |
| | On-time delivery % | Medium | 🔴 | |
| | Failed deliveries report | Medium | 🔴 | Reasons |

**Exit Criteria:** Delivery tracking end-to-end, COD reconciles

---

## Phase 7: Advanced Inventory + Accuracy (Week 15-18)

| Module | Feature | Priority | Status | Notes |
|--------|---------|----------|--------|-------|
| **Costing Methods** | FIFO costing | Medium | 🔴 | Pharmacy/FMCG |
| | Weighted average costing | Medium | 🔴 | Electronics |
| | Cost method config | Medium | 🔴 | Per tenant |
| | Accurate profit (batch-wise) | Medium | 🔴 | |
| **Batch Tracking** | Batch creation | Medium | 🔴 | On receipt |
| | Batch code | Medium | 🔴 | |
| | Mfg date | Medium | 🔴 | |
| | Expiry date | Medium | 🔴 | |
| | Batch-wise stock | Medium | 🔴 | |
| | Expiry alerts | Medium | 🔴 | 30/15/7 days |
| | Batch recall | Low | 🔴 | |
| | Sell by batch | Medium | 🔴 | FEFO |
| **Serial Number Tracking** | Serial number capture | Low | 🔴 | Electronics |
| | Serial on receipt | Low | 🔴 | |
| | Serial on sale | Low | 🔴 | |
| | Serial status tracking | Low | 🔴 | Sold/returned/warranty |
| | Warranty tracking | Low | 🔴 | |
| **Stock Audit** | Cycle count mode | Medium | 🔴 | Regular checks |
| | Full audit mode | Medium | 🔴 | Year-end |
| | Scan to count | High | 🔴 | Barcode/QR |
| | Variance report | Medium | 🔴 | System vs physical |
| | Auto-adjust from audit | Medium | 🔴 | Post variance |
| **Multi-Warehouse** | Multiple warehouses | Low | 🔴 | Per store |
| | Warehouse transfers | Low | 🔴 | |
| | Warehouse-wise reports | Low | 🔴 | |
| **Multi-Branch** | Multiple branches/stores | Low | 🔴 | |
| | Branch-wise inventory | Low | 🔴 | Isolated |
| | Branch-wise pricing | Low | 🔴 | Override |
| | Branch-wise tax | Low | 🔴 | Different states |
| | Consolidated reports | Low | 🔴 | All branches |
| | Branch comparison | Low | 🔴 | Performance |
| | Inter-branch transfer | Low | 🔴 | |

**Exit Criteria:** FIFO/batch/serial works correctly

---

## Phase 8: Integrations + Offline + AI (Week 18-22)

| Module | Feature | Priority | Status | Notes |
|--------|---------|----------|--------|-------|
| **Payment Gateway** | Razorpay integration | Medium | 🔴 | India |
| | Stripe integration | Low | 🔴 | International |
| | PayPal | Low | 🔴 | |
| | Online payment on website | Medium | 🔴 | |
| | Payment webhook handling | Medium | 🔴 | |
| | Refund via gateway | Medium | 🔴 | |
| **Notifications** | WhatsApp integration | Medium | 🔴 | Order status |
| | SMS gateway | Medium | 🔴 | OTP/alerts |
| | Email service | Medium | 🔴 | SMTP |
| | Push notifications | Low | 🔴 | Mobile app |
| | Notification templates | Medium | 🔴 | |
| | Notification preferences | Low | 🔴 | User settings |
| **Accounting** | Tally export format | Medium | 🔴 | XML/Excel |
| | QuickBooks export | Low | 🔴 | |
| | Zoho Books export | Low | 🔴 | |
| | Chart of accounts mapping | Medium | 🔴 | |
| **Offline Mode** | Offline POS (local DB) | High | 🔴 | Critical for reliability |
| | Local data cache | High | 🔴 | Products, prices |
| | Sync engine | High | 🔴 | When online |
| | Conflict resolution | High | 🔴 | Same SKU sold offline+online |
| | Sync status indicator | High | 🔴 | |
| **QR Features** | **QR invoice verification** | Medium | 🔴 | **Your requirement** |
| | QR code generation | Medium | 🔴 | Per invoice |
| | Customer scan to verify | Medium | 🔴 | Mobile app |
| | QR product info | Low | 🔴 | Price/stock |
| **AI/Smart Features** | Demand forecasting | Low | 🔴 | Predict sales |
| | Reorder suggestions | Medium | 🔴 | Auto PO |
| | Slow-mover detection | Medium | 🔴 | Auto discount |
| | Price optimization | Low | 🔴 | |
| | Customer segmentation (RFM) | Low | 🔴 | |
| | Frequently bought together | Low | 🔴 | Upsell |
| **Logistics** | Shipping aggregator | Low | 🔴 | Delhivery/Shiprocket |
| | Shipping label generation | Low | 🔴 | |
| | Tracking number | Low | 🔴 | |
| | Rate calculation | Low | 🔴 | |
| **Others** | Multi-language support | Low | 🔴 | i18n |
| | Multi-currency | Low | 🔴 | |
| | Data backup automation | High | 🔴 | Daily |
| | Data restore | High | 🔴 | |
| | API rate limiting | Medium | 🔴 | Security |
| | API usage analytics | Low | 🔴 | |

**Exit Criteria:** Offline mode works, integrations stable, sync reliable

---

## Cross-Cutting Features (All Phases)

| Module | Feature | Priority | Status | Notes |
|--------|---------|----------|--------|-------|
| **Security** | SQL injection prevention | Critical | 🔴 | Prepared statements |
| | XSS protection | Critical | 🔴 | Input sanitization |
| | CSRF protection | Critical | 🔴 | Tokens |
| | Rate limiting | High | 🔴 | API throttling |
| | IP whitelist (optional) | Low | 🔴 | |
| | Data encryption at rest | Medium | 🔴 | Sensitive fields |
| | HTTPS enforcement | Critical | 🔴 | Production |
| **Performance** | Database indexing | Critical | 🔴 | All FK, search fields |
| | Query optimization | High | 🔴 | < 100ms |
| | Caching (Redis) | Medium | 🔴 | Hot data |
| | CDN for images | Medium | 🔴 | |
| | Lazy loading | High | 🔴 | Long lists |
| | Pagination | Critical | 🔴 | All lists |
| **Testing** | Unit tests | High | 🔴 | Service layer |
| | Integration tests | Medium | 🔴 | API endpoints |
| | E2E tests | Low | 🔴 | Critical flows |
| | Performance tests | Medium | 🔴 | Load testing |
| **DevOps** | Docker containers | High | 🔴 | |
| | Docker Compose | High | 🔴 | Local dev |
| | Kubernetes (optional) | Low | 🔴 | Scale |
| | CI/CD pipeline | High | 🔴 | Auto deploy |
| | Environment configs | Critical | 🔴 | Dev/staging/prod |
| | Log aggregation | Medium | 🔴 | ELK/Loki |
| | Monitoring (Prometheus) | Medium | 🔴 | |
| | Alerting | Medium | 🔴 | Slack/email |
| **Documentation** | API docs (Swagger) | High | 🔴 | Auto-generated |
| | User manual | Medium | 🔴 | |
| | Admin manual | Medium | 🔴 | |
| | Developer docs | Medium | 🔴 | Setup guide |
| | Database schema diagram | High | 🔴 | ER diagram |

---

## Summary by Phase

| Phase | Total Features | Critical | High | Medium | Low |
|-------|----------------|----------|------|--------|-----|
| **Phase 0: Foundation** | 21 | 15 | 5 | 1 | 0 |
| **Phase 1: Inventory** | 52 | 24 | 18 | 9 | 1 |
| **Phase 2: POS Billing** | 61 | 35 | 19 | 6 | 1 |
| **Phase 3: Orders** | 55 | 28 | 18 | 8 | 1 |
| **Phase 4: Dashboard** | 67 | 35 | 24 | 7 | 1 |
| **Phase 5: Purchases** | 22 | 0 | 12 | 9 | 1 |
| **Phase 6: Delivery** | 22 | 0 | 12 | 9 | 1 |
| **Phase 7: Advanced** | 33 | 0 | 1 | 23 | 9 |
| **Phase 8: Integrations** | 35 | 0 | 4 | 14 | 17 |
| **Cross-Cutting** | 38 | 9 | 14 | 11 | 4 |
| **TOTAL** | **406** | **146** | **127** | **97** | **36** |

---

## Your Key Requirements Tracking

| Your Requirement | Phase | Feature | Status |
|------------------|-------|---------|--------|
| Shop logo/branding | 3 | Website logo & branding | 🔴 |
| Login system | 0 | Authentication | 🔴 |
| Admin panel | 0-4 | Full admin system | 🔴 |
| Website | 3 | Customer website | 🔴 |
| Adding stock | 1 | Incoming stock tab | 🔴 |
| **Showing availability on user screen** | 3 | Real-time stock display | 🔴 |
| **Ordering system** | 3 | Full order flow | 🔴 |
| **Admin accepting orders** | 3 | Order status: ACCEPTED | 🔴 |
| **Delivering** | 6 | Delivery management | 🔴 |
| **Managing items** | 1 | Product CRUD | 🔴 |
| **Category** | 1 | Category management | 🔴 |
| **Top N most selling products** | 4 | Dashboard widget | 🔴 |
| **Making bills** | 2 | POS billing | 🔴 |
| **Calculating overall day sale** | 2 | Daily closing report | 🔴 |
| **Profit calculation** | 2 | Gross profit | 🔴 |
| **Incoming items tab** | 1 | Purchase receipts | 🔴 |
| **End summary (selling - cost)** | 1 | Receipt summary | 🔴 |
| **QR based item addition** | 2 | Barcode scan billing | 🔴 |
| QR invoice | 8 | QR verification | 🔴 |

---

## How to Use This Matrix

1. **Track Progress**: Update status as features are completed
2. **Sprint Planning**: Pick features by priority for each sprint
3. **Demo Prep**: Show completed ✅ features
4. **Stakeholder Communication**: Share phase completion %
5. **Risk Management**: Identify blocked features

---

## Status Update Template

```
## Sprint X Update (Date: YYYY-MM-DD)

### Completed (🟢)
- Feature 1
- Feature 2

### In Progress (🟡)
- Feature 3 (80% done)
- Feature 4 (30% done)

### Blocked (🔴)
- Feature 5 (waiting for API keys)

### Next Sprint
- Feature 6
- Feature 7
```

---

*Last Updated: 2026-02-28*
*Version: 1.0*
*Total Features: 406*
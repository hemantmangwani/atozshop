# Phase 3 - Customer Website/App + Online Ordering 🛒

**Duration**: 3-4 weeks (Week 6-9 in original plan)
**Goal**: Enable customers to browse products and place orders online
**Complexity**: HIGH (Frontend + Backend integration)

---

## Overview

Phase 3 transforms your POS system into an **omnichannel solution** by adding:
1. **Customer-facing website** - Product catalog browsing
2. **Online ordering system** - Shopping cart + checkout
3. **Admin order management** - Accept, pack, track orders
4. **Stock integration** - Real-time availability + prevent overselling

---

## Key Features Breakdown

### 1. Customer Website (Public-Facing) 🌐

#### Product Catalog Display
- ✅ Home page with banners/carousel
- ✅ Category navigation (menu/sidebar)
- ✅ Product listing page (grid/list view)
- ✅ Product search functionality
- ✅ Filters (category, price, brand)
- ✅ Sort options (price, name, newest)
- ✅ Product detail page (images, price, description)
- ✅ Product image gallery with zoom
- ✅ Related products suggestions

#### **Stock Availability Display** ⭐ (Your Requirement)
- ✅ **Real-time "In Stock" / "Out of Stock" status**
- ✅ **"Only X left" warning** when stock is low
- ✅ Expected restock date (optional)
- ✅ Dynamic updates (no overselling)

**Example**:
```
Product: Samsung Galaxy S23 256GB Black
Status: ⚠️ Only 3 left in stock!
Price: ₹60,000
[Add to Cart]
```

---

### 2. Shopping Cart & Checkout 🛒

#### Shopping Cart
- ✅ Add products to cart
- ✅ Update quantity
- ✅ Remove items
- ✅ Cart total calculation
- ✅ **Stock validation** (prevent adding more than available)
- ✅ Wishlist (Phase 7 - optional)

#### Checkout Process
- ✅ Customer login/register
- ✅ Guest checkout (optional)
- ✅ Delivery address management
  - Multiple saved addresses
  - Select default address
  - Add new address at checkout
- ✅ Delivery slot selection (time windows)
- ✅ Order notes (special instructions)
- ✅ Apply coupon codes (Phase 5 - optional)
- ✅ Delivery fee calculation (by zone/distance)
- ✅ Payment method selection:
  - Cash on Delivery (COD)
  - Online Payment (Razorpay/Stripe - Phase 8)
- ✅ Order summary review
- ✅ Place order button

---

### 3. Order Management System 📦

#### **Order Status Workflow** ⭐ (Your Requirement)

```
Customer Places Order
    ↓
NEW (Just placed)
    ↓
ACCEPTED (Admin accepts order) ← Your action
    ↓
PACKED (Items ready to ship)
    ↓
OUT_FOR_DELIVERY (Dispatched)
    ↓
DELIVERED (Completed) ← Your confirmation
    ↓
(Optional: RETURNED if customer returns)

Side flows:
- CANCELLED (by customer or admin)
```

**Key Actions**:
1. **Customer places order** → Creates order in NEW status
2. **Admin reviews order** → Accepts or rejects
3. **Admin accepts** → Status: NEW → ACCEPTED (reserves stock)
4. **Admin packs items** → Status: ACCEPTED → PACKED
5. **Assign delivery agent** → Status: PACKED → OUT_FOR_DELIVERY (Phase 6)
6. **Confirm delivery** → Status: OUT_FOR_DELIVERY → DELIVERED
7. **If cancelled** → Stock released back to inventory

---

### 4. Admin Order Management Interface 👨‍💼

#### Order Dashboard
- ✅ Order list with filters:
  - By status (NEW, ACCEPTED, PACKED, etc.)
  - By date range
  - By customer
  - By payment status
- ✅ Order detail view (all information)
- ✅ Order search (by order number)

#### Admin Actions
- ✅ **Accept Order** button (NEW → ACCEPTED)
  - Reserves stock
  - Sends confirmation to customer
- ✅ **Mark as Packed** (ACCEPTED → PACKED)
  - Items ready for pickup/delivery
- ✅ **Assign Delivery Agent** (Phase 6)
- ✅ **Mark as Delivered**
  - Manual marking (Phase 6 adds OTP verification)
- ✅ **Cancel Order** with reason
  - Releases reserved stock
  - Notifies customer
- ✅ **Partial Fulfillment** (some items out of stock)
- ✅ **Product Substitution** workflow

---

### 5. Stock Management Integration ⚠️ CRITICAL

#### **Prevent Overselling** ⭐ (Most Important)

**Problem**: If 5 units in stock, 10 customers shouldn't be able to order simultaneously.

**Solution - Stock Reservation**:

**Option A: Reserve on ACCEPT** (Recommended)
```
Customer orders 2 units
  → Order created (status: NEW)
  → Admin accepts order
  → Stock reserved: Available 5 → Reserved 2, Available 3
  → Other customers can only order 3 units max
  → On delivery: Reserved 2 → Sold 2, Available 3 → 1
  → On cancel: Reserved 2 → Available 5
```

**Option B: Reduce on DELIVER**
```
Customer orders 2 units
  → Stock immediately reduced: 5 → 3
  → Other customers see 3 available
  → If cancelled: Stock returned: 3 → 5
```

**What We'll Implement**:
- Add `reserved_quantity` field to stock calculations
- Available for sale = Total stock - Reserved - Sold
- Display on website: "X available" (excludes reserved)
- Reserved stock display in admin panel

---

### 6. Customer Order Tracking 📍

#### Customer Portal
- ✅ Order confirmation page (after placing order)
- ✅ Order confirmation email
- ✅ "My Orders" page (order history)
- ✅ Order detail page
- ✅ **Order status timeline** (visual progress)
  ```
  ✅ Placed → ✅ Accepted → 🔄 Packing → ⏳ Out for Delivery → ⏳ Delivered
  ```
- ✅ Order cancellation (before packing)
- ✅ Track delivery (Phase 6 - live location)

---

## Database Changes Needed

### New Tables (4 tables)

#### 1. `orders` (Order Headers)
```sql
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    order_number VARCHAR(50) NOT NULL,  -- ORD-YYYYMMDD-XXX
    order_date TIMESTAMP NOT NULL,

    -- Delivery
    delivery_address_id BIGINT,
    delivery_slot VARCHAR(50),  -- "9 AM - 12 PM"
    delivery_fee DECIMAL(10,2) DEFAULT 0,
    customer_notes TEXT,

    -- Amounts
    subtotal DECIMAL(12,2) NOT NULL,
    discount_amount DECIMAL(12,2) DEFAULT 0,
    tax_amount DECIMAL(12,2) DEFAULT 0,
    total_amount DECIMAL(12,2) NOT NULL,

    -- Status
    status VARCHAR(30) NOT NULL,  -- NEW, ACCEPTED, PACKED, OUT_FOR_DELIVERY, DELIVERED, CANCELLED, RETURNED
    payment_method VARCHAR(20),   -- COD, ONLINE
    payment_status VARCHAR(20),   -- PENDING, PAID, REFUNDED

    -- Tracking
    accepted_at TIMESTAMP,
    packed_at TIMESTAMP,
    dispatched_at TIMESTAMP,
    delivered_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    cancel_reason TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, order_number)
);
```

#### 2. `order_items` (Order Line Items)
```sql
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    variant_id BIGINT NOT NULL,

    -- Snapshots (for historical accuracy)
    sku VARCHAR(50) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    variant_name VARCHAR(200),

    -- Pricing
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    discount_amount DECIMAL(10,2) DEFAULT 0,
    tax_amount DECIMAL(10,2) DEFAULT 0,
    total_amount DECIMAL(12,2) NOT NULL,

    -- Fulfillment
    fulfilled_quantity INTEGER DEFAULT 0,
    substituted_variant_id BIGINT,  -- If substituted

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 3. `customer_addresses`
```sql
CREATE TABLE customer_addresses (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id),

    address_type VARCHAR(20),  -- HOME, WORK, OTHER
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    landmark VARCHAR(100),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(100) DEFAULT 'India',

    is_default BOOLEAN DEFAULT false,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 4. `stock_reservations` (Optional - for Reserved Stock Tracking)
```sql
CREATE TABLE stock_reservations (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    variant_id BIGINT NOT NULL,
    order_id BIGINT REFERENCES orders(id),

    reserved_quantity INTEGER NOT NULL,
    reservation_date TIMESTAMP NOT NULL,
    expires_at TIMESTAMP,  -- Auto-release after X hours

    status VARCHAR(20),  -- ACTIVE, FULFILLED, CANCELLED, EXPIRED

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## API Endpoints Needed (25+ endpoints)

### Customer-Facing APIs

#### Product Catalog
```
GET    /api/v1/public/products                     - List products (with availability)
GET    /api/v1/public/products/{id}                - Product details
GET    /api/v1/public/products/search?q=samsung    - Search products
GET    /api/v1/public/categories                   - List categories
GET    /api/v1/public/categories/{id}/products     - Products by category
```

#### Shopping Cart (Session-based or User-based)
```
POST   /api/v1/cart/add                            - Add to cart
PUT    /api/v1/cart/items/{id}                     - Update quantity
DELETE /api/v1/cart/items/{id}                     - Remove item
GET    /api/v1/cart                                - Get cart
DELETE /api/v1/cart                                - Clear cart
```

#### Checkout & Orders
```
POST   /api/v1/orders                              - Place order
GET    /api/v1/orders                              - Customer's order history
GET    /api/v1/orders/{id}                         - Order details
POST   /api/v1/orders/{id}/cancel                  - Cancel order
```

#### Customer Addresses
```
POST   /api/v1/customers/addresses                 - Add address
GET    /api/v1/customers/addresses                 - List addresses
PUT    /api/v1/customers/addresses/{id}            - Update address
DELETE /api/v1/customers/addresses/{id}            - Delete address
PUT    /api/v1/customers/addresses/{id}/default    - Set default
```

### Admin-Facing APIs

#### Order Management
```
GET    /api/v1/admin/orders                        - List all orders (with filters)
GET    /api/v1/admin/orders/{id}                   - Order details
POST   /api/v1/admin/orders/{id}/accept            - Accept order (NEW → ACCEPTED)
POST   /api/v1/admin/orders/{id}/pack              - Mark as packed (ACCEPTED → PACKED)
POST   /api/v1/admin/orders/{id}/dispatch          - Mark dispatched
POST   /api/v1/admin/orders/{id}/deliver           - Mark delivered
POST   /api/v1/admin/orders/{id}/cancel            - Cancel order
PUT    /api/v1/admin/orders/{id}/items/{itemId}    - Partial fulfillment
```

#### Stock Reservation (Internal)
```
POST   /api/v1/stock/reserve                       - Reserve stock
POST   /api/v1/stock/release                       - Release reservation
GET    /api/v1/stock/reservations                  - List reservations
```

---

## Implementation Steps

### Step 1: Backend (1-1.5 weeks)

**Entities** (4 files):
- Order.java
- OrderItem.java
- CustomerAddress.java
- StockReservation.java (optional)

**Repositories** (4 files):
- OrderRepository.java
- OrderItemRepository.java
- CustomerAddressRepository.java
- StockReservationRepository.java

**Services** (3 files):
- OrderService.java (core order logic)
- StockReservationService.java (prevent overselling)
- CartService.java (shopping cart management)

**Controllers** (3 files):
- PublicProductController.java (customer-facing catalog)
- OrderController.java (customer orders)
- AdminOrderController.java (order management)

**DTOs** (15-20 files):
- CreateOrderRequest, OrderResponse, OrderItemResponse
- AddToCartRequest, CartResponse
- AddressRequest, AddressResponse
- AcceptOrderRequest, etc.

### Step 2: Frontend Website (1.5-2 weeks)

**Pages Needed**:
1. Home page (banners, featured products)
2. Product listing page (grid/list)
3. Product detail page
4. Shopping cart page
5. Checkout page
6. Order confirmation page
7. My Orders page
8. Order detail page
9. My Addresses page

**Technologies**:
- **Option A**: React + TypeScript (recommended for web)
- **Option B**: Flutter Web (if you want mobile app too)

### Step 3: Admin Interface Updates (3-4 days)

**New Screens**:
1. Orders list (with status filters)
2. Order detail view
3. Order management actions (Accept/Pack/Dispatch)
4. Reserved stock view

---

## Business Logic Highlights

### Stock Availability Calculation
```java
// Available for customers to order
availableStock = totalStock - soldStock - reservedStock

// Display on website
if (availableStock > 10) {
    return "In Stock";
} else if (availableStock > 0) {
    return "Only " + availableStock + " left!";
} else {
    return "Out of Stock";
}
```

### Order Placement Flow
```java
1. Validate cart items (stock available?)
2. Calculate totals (subtotal, tax, delivery fee)
3. Create order (status: NEW)
4. Send confirmation email/SMS
5. Notify admin (new order alert)
```

### Order Acceptance Flow
```java
1. Admin clicks "Accept Order"
2. Check stock availability again (race condition protection)
3. Reserve stock for this order
4. Update order status: NEW → ACCEPTED
5. Notify customer (order accepted)
```

### Order Delivery Flow
```java
1. Admin marks "Delivered"
2. Update order status: OUT_FOR_DELIVERY → DELIVERED
3. Deduct reserved stock from inventory (create SALE ledger entry)
4. Release reservation record
5. Update customer loyalty points (optional)
6. Send delivery confirmation
```

### Order Cancellation Flow
```java
1. Check if order can be cancelled (before DELIVERED)
2. Release reserved stock back to available
3. Update order status: → CANCELLED
4. If payment made, process refund
5. Notify customer
```

---

## Testing Strategy

### Critical Test Cases

1. **Overselling Prevention** ⭐
   ```
   - 5 units in stock
   - 3 customers add 2 units each to cart simultaneously
   - Only first 2 customers can checkout successfully
   - 3rd customer gets "Insufficient stock" error
   ```

2. **Stock Reservation**
   ```
   - Order placed: 10 → 8 available (2 reserved)
   - Order accepted: 8 available, 2 reserved
   - Order cancelled: 10 available, 0 reserved
   - Order delivered: 8 available, 0 reserved (sold)
   ```

3. **Order Status Transitions**
   ```
   - Cannot skip statuses (NEW → PACKED not allowed)
   - Cannot cancel after DELIVERED
   - Cannot accept cancelled orders
   ```

4. **Concurrent Orders**
   ```
   - Multiple customers ordering same product
   - Stock accurately decremented
   - No negative stock
   ```

---

## What You Get After Phase 3

### For Customers 👥
- ✅ Browse products online
- ✅ See real-time availability ("Only 3 left!")
- ✅ Place orders 24/7
- ✅ Track order status
- ✅ Manage delivery addresses

### For Shop Owner 👨‍💼
- ✅ Receive online orders
- ✅ Accept/reject orders
- ✅ Track order fulfillment
- ✅ Prevent overselling (stock reserved)
- ✅ Unified inventory (online + in-store)

### System Benefits 🚀
- ✅ Omnichannel solution (POS + Website)
- ✅ No overselling across channels
- ✅ Real-time stock sync
- ✅ Complete order workflow
- ✅ Customer self-service

---

## Estimated Effort

| Task | Time | Complexity |
|------|------|------------|
| Backend entities & repos | 2 days | Medium |
| Backend services & APIs | 4 days | High |
| Stock reservation logic | 2 days | High |
| Frontend website (basic) | 7 days | Medium |
| Admin order management UI | 3 days | Medium |
| Testing & bug fixes | 3 days | High |
| **Total** | **~3 weeks** | |

---

## Phase 3 vs Phase 2 Comparison

| Aspect | Phase 2 (POS) | Phase 3 (Website) |
|--------|---------------|-------------------|
| Channel | In-store only | Online + In-store |
| User | Shop staff | Customers |
| Stock | Immediate deduction | Reserved → Deducted |
| Payment | Cash/Card at counter | COD / Online |
| Workflow | Bill → Pay → Done | Order → Accept → Pack → Deliver |
| Complexity | Medium | High |

---

## Next Steps After Phase 3

Once Phase 3 is complete, you'll have:
- ✅ Complete omnichannel POS system
- ✅ Online ordering capability
- ✅ Inventory management across channels

**Then you can choose**:
- **Phase 4**: Dashboard + Analytics (see sales trends, top products)
- **Phase 5**: Purchase Order Management (supplier orders)
- **Phase 6**: Delivery Management (delivery agents, OTP, tracking)

---

## Recommendation

**Phase 3 is BIG** - It's essentially building an e-commerce website + order management system.

**Alternative Approach** (if you want quicker value):

### Mini Phase 2.5: Complete POS First (1 week)
Before tackling Phase 3, complete these POS features:
1. Receipt PDF generation ⭐ (customers need receipts)
2. Daily sales reports (you check this every day)
3. Return/refund system (will happen eventually)

**Why?** Phase 2 is 80% done. Finishing it makes the POS **production-ready NOW**.

**Then:** Tackle Phase 3 as a separate major project.

---

**What would you prefer?**

A. **Jump into Phase 3** (Website + Online Orders) - 3-4 weeks
B. **Complete Phase 2 first** (Receipt + Reports + Returns) - 1 week, then Phase 3
C. **Something else**

Let me know! 😊

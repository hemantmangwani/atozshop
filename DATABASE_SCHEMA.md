# Database Schema - A to Z Shop Management

## Design Principles

1. **Ledger Pattern**: All stock movements recorded as append-only events
2. **Snapshot Pattern**: Store price/tax/cost at transaction time (never recalculate history)
3. **Multi-Tenancy**: Tenant isolation for SaaS capability
4. **Soft Delete**: Use `is_active` flags, never hard delete
5. **Extensibility**: `metadata_json` fields for future attributes
6. **ACID Compliance**: Strong consistency for inventory and financial data

---

## 1. Core: Tenants, Stores, Users, Roles

### tenants
Multi-shop support (SaaS ready)

```sql
CREATE TABLE tenants (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    timezone VARCHAR(50) DEFAULT 'UTC',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### stores
Multi-branch support

```sql
CREATE TABLE stores (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL,
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(255),
    gst_number VARCHAR(50),
    logo_url VARCHAR(500),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, code)
);

CREATE INDEX idx_stores_tenant ON stores(tenant_id);
```

### users
All system users (admin, staff, customers)

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    store_id BIGINT REFERENCES stores(id),
    username VARCHAR(100),
    email VARCHAR(255),
    phone VARCHAR(20),
    password_hash VARCHAR(255),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    is_active BOOLEAN DEFAULT true,
    email_verified BOOLEAN DEFAULT false,
    phone_verified BOOLEAN DEFAULT false,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    metadata_json JSONB,
    UNIQUE(tenant_id, email),
    UNIQUE(tenant_id, phone)
);

CREATE INDEX idx_users_tenant ON users(tenant_id);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_phone ON users(phone);
```

### roles

```sql
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_system BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, name)
);

-- System roles: ADMIN, MANAGER, CASHIER, STOCK_KEEPER, DELIVERY_AGENT
INSERT INTO roles (tenant_id, name, is_system) VALUES
    (1, 'ADMIN', true),
    (1, 'MANAGER', true),
    (1, 'CASHIER', true),
    (1, 'STOCK_KEEPER', true),
    (1, 'DELIVERY_AGENT', true);
```

### permissions

```sql
CREATE TABLE permissions (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(100) UNIQUE NOT NULL,
    module VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Examples: product.view, product.create, product.edit, product.delete
-- bill.create, bill.refund, stock.adjust, order.cancel, report.export
```

### role_permissions

```sql
CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL REFERENCES roles(id),
    permission_id BIGINT NOT NULL REFERENCES permissions(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id, permission_id)
);
```

### user_roles

```sql
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id),
    role_id BIGINT NOT NULL REFERENCES roles(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id)
);
```

---

## 2. Catalog: Categories, Products, Variants, Pricing

### categories

```sql
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    parent_id BIGINT REFERENCES categories(id),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    description TEXT,
    image_url VARCHAR(500),
    sort_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    metadata_json JSONB,
    UNIQUE(tenant_id, slug)
);

CREATE INDEX idx_categories_tenant ON categories(tenant_id);
CREATE INDEX idx_categories_parent ON categories(parent_id);
CREATE INDEX idx_categories_active ON categories(tenant_id, is_active);
```

### products
Parent product (e.g., "T-Shirt")

```sql
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    category_id BIGINT REFERENCES categories(id),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    description TEXT,
    brand VARCHAR(100),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    metadata_json JSONB,
    UNIQUE(tenant_id, slug)
);

CREATE INDEX idx_products_tenant ON products(tenant_id);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_active ON products(tenant_id, is_active);
CREATE INDEX idx_products_brand ON products(brand);
```

### product_variants
Sellable SKU (size/color variation or standalone)

```sql
CREATE TABLE product_variants (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    product_id BIGINT NOT NULL REFERENCES products(id),
    sku VARCHAR(100) NOT NULL,
    variant_name VARCHAR(255),
    unit VARCHAR(20) DEFAULT 'pcs', -- pcs, kg, litre, box, etc.
    barcode_value VARCHAR(100),
    qr_value VARCHAR(255),
    tax_code_id BIGINT REFERENCES tax_codes(id),
    min_stock_threshold INT DEFAULT 0,
    max_stock_threshold INT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    metadata_json JSONB, -- weight, dimensions, serial_tracking_enabled, etc.
    UNIQUE(tenant_id, sku),
    UNIQUE(tenant_id, barcode_value)
);

CREATE INDEX idx_variants_tenant ON product_variants(tenant_id);
CREATE INDEX idx_variants_product ON product_variants(product_id);
CREATE INDEX idx_variants_sku ON product_variants(tenant_id, sku);
CREATE INDEX idx_variants_barcode ON product_variants(barcode_value);
CREATE INDEX idx_variants_active ON product_variants(tenant_id, is_active);
```

### product_images

```sql
CREATE TABLE product_images (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id),
    variant_id BIGINT REFERENCES product_variants(id),
    url VARCHAR(500) NOT NULL,
    alt_text VARCHAR(255),
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_images_product ON product_images(product_id);
CREATE INDEX idx_images_variant ON product_images(variant_id);
```

### tax_codes

```sql
CREATE TABLE tax_codes (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    name VARCHAR(100) NOT NULL,
    rate_percent DECIMAL(5,2) NOT NULL,
    hsn_sac VARCHAR(20),
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, name)
);

-- Examples: GST 5%, GST 12%, GST 18%, VAT 5%
```

### price_lists
Supports retail/wholesale/member pricing

```sql
CREATE TABLE price_lists (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    currency VARCHAR(3) DEFAULT 'INR',
    is_default BOOLEAN DEFAULT false,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, name)
);

-- Default: Retail, Wholesale, Member
```

### variant_prices
Store-specific and time-based pricing

```sql
CREATE TABLE variant_prices (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    store_id BIGINT REFERENCES stores(id),
    price_list_id BIGINT NOT NULL REFERENCES price_lists(id),
    variant_id BIGINT NOT NULL REFERENCES product_variants(id),
    cost_price DECIMAL(12,2),
    selling_price DECIMAL(12,2) NOT NULL,
    mrp DECIMAL(12,2),
    effective_from TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    effective_to TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_variant_prices_tenant ON variant_prices(tenant_id);
CREATE INDEX idx_variant_prices_store ON variant_prices(store_id);
CREATE INDEX idx_variant_prices_list ON variant_prices(price_list_id);
CREATE INDEX idx_variant_prices_variant ON variant_prices(variant_id);
CREATE INDEX idx_variant_prices_effective ON variant_prices(effective_from, effective_to);

-- Unique: one price per variant per list per store per time
CREATE UNIQUE INDEX idx_variant_prices_unique ON variant_prices(
    tenant_id, COALESCE(store_id, 0), price_list_id, variant_id, effective_from
);
```

---

## 3. Inventory: Stock Ledger (Core Pattern)

### warehouses

```sql
CREATE TABLE warehouses (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    store_id BIGINT REFERENCES stores(id),
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL,
    address TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, code)
);

CREATE INDEX idx_warehouses_tenant ON warehouses(tenant_id);
CREATE INDEX idx_warehouses_store ON warehouses(store_id);
```

### stock_ledger
**THE HEART OF INVENTORY** - append-only event log

```sql
CREATE TABLE stock_ledger (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    store_id BIGINT NOT NULL REFERENCES stores(id),
    warehouse_id BIGINT NOT NULL REFERENCES warehouses(id),
    variant_id BIGINT NOT NULL REFERENCES product_variants(id),
    txn_type VARCHAR(50) NOT NULL, -- INCOMING, SALE, RETURN, ADJUSTMENT, TRANSFER_IN, TRANSFER_OUT, ORDER_RESERVE, ORDER_RELEASE
    qty DECIMAL(12,3) NOT NULL, -- positive for IN, negative for OUT
    unit_cost DECIMAL(12,2), -- for FIFO/Average costing
    reference_type VARCHAR(50), -- purchase_receipt, bill, order, stock_adjustment, stock_transfer
    reference_id BIGINT,
    batch_id BIGINT REFERENCES batches(id),
    serial_id BIGINT REFERENCES serial_numbers(id),
    note TEXT,
    created_by BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_stock_ledger_tenant ON stock_ledger(tenant_id);
CREATE INDEX idx_stock_ledger_store ON stock_ledger(store_id);
CREATE INDEX idx_stock_ledger_warehouse ON stock_ledger(warehouse_id);
CREATE INDEX idx_stock_ledger_variant ON stock_ledger(variant_id);
CREATE INDEX idx_stock_ledger_created ON stock_ledger(created_at);
CREATE INDEX idx_stock_ledger_reference ON stock_ledger(reference_type, reference_id);
CREATE INDEX idx_stock_ledger_txn_type ON stock_ledger(txn_type);

-- Composite index for stock calculation
CREATE INDEX idx_stock_ledger_balance ON stock_ledger(
    tenant_id, store_id, warehouse_id, variant_id, created_at
);
```

### inventory_balances
**Optional cache** - calculated from ledger

```sql
CREATE TABLE inventory_balances (
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    store_id BIGINT NOT NULL REFERENCES stores(id),
    warehouse_id BIGINT NOT NULL REFERENCES warehouses(id),
    variant_id BIGINT NOT NULL REFERENCES product_variants(id),
    qty_on_hand DECIMAL(12,3) DEFAULT 0,
    qty_reserved DECIMAL(12,3) DEFAULT 0, -- for pending orders
    qty_available DECIMAL(12,3) DEFAULT 0, -- on_hand - reserved
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (tenant_id, store_id, warehouse_id, variant_id)
);

CREATE INDEX idx_inventory_balances_variant ON inventory_balances(variant_id);
CREATE INDEX idx_inventory_balances_store ON inventory_balances(store_id);
```

### stock_adjustments
Manual corrections (damage, theft, count variance)

```sql
CREATE TABLE stock_adjustments (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    store_id BIGINT NOT NULL REFERENCES stores(id),
    warehouse_id BIGINT NOT NULL REFERENCES warehouses(id),
    adjustment_no VARCHAR(50) NOT NULL,
    reason VARCHAR(255) NOT NULL,
    status VARCHAR(20) DEFAULT 'DRAFT', -- DRAFT, POSTED, VOID
    notes TEXT,
    created_by BIGINT NOT NULL REFERENCES users(id),
    posted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, adjustment_no)
);

CREATE INDEX idx_stock_adjustments_tenant ON stock_adjustments(tenant_id);
CREATE INDEX idx_stock_adjustments_store ON stock_adjustments(store_id);
```

### stock_adjustment_items

```sql
CREATE TABLE stock_adjustment_items (
    id BIGSERIAL PRIMARY KEY,
    adjustment_id BIGINT NOT NULL REFERENCES stock_adjustments(id),
    variant_id BIGINT NOT NULL REFERENCES product_variants(id),
    qty_before DECIMAL(12,3),
    qty_after DECIMAL(12,3),
    qty_delta DECIMAL(12,3) NOT NULL,
    unit_cost DECIMAL(12,2),
    note TEXT
);

CREATE INDEX idx_adjustment_items_adjustment ON stock_adjustment_items(adjustment_id);
CREATE INDEX idx_adjustment_items_variant ON stock_adjustment_items(variant_id);
```

### stock_transfers

```sql
CREATE TABLE stock_transfers (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    transfer_no VARCHAR(50) NOT NULL,
    from_warehouse_id BIGINT NOT NULL REFERENCES warehouses(id),
    to_warehouse_id BIGINT NOT NULL REFERENCES warehouses(id),
    status VARCHAR(20) DEFAULT 'DRAFT', -- DRAFT, IN_TRANSIT, RECEIVED, CANCELLED
    notes TEXT,
    created_by BIGINT NOT NULL REFERENCES users(id),
    received_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    received_at TIMESTAMP,
    UNIQUE(tenant_id, transfer_no)
);

CREATE INDEX idx_stock_transfers_tenant ON stock_transfers(tenant_id);
```

### stock_transfer_items

```sql
CREATE TABLE stock_transfer_items (
    id BIGSERIAL PRIMARY KEY,
    transfer_id BIGINT NOT NULL REFERENCES stock_transfers(id),
    variant_id BIGINT NOT NULL REFERENCES product_variants(id),
    qty DECIMAL(12,3) NOT NULL,
    received_qty DECIMAL(12,3)
);

CREATE INDEX idx_transfer_items_transfer ON stock_transfer_items(transfer_id);
```

---

## 4. Purchases / Incoming Stock

### suppliers

```sql
CREATE TABLE suppliers (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50),
    contact_person VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(255),
    address TEXT,
    gst_number VARCHAR(50),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    metadata_json JSONB,
    UNIQUE(tenant_id, code)
);

CREATE INDEX idx_suppliers_tenant ON suppliers(tenant_id);
```

### purchase_orders

```sql
CREATE TABLE purchase_orders (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    store_id BIGINT NOT NULL REFERENCES stores(id),
    po_no VARCHAR(50) NOT NULL,
    supplier_id BIGINT NOT NULL REFERENCES suppliers(id),
    status VARCHAR(20) DEFAULT 'DRAFT', -- DRAFT, SENT, PARTIAL, RECEIVED, CLOSED, CANCELLED
    expected_date DATE,
    subtotal DECIMAL(12,2),
    tax_total DECIMAL(12,2),
    grand_total DECIMAL(12,2),
    notes TEXT,
    created_by BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, po_no)
);

CREATE INDEX idx_purchase_orders_tenant ON purchase_orders(tenant_id);
CREATE INDEX idx_purchase_orders_supplier ON purchase_orders(supplier_id);
CREATE INDEX idx_purchase_orders_status ON purchase_orders(status);
```

### purchase_order_items

```sql
CREATE TABLE purchase_order_items (
    id BIGSERIAL PRIMARY KEY,
    po_id BIGINT NOT NULL REFERENCES purchase_orders(id),
    variant_id BIGINT NOT NULL REFERENCES product_variants(id),
    ordered_qty DECIMAL(12,3) NOT NULL,
    received_qty DECIMAL(12,3) DEFAULT 0,
    expected_cost DECIMAL(12,2) NOT NULL,
    tax_percent DECIMAL(5,2),
    line_total DECIMAL(12,2)
);

CREATE INDEX idx_po_items_po ON purchase_order_items(po_id);
CREATE INDEX idx_po_items_variant ON purchase_order_items(variant_id);
```

### purchase_receipts
**Your "Incoming Stock Tab"**

```sql
CREATE TABLE purchase_receipts (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    store_id BIGINT NOT NULL REFERENCES stores(id),
    warehouse_id BIGINT NOT NULL REFERENCES warehouses(id),
    receipt_no VARCHAR(50) NOT NULL,
    supplier_id BIGINT REFERENCES suppliers(id), -- nullable for MVP
    po_id BIGINT REFERENCES purchase_orders(id),
    invoice_no VARCHAR(100),
    invoice_date DATE,
    status VARCHAR(20) DEFAULT 'DRAFT', -- DRAFT, POSTED, VOID
    subtotal DECIMAL(12,2),
    tax_total DECIMAL(12,2),
    other_charges DECIMAL(12,2),
    grand_total DECIMAL(12,2),
    notes TEXT,
    created_by BIGINT NOT NULL REFERENCES users(id),
    posted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, receipt_no)
);

CREATE INDEX idx_purchase_receipts_tenant ON purchase_receipts(tenant_id);
CREATE INDEX idx_purchase_receipts_supplier ON purchase_receipts(supplier_id);
CREATE INDEX idx_purchase_receipts_po ON purchase_receipts(po_id);
CREATE INDEX idx_purchase_receipts_created ON purchase_receipts(created_at);
```

### purchase_receipt_items

```sql
CREATE TABLE purchase_receipt_items (
    id BIGSERIAL PRIMARY KEY,
    receipt_id BIGINT NOT NULL REFERENCES purchase_receipts(id),
    variant_id BIGINT NOT NULL REFERENCES product_variants(id),
    qty_received DECIMAL(12,3) NOT NULL,
    unit_cost DECIMAL(12,2) NOT NULL, -- Important for profit calculation
    selling_price_snapshot DECIMAL(12,2), -- For "expected revenue" in summary
    mrp_snapshot DECIMAL(12,2),
    tax_percent DECIMAL(5,2),
    batch_id BIGINT REFERENCES batches(id),
    line_total_cost DECIMAL(12,2)
);

CREATE INDEX idx_receipt_items_receipt ON purchase_receipt_items(receipt_id);
CREATE INDEX idx_receipt_items_variant ON purchase_receipt_items(variant_id);
```

**On POST**: Insert into `stock_ledger` with `txn_type='INCOMING'`, `qty=qty_received`, `unit_cost=unit_cost`, `reference_type='purchase_receipt'`, `reference_id=receipt_id`

---

## 5. Billing / POS

### customers

```sql
CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    customer_code VARCHAR(50),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(255),
    loyalty_points INT DEFAULT 0,
    credit_limit DECIMAL(12,2) DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    metadata_json JSONB,
    UNIQUE(tenant_id, phone),
    UNIQUE(tenant_id, email)
);

CREATE INDEX idx_customers_tenant ON customers(tenant_id);
CREATE INDEX idx_customers_phone ON customers(phone);
CREATE INDEX idx_customers_email ON customers(email);
```

### customer_addresses

```sql
CREATE TABLE customer_addresses (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    label VARCHAR(50), -- Home, Office
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100),
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    is_default BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_customer_addresses_customer ON customer_addresses(customer_id);
```

### bills (POS invoices)

```sql
CREATE TABLE bills (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    store_id BIGINT NOT NULL REFERENCES stores(id),
    warehouse_id BIGINT NOT NULL REFERENCES warehouses(id),
    bill_no VARCHAR(50) NOT NULL,
    customer_id BIGINT REFERENCES customers(id), -- nullable for walk-in
    bill_status VARCHAR(20) DEFAULT 'PAID', -- PAID, PARTIAL, DUE, VOID, REFUNDED
    subtotal DECIMAL(12,2) NOT NULL,
    discount_total DECIMAL(12,2) DEFAULT 0,
    tax_total DECIMAL(12,2) DEFAULT 0,
    rounding DECIMAL(5,2) DEFAULT 0,
    grand_total DECIMAL(12,2) NOT NULL,
    cost_of_goods_sold DECIMAL(12,2), -- For profit calc
    gross_profit DECIMAL(12,2), -- grand_total - COGS
    cashier_session_id BIGINT REFERENCES cashier_sessions(id),
    notes TEXT,
    created_by BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, bill_no)
);

CREATE INDEX idx_bills_tenant ON bills(tenant_id);
CREATE INDEX idx_bills_store ON bills(store_id);
CREATE INDEX idx_bills_customer ON bills(customer_id);
CREATE INDEX idx_bills_created ON bills(created_at);
CREATE INDEX idx_bills_status ON bills(bill_status);
CREATE INDEX idx_bills_session ON bills(cashier_session_id);
```

### bill_items

```sql
CREATE TABLE bill_items (
    id BIGSERIAL PRIMARY KEY,
    bill_id BIGINT NOT NULL REFERENCES bills(id),
    variant_id BIGINT NOT NULL REFERENCES product_variants(id),
    qty DECIMAL(12,3) NOT NULL,
    unit_selling_price DECIMAL(12,2) NOT NULL,
    unit_cost_snapshot DECIMAL(12,2), -- Critical for accurate profit
    discount_amount DECIMAL(12,2) DEFAULT 0,
    tax_percent DECIMAL(5,2),
    tax_amount DECIMAL(12,2),
    line_total DECIMAL(12,2) NOT NULL,
    batch_id BIGINT REFERENCES batches(id),
    serial_id BIGINT REFERENCES serial_numbers(id)
);

CREATE INDEX idx_bill_items_bill ON bill_items(bill_id);
CREATE INDEX idx_bill_items_variant ON bill_items(variant_id);
```

**On PAID**: Insert into `stock_ledger` with `txn_type='SALE'`, `qty=-qty`, `unit_cost=unit_cost_snapshot`, `reference_type='bill'`, `reference_id=bill_id`

### payments
Unified for bills AND orders

```sql
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    store_id BIGINT NOT NULL REFERENCES stores(id),
    entity_type VARCHAR(20) NOT NULL, -- BILL, ORDER, SUPPLIER_PAYMENT
    entity_id BIGINT NOT NULL,
    method VARCHAR(20) NOT NULL, -- CASH, CARD, UPI, WALLET, COD, BANK
    amount DECIMAL(12,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'SUCCESS', -- SUCCESS, PENDING, FAILED, REFUNDED
    provider VARCHAR(50), -- Razorpay, Stripe, etc.
    provider_ref VARCHAR(255),
    notes TEXT,
    paid_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payments_tenant ON payments(tenant_id);
CREATE INDEX idx_payments_entity ON payments(entity_type, entity_id);
CREATE INDEX idx_payments_method ON payments(method);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_paid ON payments(paid_at);
```

### refunds

```sql
CREATE TABLE refunds (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    payment_id BIGINT NOT NULL REFERENCES payments(id),
    amount DECIMAL(12,2) NOT NULL,
    reason TEXT,
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, PROCESSED, FAILED
    created_by BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_refunds_payment ON refunds(payment_id);
```

### cashier_sessions

```sql
CREATE TABLE cashier_sessions (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    store_id BIGINT NOT NULL REFERENCES stores(id),
    cashier_user_id BIGINT NOT NULL REFERENCES users(id),
    session_no VARCHAR(50) NOT NULL,
    opened_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    closed_at TIMESTAMP,
    opening_cash DECIMAL(12,2),
    closing_cash_declared DECIMAL(12,2),
    closing_cash_expected DECIMAL(12,2),
    variance DECIMAL(12,2),
    closing_notes TEXT,
    UNIQUE(tenant_id, session_no)
);

CREATE INDEX idx_cashier_sessions_tenant ON cashier_sessions(tenant_id);
CREATE INDEX idx_cashier_sessions_cashier ON cashier_sessions(cashier_user_id);
CREATE INDEX idx_cashier_sessions_opened ON cashier_sessions(opened_at);
```

---

## 6. Orders (Online + Offline)

### orders

```sql
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    store_id BIGINT NOT NULL REFERENCES stores(id),
    warehouse_id BIGINT NOT NULL REFERENCES warehouses(id),
    order_no VARCHAR(50) NOT NULL,
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    status VARCHAR(30) DEFAULT 'NEW', -- NEW, ACCEPTED, PACKED, OUT_FOR_DELIVERY, DELIVERED, CANCELLED, RETURNED
    channel VARCHAR(20) DEFAULT 'WEB', -- WEB, APP, POS, PHONE
    subtotal DECIMAL(12,2) NOT NULL,
    discount_total DECIMAL(12,2) DEFAULT 0,
    tax_total DECIMAL(12,2) DEFAULT 0,
    delivery_fee DECIMAL(12,2) DEFAULT 0,
    grand_total DECIMAL(12,2) NOT NULL,
    payment_status VARCHAR(20) DEFAULT 'UNPAID', -- UNPAID, PAID, PARTIAL, REFUNDED
    delivery_address_id BIGINT REFERENCES customer_addresses(id),
    delivery_slot_start TIMESTAMP,
    delivery_slot_end TIMESTAMP,
    notes TEXT,
    placed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, order_no)
);

CREATE INDEX idx_orders_tenant ON orders(tenant_id);
CREATE INDEX idx_orders_store ON orders(store_id);
CREATE INDEX idx_orders_customer ON orders(customer_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_placed ON orders(placed_at);
CREATE INDEX idx_orders_channel ON orders(channel);
```

### order_items

```sql
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    variant_id BIGINT NOT NULL REFERENCES product_variants(id),
    qty DECIMAL(12,3) NOT NULL,
    unit_price_snapshot DECIMAL(12,2) NOT NULL,
    unit_cost_snapshot DECIMAL(12,2),
    discount_amount DECIMAL(12,2) DEFAULT 0,
    tax_percent DECIMAL(5,2),
    tax_amount DECIMAL(12,2),
    line_total DECIMAL(12,2) NOT NULL,
    fulfillment_status VARCHAR(30) DEFAULT 'PENDING', -- PENDING, PACKED, SUBSTITUTED, CANCELLED
    substitute_variant_id BIGINT REFERENCES product_variants(id)
);

CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_order_items_variant ON order_items(variant_id);
```

### order_status_history

```sql
CREATE TABLE order_status_history (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    from_status VARCHAR(30),
    to_status VARCHAR(30) NOT NULL,
    note TEXT,
    changed_by BIGINT NOT NULL REFERENCES users(id),
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_order_status_history_order ON order_status_history(order_id);
CREATE INDEX idx_order_status_history_changed ON order_status_history(changed_at);
```

---

## 7. Delivery Management

### delivery_agents

```sql
CREATE TABLE delivery_agents (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    vehicle_type VARCHAR(50),
    vehicle_number VARCHAR(50),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_delivery_agents_tenant ON delivery_agents(tenant_id);
CREATE INDEX idx_delivery_agents_user ON delivery_agents(user_id);
```

### deliveries

```sql
CREATE TABLE deliveries (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    store_id BIGINT NOT NULL REFERENCES stores(id),
    order_id BIGINT NOT NULL REFERENCES orders(id),
    agent_id BIGINT REFERENCES delivery_agents(id),
    status VARCHAR(30) DEFAULT 'ASSIGNED', -- ASSIGNED, PICKED, IN_TRANSIT, DELIVERED, FAILED, CANCELLED
    otp_required BOOLEAN DEFAULT false,
    otp_code_hash VARCHAR(255),
    assigned_at TIMESTAMP,
    picked_at TIMESTAMP,
    delivered_at TIMESTAMP,
    proof_photo_url VARCHAR(500),
    customer_signature_url VARCHAR(500),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_deliveries_tenant ON deliveries(tenant_id);
CREATE INDEX idx_deliveries_order ON deliveries(order_id);
CREATE INDEX idx_deliveries_agent ON deliveries(agent_id);
CREATE INDEX idx_deliveries_status ON deliveries(status);
CREATE INDEX idx_deliveries_assigned ON deliveries(assigned_at);
```

---

## 8. Batch/Expiry/Serial (Phase 7)

### batches

```sql
CREATE TABLE batches (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    variant_id BIGINT NOT NULL REFERENCES product_variants(id),
    batch_code VARCHAR(100) NOT NULL,
    mfg_date DATE,
    expiry_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, variant_id, batch_code)
);

CREATE INDEX idx_batches_variant ON batches(variant_id);
CREATE INDEX idx_batches_expiry ON batches(expiry_date);
```

### serial_numbers

```sql
CREATE TABLE serial_numbers (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    variant_id BIGINT NOT NULL REFERENCES product_variants(id),
    serial_no VARCHAR(100) NOT NULL,
    status VARCHAR(20) DEFAULT 'IN_STOCK', -- IN_STOCK, SOLD, RETURNED, WARRANTY_CLAIM
    reference_type VARCHAR(50),
    reference_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, serial_no)
);

CREATE INDEX idx_serial_numbers_variant ON serial_numbers(variant_id);
CREATE INDEX idx_serial_numbers_status ON serial_numbers(status);
```

---

## 9. Offers, Coupons, Loyalty (Phase 2/3)

### coupons

```sql
CREATE TABLE coupons (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    code VARCHAR(50) NOT NULL,
    type VARCHAR(20) NOT NULL, -- PERCENT, FIXED, FREE_SHIPPING
    value DECIMAL(12,2) NOT NULL,
    min_cart_total DECIMAL(12,2) DEFAULT 0,
    max_discount DECIMAL(12,2),
    start_at TIMESTAMP,
    end_at TIMESTAMP,
    usage_limit_total INT,
    usage_limit_per_customer INT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    metadata_json JSONB, -- eligible categories/products
    UNIQUE(tenant_id, code)
);

CREATE INDEX idx_coupons_tenant ON coupons(tenant_id);
CREATE INDEX idx_coupons_code ON coupons(code);
CREATE INDEX idx_coupons_active ON coupons(is_active, start_at, end_at);
```

### order_discounts

```sql
CREATE TABLE order_discounts (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    coupon_id BIGINT REFERENCES coupons(id),
    discount_type VARCHAR(20),
    amount DECIMAL(12,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_order_discounts_order ON order_discounts(order_id);
CREATE INDEX idx_order_discounts_coupon ON order_discounts(coupon_id);
```

### loyalty_wallets

```sql
CREATE TABLE loyalty_wallets (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    points_balance INT DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, customer_id)
);

CREATE INDEX idx_loyalty_wallets_customer ON loyalty_wallets(customer_id);
```

### loyalty_transactions

```sql
CREATE TABLE loyalty_transactions (
    id BIGSERIAL PRIMARY KEY,
    wallet_id BIGINT NOT NULL REFERENCES loyalty_wallets(id),
    points_delta INT NOT NULL,
    reason VARCHAR(255),
    reference_type VARCHAR(50),
    reference_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_loyalty_transactions_wallet ON loyalty_transactions(wallet_id);
CREATE INDEX idx_loyalty_transactions_created ON loyalty_transactions(created_at);
```

---

## 10. Settings, Notifications, Audit

### settings

```sql
CREATE TABLE settings (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    store_id BIGINT REFERENCES stores(id), -- nullable = global
    key VARCHAR(100) NOT NULL,
    value_json JSONB NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, COALESCE(store_id, 0), key)
);

CREATE INDEX idx_settings_tenant ON settings(tenant_id);
CREATE INDEX idx_settings_key ON settings(key);
```

### notifications

```sql
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    user_id BIGINT REFERENCES users(id),
    type VARCHAR(50) NOT NULL, -- LOW_STOCK, NEW_ORDER, PAYMENT_FAILED, EXPIRY_ALERT
    title VARCHAR(255) NOT NULL,
    message TEXT,
    is_read BOOLEAN DEFAULT false,
    reference_type VARCHAR(50),
    reference_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notifications_tenant ON notifications(tenant_id);
CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);
CREATE INDEX idx_notifications_created ON notifications(created_at);
```

### audit_logs
Critical for compliance and debugging

```sql
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    user_id BIGINT REFERENCES users(id),
    action VARCHAR(100) NOT NULL, -- PRODUCT_PRICE_UPDATE, STOCK_ADJUSTMENT, REFUND_ISSUED, etc.
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    before_json JSONB,
    after_json JSONB,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_tenant ON audit_logs(tenant_id);
CREATE INDEX idx_audit_logs_user ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_created ON audit_logs(created_at);
```

---

## 11. Reporting Views (Optional Optimization)

### daily_sales_summary
Materialized view for fast reporting

```sql
CREATE MATERIALIZED VIEW daily_sales_summary AS
SELECT
    tenant_id,
    store_id,
    DATE(created_at) as sales_date,
    COUNT(*) as bill_count,
    SUM(subtotal) as gross_sales,
    SUM(discount_total) as total_discounts,
    SUM(tax_total) as total_tax,
    SUM(grand_total) as net_sales,
    SUM(cost_of_goods_sold) as total_cogs,
    SUM(gross_profit) as total_profit
FROM bills
WHERE bill_status IN ('PAID', 'PARTIAL')
GROUP BY tenant_id, store_id, DATE(created_at);

CREATE UNIQUE INDEX idx_daily_sales_summary ON daily_sales_summary(tenant_id, store_id, sales_date);

-- Refresh daily at midnight
-- REFRESH MATERIALIZED VIEW CONCURRENTLY daily_sales_summary;
```

---

## 12. Critical Constraints & Triggers

### Stock never negative (optional enforced trigger)

```sql
CREATE OR REPLACE FUNCTION check_negative_stock()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.qty_available < 0 THEN
        RAISE EXCEPTION 'Stock cannot be negative for variant_id %', NEW.variant_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_check_negative_stock
BEFORE INSERT OR UPDATE ON inventory_balances
FOR EACH ROW
EXECUTE FUNCTION check_negative_stock();
```

### Auto-update timestamps

```sql
CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply to all tables with updated_at
CREATE TRIGGER trigger_update_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE FUNCTION update_updated_at();

-- Repeat for: stores, products, variants, bills, orders, etc.
```

---

## 13. MVP Subset (What to build first)

**Phase 1 Tables** (Weeks 1-4):
- tenants, stores, users, roles, permissions, role_permissions, user_roles
- categories, products, product_variants, product_images
- tax_codes, price_lists, variant_prices
- warehouses, stock_ledger, inventory_balances
- purchase_receipts, purchase_receipt_items
- audit_logs

**Phase 2 Tables** (Weeks 4-6):
- customers, customer_addresses
- bills, bill_items, payments, refunds
- cashier_sessions

**Phase 3 Tables** (Weeks 6-9):
- orders, order_items, order_status_history

**Phase 4+** (Weeks 9+):
- suppliers, purchase_orders, purchase_order_items
- delivery_agents, deliveries
- batches, serial_numbers
- coupons, loyalty_wallets, loyalty_transactions
- stock_adjustments, stock_transfers

---

## Next Steps

1. **Review schema** - Confirm tables match your requirements
2. **Generate SQL DDL** - Full CREATE TABLE statements
3. **Create ER diagram** - Visual representation
4. **Define indexes** - Performance optimization
5. **Seed data** - Sample data for testing
6. **API design** - Map endpoints to tables

---

*Last Updated: 2026-02-28*
*Database: PostgreSQL 15+*
*Schema Version: 1.0*

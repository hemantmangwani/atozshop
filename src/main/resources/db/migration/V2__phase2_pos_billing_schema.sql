-- ========================================
-- Phase 2: POS Billing System Schema
-- Version: 2.0
-- Date: 2026-03-02
-- ========================================

-- ========================================
-- Table 1: customers
-- Customer information for tracking and loyalty programs
-- ========================================
CREATE TABLE IF NOT EXISTS customers (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    customer_code VARCHAR(50) NOT NULL,  -- Auto: CUST-YYYYMMDD-XXX
    name VARCHAR(200) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(255),
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    gstin VARCHAR(50),
    loyalty_points INTEGER DEFAULT 0,
    total_purchases DECIMAL(12,2) DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_customers_tenant_phone UNIQUE (tenant_id, phone),
    CONSTRAINT uk_customers_tenant_code UNIQUE (tenant_id, customer_code)
);

-- Indexes for customers
CREATE INDEX IF NOT EXISTS idx_customers_tenant ON customers(tenant_id);
CREATE INDEX IF NOT EXISTS idx_customers_phone ON customers(tenant_id, phone);
CREATE INDEX IF NOT EXISTS idx_customers_code ON customers(tenant_id, customer_code);
CREATE INDEX IF NOT EXISTS idx_customers_active ON customers(tenant_id, is_active);

COMMENT ON TABLE customers IS 'Customer master data for POS billing and loyalty tracking';
COMMENT ON COLUMN customers.customer_code IS 'Auto-generated unique customer code: CUST-YYYYMMDD-XXX';
COMMENT ON COLUMN customers.loyalty_points IS 'Accumulated loyalty points from purchases';
COMMENT ON COLUMN customers.total_purchases IS 'Total purchase amount lifetime';

-- ========================================
-- Table 2: bills
-- Sales bill/invoice headers with auto-generated bill numbers
-- ========================================
CREATE TABLE IF NOT EXISTS bills (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    customer_id BIGINT,  -- NULL for walk-in customers
    cashier_id BIGINT,   -- User who created the bill
    bill_number VARCHAR(50) NOT NULL,  -- Auto: BIL-YYYYMMDD-XXX
    bill_date TIMESTAMP NOT NULL,
    bill_type VARCHAR(20) NOT NULL,  -- SALES, SALES_RETURN
    total_items INTEGER DEFAULT 0,
    total_quantity INTEGER DEFAULT 0,
    subtotal DECIMAL(12,2) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(12,2) DEFAULT 0,
    tax_amount DECIMAL(12,2) DEFAULT 0,
    total_amount DECIMAL(12,2) NOT NULL,
    paid_amount DECIMAL(12,2) DEFAULT 0,
    balance_amount DECIMAL(12,2) DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',  -- DRAFT, CONFIRMED, CANCELLED
    payment_status VARCHAR(20) DEFAULT 'UNPAID',  -- UNPAID, PARTIAL, PAID, REFUNDED
    notes TEXT,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_bills_tenant_number UNIQUE (tenant_id, bill_number),
    CONSTRAINT fk_bills_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
);

-- Indexes for bills
CREATE INDEX IF NOT EXISTS idx_bills_tenant_store ON bills(tenant_id, store_id);
CREATE INDEX IF NOT EXISTS idx_bills_date ON bills(tenant_id, bill_date);
CREATE INDEX IF NOT EXISTS idx_bills_customer ON bills(customer_id);
CREATE INDEX IF NOT EXISTS idx_bills_status ON bills(tenant_id, status);
CREATE INDEX IF NOT EXISTS idx_bills_payment_status ON bills(tenant_id, payment_status);
CREATE INDEX IF NOT EXISTS idx_bills_number ON bills(tenant_id, bill_number);
CREATE INDEX IF NOT EXISTS idx_bills_cashier ON bills(cashier_id);

COMMENT ON TABLE bills IS 'Sales bill/invoice headers for POS transactions';
COMMENT ON COLUMN bills.bill_number IS 'Auto-generated unique bill number: BIL-YYYYMMDD-XXX';
COMMENT ON COLUMN bills.status IS 'Bill status: DRAFT (editable), CONFIRMED (stock deducted), CANCELLED';
COMMENT ON COLUMN bills.payment_status IS 'Payment status: UNPAID, PARTIAL, PAID, REFUNDED';

-- ========================================
-- Table 3: bill_items
-- Line items in bills with quantity, price, and calculations
-- ========================================
CREATE TABLE IF NOT EXISTS bill_items (
    id BIGSERIAL PRIMARY KEY,
    bill_id BIGINT NOT NULL,
    variant_id BIGINT NOT NULL,
    sku VARCHAR(50) NOT NULL,
    product_name VARCHAR(200) NOT NULL,   -- Snapshot for historical accuracy
    variant_name VARCHAR(200),             -- Snapshot for historical accuracy
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    mrp DECIMAL(10,2),
    discount_percent DECIMAL(5,2) DEFAULT 0,
    discount_amount DECIMAL(10,2) DEFAULT 0,
    subtotal DECIMAL(12,2) NOT NULL,       -- quantity × unit_price
    tax_percent DECIMAL(5,2) DEFAULT 0,
    tax_amount DECIMAL(10,2) DEFAULT 0,
    total_amount DECIMAL(12,2) NOT NULL,   -- Final amount after discount + tax
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bill_items_bill FOREIGN KEY (bill_id) REFERENCES bills(id) ON DELETE CASCADE
);

-- Indexes for bill_items
CREATE INDEX IF NOT EXISTS idx_bill_items_bill ON bill_items(bill_id);
CREATE INDEX IF NOT EXISTS idx_bill_items_variant ON bill_items(variant_id);
CREATE INDEX IF NOT EXISTS idx_bill_items_sku ON bill_items(sku);

COMMENT ON TABLE bill_items IS 'Line items in sales bills with price snapshots';
COMMENT ON COLUMN bill_items.product_name IS 'Product name snapshot at time of sale';
COMMENT ON COLUMN bill_items.variant_name IS 'Variant name snapshot at time of sale';
COMMENT ON COLUMN bill_items.subtotal IS 'quantity × unit_price (before discount and tax)';
COMMENT ON COLUMN bill_items.total_amount IS 'Final item total after discount and tax';

-- ========================================
-- Table 4: payments
-- Payment transactions with support for multiple payment methods
-- ========================================
CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    bill_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    payment_method VARCHAR(20) NOT NULL,  -- CASH, CARD, UPI, WALLET, CHEQUE
    payment_date TIMESTAMP NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    reference_number VARCHAR(100),        -- Transaction ID for digital payments
    card_last4 VARCHAR(4),                -- Last 4 digits of card
    upi_id VARCHAR(100),                  -- UPI ID
    bank_name VARCHAR(100),               -- For cards/cheques
    notes TEXT,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payments_bill FOREIGN KEY (bill_id) REFERENCES bills(id) ON DELETE CASCADE
);

-- Indexes for payments
CREATE INDEX IF NOT EXISTS idx_payments_bill ON payments(bill_id);
CREATE INDEX IF NOT EXISTS idx_payments_date ON payments(tenant_id, payment_date);
CREATE INDEX IF NOT EXISTS idx_payments_method ON payments(tenant_id, payment_method);
CREATE INDEX IF NOT EXISTS idx_payments_reference ON payments(reference_number);

COMMENT ON TABLE payments IS 'Payment transactions for bills with multiple payment method support';
COMMENT ON COLUMN payments.payment_method IS 'Payment method: CASH, CARD, UPI, WALLET, CHEQUE';
COMMENT ON COLUMN payments.reference_number IS 'Transaction ID/reference for digital payments';

-- ========================================
-- Table 5: discounts
-- Discount/offer definitions with flexible rules
-- ========================================
CREATE TABLE IF NOT EXISTS discounts (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    discount_code VARCHAR(50) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    discount_type VARCHAR(20) NOT NULL,     -- PERCENTAGE, FIXED_AMOUNT
    discount_value DECIMAL(10,2) NOT NULL,  -- 10 for 10%, or 500 for ₹500 off
    min_purchase_amount DECIMAL(12,2),      -- Minimum bill amount required
    max_discount_amount DECIMAL(12,2),      -- Cap on discount amount
    applicable_on VARCHAR(20) NOT NULL,     -- ITEM, BILL, CATEGORY
    valid_from DATE,
    valid_to DATE,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_discounts_tenant_code UNIQUE (tenant_id, discount_code)
);

-- Indexes for discounts
CREATE INDEX IF NOT EXISTS idx_discounts_tenant ON discounts(tenant_id);
CREATE INDEX IF NOT EXISTS idx_discounts_active ON discounts(tenant_id, is_active);
CREATE INDEX IF NOT EXISTS idx_discounts_code ON discounts(tenant_id, discount_code);
CREATE INDEX IF NOT EXISTS idx_discounts_dates ON discounts(tenant_id, valid_from, valid_to);

COMMENT ON TABLE discounts IS 'Discount and promotional offer definitions';
COMMENT ON COLUMN discounts.discount_type IS 'Discount type: PERCENTAGE (10 = 10%), FIXED_AMOUNT (500 = ₹500 off)';
COMMENT ON COLUMN discounts.applicable_on IS 'Where discount applies: ITEM (specific products), BILL (entire bill), CATEGORY';

-- ========================================
-- Table 6: bill_discounts
-- Discounts applied to specific bills (historical record)
-- ========================================
CREATE TABLE IF NOT EXISTS bill_discounts (
    id BIGSERIAL PRIMARY KEY,
    bill_id BIGINT NOT NULL,
    discount_id BIGINT,                     -- NULL if ad-hoc discount
    discount_name VARCHAR(200) NOT NULL,    -- Snapshot
    discount_code VARCHAR(50),              -- Snapshot
    discount_type VARCHAR(20) NOT NULL,     -- Snapshot
    discount_value DECIMAL(10,2),           -- Snapshot
    discount_amount DECIMAL(12,2) NOT NULL, -- Actual amount deducted
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bill_discounts_bill FOREIGN KEY (bill_id) REFERENCES bills(id) ON DELETE CASCADE
);

-- Indexes for bill_discounts
CREATE INDEX IF NOT EXISTS idx_bill_discounts_bill ON bill_discounts(bill_id);
CREATE INDEX IF NOT EXISTS idx_bill_discounts_discount ON bill_discounts(discount_id);

COMMENT ON TABLE bill_discounts IS 'Historical record of discounts applied to bills';
COMMENT ON COLUMN bill_discounts.discount_id IS 'Reference to discount master (NULL for ad-hoc discounts)';
COMMENT ON COLUMN bill_discounts.discount_amount IS 'Actual discount amount applied to the bill';

-- ========================================
-- Grant permissions (if needed)
-- ========================================
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO atozshop;
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO atozshop;

-- ========================================
-- End of Phase 2 Schema Migration
-- ========================================

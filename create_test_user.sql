-- Create test admin user for login testing
-- Password: admin123 (BCrypt encrypted)

-- First, ensure tenant exists
INSERT INTO tenants (name, code, email, phone, is_active, created_at, updated_at)
VALUES ('Test Shop', 'TEST01', 'test@shop.com', '1234567890', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- Create admin user (password: admin123)
-- BCrypt hash for "admin123": $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
INSERT INTO users (tenant_id, email, password, first_name, last_name, role, is_active, created_at, updated_at)
SELECT
    (SELECT id FROM tenants WHERE code = 'TEST01'),
    'admin@atozshop.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Admin',
    'User',
    'ADMIN',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@atozshop.com');

-- Create customer user (password: customer123)
-- BCrypt hash for "customer123": $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi
INSERT INTO users (tenant_id, email, password, first_name, last_name, role, is_active, created_at, updated_at)
SELECT
    (SELECT id FROM tenants WHERE code = 'TEST01'),
    'customer@atozshop.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'Customer',
    'User',
    'CUSTOMER',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'customer@atozshop.com');

-- Verify users created
SELECT id, email, role, is_active FROM users WHERE email IN ('admin@atozshop.com', 'customer@atozshop.com');

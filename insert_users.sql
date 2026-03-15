-- Insert Test Users for AtoZShop
-- Database: atozshop
-- Connection: jdbc:postgresql://localhost:5432/atozshop
-- Username: atozshop
-- Password: atozshop123

-- Delete existing test users (if any)
DELETE FROM users WHERE email IN ('admin@atozshop.com', 'customer@atozshop.com');

-- Insert Admin User
-- Email: admin@atozshop.com
-- Password: admin123
INSERT INTO users (
    tenant_id,
    email,
    password,
    first_name,
    last_name,
    role,
    is_active,
    created_at,
    updated_at
)
VALUES (
    1,
    'admin@atozshop.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Admin',
    'User',
    'ADMIN',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Insert Customer User
-- Email: customer@atozshop.com
-- Password: customer123
INSERT INTO users (
    tenant_id,
    email,
    password,
    first_name,
    last_name,
    role,
    is_active,
    created_at,
    updated_at
)
VALUES (
    1,
    'customer@atozshop.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'Customer',
    'User',
    'CUSTOMER',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Verify users were created
SELECT
    id,
    email,
    role,
    is_active,
    created_at
FROM users
ORDER BY id;

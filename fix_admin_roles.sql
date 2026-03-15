-- Fix Admin User Roles
-- This script assigns the ADMIN role to the admin user

-- First, ensure the ADMIN role exists in the roles table
INSERT INTO roles (tenant_id, name, description, is_system, created_at, updated_at)
VALUES (1, 'ADMIN', 'Administrator role with full access', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (tenant_id, name) DO NOTHING;

-- Get the role_id for ADMIN role
DO $$
DECLARE
    admin_role_id BIGINT;
    admin_user_id BIGINT;
BEGIN
    -- Get ADMIN role ID
    SELECT id INTO admin_role_id FROM roles WHERE name = 'ADMIN' AND tenant_id = 1;

    -- Get admin user ID
    SELECT id INTO admin_user_id FROM users WHERE email = 'admin@atozshop.com' AND tenant_id = 1;

    -- Delete existing role assignments for admin user
    DELETE FROM user_roles WHERE user_id = admin_user_id;

    -- Assign ADMIN role to admin user
    INSERT INTO user_roles (user_id, role_id)
    VALUES (admin_user_id, admin_role_id);

    RAISE NOTICE 'Admin role assigned successfully to user ID: %', admin_user_id;
END $$;

-- Also ensure CUSTOMER role exists and is assigned to customer user
INSERT INTO roles (tenant_id, name, description, is_system, created_at, updated_at)
VALUES (1, 'CUSTOMER', 'Customer role for shoppers', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (tenant_id, name) DO NOTHING;

DO $$
DECLARE
    customer_role_id BIGINT;
    customer_user_id BIGINT;
BEGIN
    -- Get CUSTOMER role ID
    SELECT id INTO customer_role_id FROM roles WHERE name = 'CUSTOMER' AND tenant_id = 1;

    -- Get customer user ID
    SELECT id INTO customer_user_id FROM users WHERE email = 'customer@atozshop.com' AND tenant_id = 1;

    -- Delete existing role assignments for customer user
    DELETE FROM user_roles WHERE user_id = customer_user_id;

    -- Assign CUSTOMER role to customer user
    INSERT INTO user_roles (user_id, role_id)
    VALUES (customer_user_id, customer_role_id);

    RAISE NOTICE 'Customer role assigned successfully to user ID: %', customer_user_id;
END $$;

-- Verify the role assignments
SELECT
    u.id as user_id,
    u.email,
    u.first_name,
    u.last_name,
    r.name as role
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id
WHERE u.email IN ('admin@atozshop.com', 'customer@atozshop.com')
ORDER BY u.email, r.name;

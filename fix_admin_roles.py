#!/usr/bin/env python3
"""
Fix Admin User Roles
This script assigns the ADMIN role to the admin@atozshop.com user
"""

import psycopg2
from psycopg2 import sql

# Database connection parameters
DB_CONFIG = {
    'host': 'localhost',
    'port': 5432,
    'database': 'atozshop',
    'user': 'atozshop',
    'password': 'atozshop123'
}

def fix_admin_roles():
    try:
        # Connect to database
        print("Connecting to database...")
        conn = psycopg2.connect(**DB_CONFIG)
        cur = conn.cursor()

        # Create ADMIN role if it doesn't exist
        print("Creating ADMIN role if not exists...")
        cur.execute("""
            INSERT INTO roles (tenant_id, name, description, is_system, created_at, updated_at)
            VALUES (1, 'ADMIN', 'Administrator role with full access', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (tenant_id, name) DO NOTHING
        """)

        # Create CUSTOMER role if it doesn't exist
        print("Creating CUSTOMER role if not exists...")
        cur.execute("""
            INSERT INTO roles (tenant_id, name, description, is_system, created_at, updated_at)
            VALUES (1, 'CUSTOMER', 'Customer role for shoppers', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (tenant_id, name) DO NOTHING
        """)

        # Get ADMIN role ID
        cur.execute("SELECT id FROM roles WHERE name = 'ADMIN' AND tenant_id = 1")
        admin_role_result = cur.fetchone()
        if not admin_role_result:
            print("ERROR: ADMIN role not found!")
            return
        admin_role_id = admin_role_result[0]
        print(f"ADMIN role ID: {admin_role_id}")

        # Get CUSTOMER role ID
        cur.execute("SELECT id FROM roles WHERE name = 'CUSTOMER' AND tenant_id = 1")
        customer_role_result = cur.fetchone()
        if not customer_role_result:
            print("ERROR: CUSTOMER role not found!")
            return
        customer_role_id = customer_role_result[0]
        print(f"CUSTOMER role ID: {customer_role_id}")

        # Get admin user ID
        cur.execute("SELECT id FROM users WHERE email = 'admin@atozshop.com' AND tenant_id = 1")
        admin_user_result = cur.fetchone()
        if not admin_user_result:
            print("ERROR: admin@atozshop.com user not found!")
            return
        admin_user_id = admin_user_result[0]
        print(f"Admin user ID: {admin_user_id}")

        # Get customer user ID
        cur.execute("SELECT id FROM users WHERE email = 'customer@atozshop.com' AND tenant_id = 1")
        customer_user_result = cur.fetchone()
        if not customer_user_result:
            print("ERROR: customer@atozshop.com user not found!")
            return
        customer_user_id = customer_user_result[0]
        print(f"Customer user ID: {customer_user_id}")

        # Clear existing role assignments for admin user
        print(f"Clearing existing roles for admin user...")
        cur.execute("DELETE FROM user_roles WHERE user_id = %s", (admin_user_id,))

        # Assign ADMIN role to admin user
        print(f"Assigning ADMIN role to admin user...")
        cur.execute("""
            INSERT INTO user_roles (user_id, role_id)
            VALUES (%s, %s)
            ON CONFLICT DO NOTHING
        """, (admin_user_id, admin_role_id))

        # Clear existing role assignments for customer user
        print(f"Clearing existing roles for customer user...")
        cur.execute("DELETE FROM user_roles WHERE user_id = %s", (customer_user_id,))

        # Assign CUSTOMER role to customer user
        print(f"Assigning CUSTOMER role to customer user...")
        cur.execute("""
            INSERT INTO user_roles (user_id, role_id)
            VALUES (%s, %s)
            ON CONFLICT DO NOTHING
        """, (customer_user_id, customer_role_id))

        # Commit changes
        conn.commit()
        print("\n✅ Roles assigned successfully!")

        # Verify the assignments
        print("\nVerifying role assignments...")
        cur.execute("""
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
            ORDER BY u.email, r.name
        """)

        results = cur.fetchall()
        print("\nCurrent user role assignments:")
        print("-" * 80)
        for row in results:
            print(f"User ID: {row[0]}, Email: {row[1]}, Name: {row[2]} {row[3]}, Role: {row[4]}")
        print("-" * 80)

        # Close connection
        cur.close()
        conn.close()
        print("\n✅ Done! You can now log in with admin@atozshop.com")

    except Exception as e:
        print(f"❌ Error: {e}")
        if 'conn' in locals():
            conn.rollback()
        raise

if __name__ == "__main__":
    fix_admin_roles()

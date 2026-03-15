#!/usr/bin/env python3
"""
Create Customer Records for Users
This script creates customer records for admin and customer users
"""

import psycopg2
from datetime import datetime

# Database connection parameters
DB_CONFIG = {
    'host': 'localhost',
    'port': 5432,
    'database': 'atozshop',
    'user': 'atozshop',
    'password': 'atozshop123'
}

def create_customer_records():
    try:
        # Connect to database
        print("Connecting to database...")
        conn = psycopg2.connect(**DB_CONFIG)
        cur = conn.cursor()

        # Get users
        cur.execute("""
            SELECT id, email, first_name, last_name, phone, tenant_id
            FROM users
            WHERE email IN ('admin@atozshop.com', 'customer@atozshop.com')
            ORDER BY email
        """)
        users = cur.fetchall()

        if not users:
            print("ERROR: No users found!")
            return

        for user in users:
            user_id, email, first_name, last_name, phone, tenant_id = user

            # Generate customer code
            date_str = datetime.now().strftime('%Y%m%d')

            # Check if customer already exists for this email
            cur.execute("""
                SELECT id FROM customers WHERE email = %s AND tenant_id = %s
            """, (email, tenant_id))

            existing_customer = cur.fetchone()

            if existing_customer:
                customer_id = existing_customer[0]
                print(f"✓ Customer already exists for {email} (ID: {customer_id})")
                continue

            # Get next sequence number for today
            cur.execute("""
                SELECT COUNT(*) FROM customers
                WHERE tenant_id = %s AND customer_code LIKE %s
            """, (tenant_id, f'CUST-{date_str}-%'))

            count = cur.fetchone()[0]
            seq = count + 1
            customer_code = f'CUST-{date_str}-{seq:03d}'

            # Create customer record
            name = f"{first_name} {last_name}".strip() if first_name or last_name else email.split('@')[0]
            phone_number = phone if phone else '0000000000'  # Default phone if not set

            cur.execute("""
                INSERT INTO customers (
                    tenant_id, customer_code, name, phone, email,
                    loyalty_points, total_purchases, is_active,
                    created_at, updated_at
                )
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                RETURNING id
            """, (tenant_id, customer_code, name, phone_number, email, 0, 0.0, True))

            customer_id = cur.fetchone()[0]
            print(f"✅ Created customer record for {email}")
            print(f"   - Customer ID: {customer_id}")
            print(f"   - Customer Code: {customer_code}")
            print(f"   - Name: {name}")

        # Commit changes
        conn.commit()
        print("\n✅ Customer records created successfully!")

        # Display final mapping
        print("\nUser ID to Customer ID mapping:")
        print("-" * 80)
        cur.execute("""
            SELECT u.id, u.email, c.id, c.customer_code, c.name
            FROM users u
            LEFT JOIN customers c ON u.email = c.email AND u.tenant_id = c.tenant_id
            WHERE u.email IN ('admin@atozshop.com', 'customer@atozshop.com')
            ORDER BY u.email
        """)

        results = cur.fetchall()
        for row in results:
            print(f"User ID: {row[0]}, Email: {row[1]} → Customer ID: {row[2]}, Code: {row[3]}, Name: {row[4]}")
        print("-" * 80)

        # Close connection
        cur.close()
        conn.close()

    except Exception as e:
        print(f"❌ Error: {e}")
        if 'conn' in locals():
            conn.rollback()
        raise

if __name__ == "__main__":
    create_customer_records()

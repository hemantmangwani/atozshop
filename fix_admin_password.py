#!/usr/bin/env python3
"""Fix admin user password by copying hash from working user"""

import psycopg2

try:
    conn = psycopg2.connect(
        host='localhost',
        port=5432,
        database='atozshop',
        user='atozshop',
        password='atozshop123'
    )

    cursor = conn.cursor()

    # Get the working password hash from testadmin user
    cursor.execute("SELECT password_hash FROM users WHERE email = 'testadmin@atozshop.com'")
    row = cursor.fetchone()

    if row:
        working_hash = row[0]
        print(f"Got working hash: {working_hash}")

        # Update admin user with this hash
        cursor.execute("""
            UPDATE users
            SET password_hash = %s
            WHERE email = 'admin@atozshop.com'
        """, (working_hash,))

        conn.commit()
        print("✅ Updated admin@atozshop.com password")

        # Also update customer user
        cursor.execute("""
            UPDATE users
            SET password_hash = %s
            WHERE email = 'customer@atozshop.com'
        """, (working_hash,))

        conn.commit()
        print("✅ Updated customer@atozshop.com password")

        print("\nBoth users now have password: admin123")
    else:
        print("Test user not found!")

    cursor.close()
    conn.close()

except Exception as e:
    print(f'Error: {e}')

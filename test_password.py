#!/usr/bin/env python3
"""Test if the stored password hash matches"""

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

    # Get admin user password hash
    cursor.execute("SELECT id, email, password_hash FROM users WHERE email = 'admin@atozshop.com'")
    row = cursor.fetchone()

    if row:
        print(f"User ID: {row[0]}")
        print(f"Email: {row[1]}")
        print(f"Password Hash: {row[2]}")
        print(f"\nExpected Hash: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy")
        print(f"Match: {row[2] == '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'}")
    else:
        print("User not found!")

    cursor.close()
    conn.close()

except Exception as e:
    print(f'Error: {e}')

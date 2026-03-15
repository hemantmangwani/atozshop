#!/usr/bin/env python3
"""Insert test users into AtoZShop database"""

try:
    import psycopg2
except ImportError:
    print("❌ psycopg2 not installed. Installing...")
    import subprocess
    subprocess.run(['pip3', 'install', '--user', 'psycopg2-binary'], check=True)
    import psycopg2

try:
    # Connect to database
    conn = psycopg2.connect(
        host='localhost',
        port=5432,
        database='atozshop',
        user='atozshop',
        password='atozshop123'
    )
    print('✅ Connected to database successfully!')

    cursor = conn.cursor()

    # Delete existing test users
    cursor.execute("DELETE FROM users WHERE email IN ('admin@atozshop.com', 'customer@atozshop.com')")
    deleted_count = cursor.rowcount
    if deleted_count > 0:
        print(f'🗑️  Deleted {deleted_count} existing test user(s)')

    # Insert admin user (using password_hash column and username column)
    cursor.execute("""
        INSERT INTO users (tenant_id, email, password_hash, first_name, last_name, username, is_active, email_verified, phone_verified, created_at, updated_at)
        VALUES (1, 'admin@atozshop.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin', 'User', 'admin', true, true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        RETURNING id
    """)
    admin_id = cursor.fetchone()[0]
    print(f'✅ Admin user created with ID: {admin_id}')

    # Insert customer user
    cursor.execute("""
        INSERT INTO users (tenant_id, email, password_hash, first_name, last_name, username, is_active, email_verified, phone_verified, created_at, updated_at)
        VALUES (1, 'customer@atozshop.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Customer', 'User', 'customer', true, true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        RETURNING id
    """)
    customer_id = cursor.fetchone()[0]
    print(f'✅ Customer user created with ID: {customer_id}')

    # Commit transaction
    conn.commit()

    # Verify users
    cursor.execute('SELECT id, email, username, is_active FROM users ORDER BY id')
    print('\n📋 All users in database:')
    for row in cursor.fetchall():
        active_str = 'Yes' if row[3] else 'No'
        print(f'   ID: {row[0]} | Email: {row[1]} | Username: {row[2]} | Active: {active_str}')

    print('\n✅ Test users ready!')
    print('\n📝 Login Credentials:')
    print('   Admin:')
    print('     Email: admin@atozshop.com')
    print('     Password: admin123')
    print()
    print('   Customer:')
    print('     Email: customer@atozshop.com')
    print('     Password: customer123')

    cursor.close()
    conn.close()

except Exception as e:
    print(f'❌ Error: {e}')
    import traceback
    traceback.print_exc()

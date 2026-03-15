#!/usr/bin/env python3
"""Check the actual users table schema"""

import psycopg2

try:
    conn = psycopg2.connect(
        host='localhost',
        port=5432,
        database='atozshop',
        user='atozshop',
        password='atozshop123'
    )
    print('✅ Connected to database')

    cursor = conn.cursor()

    # Get column information
    cursor.execute("""
        SELECT column_name, data_type, is_nullable
        FROM information_schema.columns
        WHERE table_name = 'users'
        ORDER BY ordinal_position
    """)

    print('\n📋 Users table schema:')
    print('-' * 60)
    for row in cursor.fetchall():
        print(f'{row[0]:20} | {row[1]:20} | Nullable: {row[2]}')
    print('-' * 60)

    cursor.close()
    conn.close()

except Exception as e:
    print(f'❌ Error: {e}')

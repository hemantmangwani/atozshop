#!/usr/bin/env python3
"""Check products and variants in database"""

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

    # Check products
    cursor.execute("SELECT id, name, category_id FROM products LIMIT 5")
    products = cursor.fetchall()
    print("📦 Products in database:")
    for p in products:
        print(f"   ID: {p[0]} | Name: {p[1]} | Category: {p[2]}")

    print()

    # Check categories
    cursor.execute("SELECT id, name FROM categories LIMIT 5")
    categories = cursor.fetchall()
    print("📁 Categories in database:")
    for c in categories:
        print(f"   ID: {c[0]} | Name: {c[1]}")

    print()

    # Check product variants
    cursor.execute("SELECT id, product_id, name, sku FROM product_variants LIMIT 5")
    variants = cursor.fetchall()
    print("🏷️ Product Variants in database:")
    if variants:
        for v in variants:
            print(f"   ID: {v[0]} | Product: {v[1]} | Name: {v[2]} | SKU: {v[3]}")
    else:
        print("   ⚠️  No variants found!")

    print()

    # Check variant prices
    cursor.execute("SELECT variant_id, store_id, selling_price, mrp FROM variant_prices LIMIT 5")
    prices = cursor.fetchall()
    print("💰 Variant Prices in database:")
    if prices:
        for p in prices:
            print(f"   Variant: {p[0]} | Store: {p[1]} | Price: ₹{p[2]} | MRP: ₹{p[3]}")
    else:
        print("   ⚠️  No prices found!")

    cursor.close()
    conn.close()

except Exception as e:
    print(f'Error: {e}')

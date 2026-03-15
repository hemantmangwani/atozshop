#!/bin/bash

echo "🧪 Testing After Database Fixes"
echo "================================"
echo ""

# Test 1: Customer login
echo "1️⃣ Testing customer login..."
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"customer@atozshop.com","password":"admin123"}')

TOKEN=$(echo "$LOGIN_RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin).get('token', ''))" 2>/dev/null)

if [ -n "$TOKEN" ]; then
    echo "   ✅ Customer login successful!"
    echo "   📝 User: $(echo "$LOGIN_RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin).get('fullName', ''))")"
else
    echo "   ❌ Customer login failed!"
    echo "   Response: $LOGIN_RESPONSE"
    exit 1
fi

echo ""

# Test 2: Get products
echo "2️⃣ Testing products API..."
PRODUCTS_RESPONSE=$(curl -s -X GET "http://localhost:8080/api/v1/products?tenantId=1&page=0&size=10" \
  -H "Authorization: Bearer $TOKEN")

PRODUCTS_COUNT=$(echo "$PRODUCTS_RESPONSE" | python3 -c "import sys, json; data=json.load(sys.stdin); print(len(data.get('content', [])))" 2>/dev/null)

if [ -n "$PRODUCTS_COUNT" ] && [ "$PRODUCTS_COUNT" -gt 0 ]; then
    echo "   ✅ Products API working!"
    echo "   📦 Products found: $PRODUCTS_COUNT"
else
    echo "   ❌ No products found!"
fi

echo ""

# Test 3: Check each product for variants and prices
echo "3️⃣ Testing product variants and prices..."

python3 - << 'EOF'
import psycopg2
import sys

try:
    conn = psycopg2.connect(
        host='localhost',
        port=5432,
        database='atozshop',
        user='atozshop',
        password='atozshop123'
    )

    cursor = conn.cursor()

    cursor.execute("""
        SELECT p.name, pv.variant_name, vp.selling_price, vp.mrp,
               COALESCE(SUM(sl.quantity_change), 0) as stock
        FROM products p
        JOIN product_variants pv ON p.id = pv.product_id
        JOIN variant_prices vp ON pv.id = vp.variant_id
        LEFT JOIN stock_ledger sl ON pv.id = sl.variant_id AND vp.store_id = sl.store_id
        WHERE p.tenant_id = 1
        GROUP BY p.name, pv.variant_name, vp.selling_price, vp.mrp
        ORDER BY p.name, pv.variant_name
    """)

    products = cursor.fetchall()
    all_good = True

    for p in products:
        has_price = p[2] and p[2] > 0
        has_mrp = p[3] and p[3] > 0
        has_stock = p[4] > 0

        status = "✅" if (has_price and has_mrp and has_stock) else "❌"

        print(f"   {status} {p[0]} - {p[1]}")
        print(f"      Price: ₹{p[2] if has_price else 0:,.2f} | MRP: ₹{p[3] if has_mrp else 0:,.2f} | Stock: {p[4]}")

        if not (has_price and has_mrp and has_stock):
            all_good = False

    cursor.close()
    conn.close()

    sys.exit(0 if all_good else 1)

except Exception as e:
    print(f"   ❌ Database check failed: {e}")
    sys.exit(1)
EOF

if [ $? -eq 0 ]; then
    echo ""
    echo "   ✅ All products have prices and stock!"
else
    echo ""
    echo "   ❌ Some products missing prices or stock!"
fi

echo ""
echo "================================"
echo "✅ Testing Complete!"
echo ""
echo "📝 Login Credentials:"
echo "   Customer: customer@atozshop.com / admin123"
echo "   Admin: admin@atozshop.com / admin123"
echo ""
echo "🌐 Test manually at:"
echo "   Frontend: http://localhost:5173"
echo "   Backend API: http://localhost:8080/swagger-ui/index.html"
echo ""

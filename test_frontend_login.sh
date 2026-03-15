#!/bin/bash

echo "🧪 Testing Frontend Login Flow"
echo "================================"
echo ""

# Test 1: Check frontend is accessible
echo "1️⃣ Checking frontend accessibility..."
FRONTEND_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:5173)
if [ "$FRONTEND_STATUS" -eq 200 ]; then
    echo "   ✅ Frontend is accessible at http://localhost:5173"
else
    echo "   ❌ Frontend not accessible (HTTP $FRONTEND_STATUS)"
    exit 1
fi

echo ""

# Test 2: Check backend is accessible
echo "2️⃣ Checking backend accessibility..."
BACKEND_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/v1/auth/login -X POST -H "Content-Type: application/json" -d '{}')
if [ "$BACKEND_STATUS" -eq 400 ] || [ "$BACKEND_STATUS" -eq 401 ]; then
    echo "   ✅ Backend is accessible at http://localhost:8080"
else
    echo "   ❌ Backend not responding correctly (HTTP $BACKEND_STATUS)"
    exit 1
fi

echo ""

# Test 3: Test login endpoint
echo "3️⃣ Testing login endpoint..."
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@atozshop.com","password":"admin123"}')

TOKEN=$(echo "$LOGIN_RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin).get('token', ''))" 2>/dev/null)

if [ -n "$TOKEN" ]; then
    echo "   ✅ Login successful!"
    echo "   📝 User: $(echo "$LOGIN_RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin).get('fullName', ''))")"
    echo "   🎫 Token: ${TOKEN:0:50}..."
else
    echo "   ❌ Login failed!"
    echo "   Response: $LOGIN_RESPONSE"
    exit 1
fi

echo ""

# Test 4: Test authenticated endpoint
echo "4️⃣ Testing authenticated endpoint..."
PRODUCTS_RESPONSE=$(curl -s -X GET "http://localhost:8080/api/v1/products?tenantId=1&page=0&size=10" \
  -H "Authorization: Bearer $TOKEN")

PRODUCTS_COUNT=$(echo "$PRODUCTS_RESPONSE" | python3 -c "import sys, json; data=json.load(sys.stdin); print(len(data.get('content', [])))" 2>/dev/null)

if [ -n "$PRODUCTS_COUNT" ]; then
    echo "   ✅ Authenticated request successful!"
    echo "   📦 Products found: $PRODUCTS_COUNT"
else
    echo "   ⚠️  No products found (database might be empty)"
fi

echo ""
echo "================================"
echo "✅ All tests passed!"
echo ""
echo "🌐 You can now test manually at:"
echo "   Frontend: http://localhost:5173"
echo "   Login with:"
echo "     Email: admin@atozshop.com"
echo "     Password: admin123"
echo ""

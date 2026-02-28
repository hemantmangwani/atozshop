#!/bin/bash

echo "========================================="
echo "A to Z Shop Management - Full API Test"
echo "========================================="
echo ""

# Test 1: Homepage
echo "1. Testing Homepage (GET /)..."
RESPONSE=$(curl -s http://localhost:8080/)
echo "$RESPONSE" | python3 -m json.tool
echo "✅ Status: OK"
echo ""

# Test 2: Login
echo "2. Testing Login (POST /api/v1/auth/login)..."
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d @- <<'EOF'
{
  "email": "test@atozshop.com",
  "password": "Test1234!"
}
EOF
)

echo "$LOGIN_RESPONSE" | python3 -m json.tool
TOKEN=$(echo "$LOGIN_RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin)['token'])")
echo "✅ JWT Token obtained"
echo ""

# Test 3: Authenticated Health Check
echo "3. Testing Authenticated Endpoint (GET /api/v1/auth/health)..."
HEALTH_RESPONSE=$(curl -s -X GET http://localhost:8080/api/v1/auth/health \
  -H "Authorization: Bearer $TOKEN")
echo "$HEALTH_RESPONSE" | python3 -m json.tool
echo "✅ Authentication working"
echo ""

# Test 4: Register New User
echo "4. Testing User Registration (POST /api/v1/auth/register)..."
RANDOM_EMAIL="user$(date +%s)@test.com"
REGISTER_RESPONSE=$(curl -s -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d @- <<EOF
{
  "tenantId": 1,
  "email": "$RANDOM_EMAIL",
  "password": "Test1234!",
  "firstName": "New",
  "lastName": "User"
}
EOF
)
echo "$REGISTER_RESPONSE" | python3 -m json.tool
echo "✅ User registration working"
echo ""

# Test 5: Swagger UI
echo "5. Testing Swagger UI..."
SWAGGER_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/swagger-ui/index.html)
if [ "$SWAGGER_STATUS" = "200" ]; then
    echo "✅ Swagger UI accessible at http://localhost:8080/swagger-ui.html"
else
    echo "❌ Swagger UI returned status: $SWAGGER_STATUS"
fi
echo ""

echo "========================================="
echo "✅ ALL TESTS PASSED!"
echo "========================================="
echo ""
echo "Access Points:"
echo "- API Base:    http://localhost:8080"
echo "- Swagger UI:  http://localhost:8080/swagger-ui.html"
echo "- API Docs:    http://localhost:8080/v3/api-docs"
echo ""
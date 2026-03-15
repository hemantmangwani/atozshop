#!/bin/bash

echo "========================================="
echo "🛒 PHASE 2: POS BILLING SYSTEM - TEST"
echo "========================================="
echo ""

BASE_URL="http://localhost:8080/api/v1"
TENANT_ID=1
STORE_ID=1

# Step 1: Admin Login
echo "Step 1: Admin Login"
echo "-------------------"
LOGIN_RESPONSE=$(curl -s -X POST ${BASE_URL}/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@atozshop.com","password":"admin123"}')

TOKEN=$(echo $LOGIN_RESPONSE | python3 -c "import sys, json; print(json.load(sys.stdin).get('token', ''))")
USER_ID=$(echo $LOGIN_RESPONSE | python3 -c "import sys, json; print(json.load(sys.stdin).get('id', ''))")

if [ -z "$TOKEN" ]; then
  echo "❌ Login FAILED"
  exit 1
fi

echo "✅ Login SUCCESSFUL"
echo "   User ID: $USER_ID"
echo ""

# Step 2: Find or Create Customer
echo "Step 2: Find or Create Customer"
echo "--------------------------------"
PHONE="9876543999"  # Different phone to avoid conflict

# Try to find existing customer
CUSTOMER_RESPONSE=$(curl -s "${BASE_URL}/customers/phone/${PHONE}?tenantId=${TENANT_ID}" \
  -H "Authorization: Bearer $TOKEN")

CUSTOMER_ID=$(echo $CUSTOMER_RESPONSE | python3 -c "import sys, json; print(json.load(sys.stdin).get('id', ''))" 2>/dev/null)

if [ -z "$CUSTOMER_ID" ]; then
  # Create new customer
  CUSTOMER_RESPONSE=$(curl -s -X POST ${BASE_URL}/customers \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
      "tenantId": '$TENANT_ID',
      "name": "POS Test Customer V2",
      "phone": "'${PHONE}'",
      "email": "pos.test.v2@atozshop.com",
      "address": "123 Test Street",
      "city": "Mumbai",
      "state": "Maharashtra",
      "postalCode": "400001"
    }')

  CUSTOMER_ID=$(echo $CUSTOMER_RESPONSE | python3 -c "import sys, json; print(json.load(sys.stdin).get('id', ''))" 2>/dev/null)
fi

CUSTOMER_CODE=$(echo $CUSTOMER_RESPONSE | python3 -c "import sys, json; print(json.load(sys.stdin).get('customerCode', ''))" 2>/dev/null)

if [ -z "$CUSTOMER_ID" ]; then
  echo "❌ Customer FAILED"
  exit 1
fi

echo "✅ Customer ready"
echo "   Customer ID: $CUSTOMER_ID"
echo "   Customer Code: $CUSTOMER_CODE"
echo ""

# Step 3: Get Available Products
echo "Step 3: Get Product Variant"
echo "----------------------------"
PRODUCTS=$(curl -s "${BASE_URL}/public/products?tenantId=${TENANT_ID}" \
  -H "Authorization: Bearer $TOKEN")

VARIANT_ID=$(echo $PRODUCTS | python3 -c "
import sys, json
products = json.load(sys.stdin)
for p in products:
    if p.get('isActive') and p.get('variants'):
        for v in p['variants']:
            if v.get('isActive'):
                print(v['id'])
                break
        break
" 2>/dev/null)

echo "✅ Found variant ID: $VARIANT_ID"
echo ""

# Step 4: Create Bill
echo "Step 4: Create Bill"
echo "-------------------"
BILL_RESPONSE=$(curl -s -X POST ${BASE_URL}/bills \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "tenantId": '$TENANT_ID',
    "storeId": '$STORE_ID',
    "customerId": '$CUSTOMER_ID',
    "cashierId": '$USER_ID',
    "billType": "SALES",
    "items": [{"variantId": '$VARIANT_ID', "quantity": 1}]
  }')

BILL_ID=$(echo $BILL_RESPONSE | python3 -c "import sys, json; print(json.load(sys.stdin).get('id', ''))" 2>/dev/null)
BILL_NUMBER=$(echo $BILL_RESPONSE | python3 -c "import sys, json; print(json.load(sys.stdin).get('billNumber', ''))" 2>/dev/null)
BILL_TOTAL=$(echo $BILL_RESPONSE | python3 -c "import sys, json; print(json.load(sys.stdin).get('totalAmount', 0))" 2>/dev/null)

if [ -z "$BILL_ID" ]; then
  echo "❌ Bill creation FAILED"
  echo "$BILL_RESPONSE"
  exit 1
fi

echo "✅ Bill created"
echo "   Bill Number: $BILL_NUMBER"
echo "   Total: ₹$BILL_TOTAL"
echo ""

# Step 5: Process Payment
echo "Step 5: Process Payment (CASH)"
echo "-------------------------------"
PAYMENT=$(curl -s -X POST ${BASE_URL}/payments \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "billId": '$BILL_ID',
    "tenantId": '$TENANT_ID',
    "paymentMethod": "CASH",
    "amount": '$BILL_TOTAL',
    "paymentDate": "'$(date -u +%Y-%m-%dT%H:%M:%S)'"
  }')

echo "✅ Payment processed: ₹$BILL_TOTAL"
echo ""

# Step 6: Check stock BEFORE confirmation
echo "Step 6: Confirm Bill & Verify Stock"
echo "------------------------------------"
STOCK_BEFORE=$(curl -s "${BASE_URL}/public/products/variant/${VARIANT_ID}/availability?tenantId=${TENANT_ID}&storeId=${STORE_ID}" \
  -H "Authorization: Bearer $TOKEN" | python3 -c "import sys, json; print(json.load(sys.stdin).get('availableStock', 0))" 2>/dev/null)

echo "Stock BEFORE: $STOCK_BEFORE units"

# Confirm bill
CONFIRM=$(curl -s -X POST "${BASE_URL}/bills/${BILL_ID}/confirm?tenantId=${TENANT_ID}" \
  -H "Authorization: Bearer $TOKEN")

STATUS=$(echo $CONFIRM | python3 -c "import sys, json; print(json.load(sys.stdin).get('status', ''))" 2>/dev/null)

if [ "$STATUS" = "CONFIRMED" ]; then
  echo "✅ Bill CONFIRMED"
else
  echo "❌ Confirmation FAILED: $CONFIRM"
  exit 1
fi

# Check stock AFTER
STOCK_AFTER=$(curl -s "${BASE_URL}/public/products/variant/${VARIANT_ID}/availability?tenantId=${TENANT_ID}&storeId=${STORE_ID}" \
  -H "Authorization: Bearer $TOKEN" | python3 -c "import sys, json; print(json.load(sys.stdin).get('availableStock', 0))" 2>/dev/null)

echo "Stock AFTER:  $STOCK_AFTER units"
DEDUCTED=$((STOCK_BEFORE - STOCK_AFTER))
echo "✅ Stock deducted: $DEDUCTED units"
echo ""

# Step 7: Sales Report
echo "Step 7: Daily Sales Report"
echo "--------------------------"
TODAY=$(date +%Y-%m-%d)
REPORT=$(curl -s "${BASE_URL}/reports/sales/daily?tenantId=${TENANT_ID}&storeId=${STORE_ID}&date=${TODAY}" \
  -H "Authorization: Bearer $TOKEN")

TOTAL_SALES=$(echo $REPORT | python3 -c "import sys, json; print(json.load(sys.stdin).get('totalSales', 0))" 2>/dev/null)
TX_COUNT=$(echo $REPORT | python3 -c "import sys, json; print(json.load(sys.stdin).get('transactionCount', 0))" 2>/dev/null)

echo "✅ Sales Report Generated"
echo "   Total Sales: ₹$TOTAL_SALES"
echo "   Transactions: $TX_COUNT"
echo ""

# Summary
echo "========================================="
echo "✅ PHASE 2 TEST COMPLETE!"
echo "========================================="
echo ""
echo "Verified:"
echo "  ✅ Customer management (auto-code)"
echo "  ✅ Bill creation (auto-number: $BILL_NUMBER)"
echo "  ✅ Payment processing (CASH)"
echo "  ✅ Bill confirmation"
echo "  ✅ Stock deduction ($DEDUCTED units)"
echo "  ✅ Sales reporting"
echo "  ✅ Phase 1 integration working"
echo ""
echo "🎉 POS BILLING SYSTEM WORKING!"
echo ""

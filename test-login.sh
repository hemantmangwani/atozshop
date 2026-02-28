#!/bin/bash

# Test Login
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d @- <<'EOF'
{
  "email": "test@atozshop.com",
  "password": "Test1234!"
}
EOF

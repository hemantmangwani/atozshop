# API Documentation - A to Z Shop Management

## 🔐 Authentication

All API endpoints (except authentication endpoints) require a valid JWT token.

### Headers
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

---

## 📍 Base URL

**Local Development**: `http://localhost:8080`
**Production**: `https://api.atozshop.com`

---

## 🔑 Authentication Endpoints

### 1. User Registration

**POST** `/api/v1/auth/register`

Register a new user account.

**Request Body:**
```json
{
  "tenantId": 1,
  "email": "john.doe@example.com",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1234567890",
  "username": "johndoe",
  "storeId": 1
}
```

**Response (200 OK):**
```json
{
  "message": "User registered successfully"
}
```

**Validation Rules:**
- `tenantId`: Required
- `email`: Required, valid email format
- `password`: Required, minimum 8 characters
- `firstName`: Required
- `lastName`: Optional
- `phone`: Optional
- `username`: Optional
- `storeId`: Optional

**Error Responses:**
- `400 Bad Request`: Email or phone already exists
- `400 Bad Request`: Validation errors

---

### 2. User Login

**POST** `/api/v1/auth/login`

Authenticate user and receive JWT token.

**Request Body:**
```json
{
  "email": "john.doe@example.com",
  "password": "SecurePass123!"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "id": 1,
  "email": "john.doe@example.com",
  "username": "johndoe",
  "fullName": "John Doe",
  "tenantId": 1,
  "roles": ["USER", "ADMIN"]
}
```

**Error Responses:**
- `401 Unauthorized`: Invalid credentials
- `400 Bad Request`: Validation errors

---

### 3. Health Check

**GET** `/api/v1/auth/health`

Check if authentication service is running.

**Response (200 OK):**
```json
{
  "message": "Auth service is running"
}
```

---

## 🔒 Using JWT Token

After successful login, include the JWT token in all subsequent requests:

```http
GET /api/v1/users
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

### Token Expiration

Tokens expire after **24 hours** (86400000 ms).

When a token expires, the API will return:
```json
{
  "error": "Unauthorized",
  "message": "Expired JWT token"
}
```

You must login again to get a new token.

---

## 📊 Error Response Format

All errors follow this standard format:

```json
{
  "timestamp": "2026-02-28T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Email is already taken",
  "path": "/api/v1/auth/register"
}
```

**Validation Errors:**
```json
{
  "timestamp": "2026-02-28T10:30:00",
  "status": 400,
  "error": "Validation Failed",
  "errors": {
    "email": "Email should be valid",
    "password": "Password must be at least 8 characters"
  },
  "path": "/api/v1/auth/register"
}
```

---

## 🔐 Security Features

### Password Hashing
- All passwords are hashed using **BCrypt**
- Passwords are never stored in plain text
- Minimum password length: 8 characters

### JWT Security
- Tokens are signed using **HMAC-SHA256**
- Tokens include user ID, email, tenant ID
- Tokens expire after 24 hours

### Role-Based Access Control (RBAC)
- Users can have multiple roles
- Roles: ADMIN, MANAGER, CASHIER, STOCK_KEEPER, DELIVERY_AGENT, USER
- Endpoints can require specific roles (coming in next phases)

---

## 🧪 Testing with Swagger UI

Access interactive API documentation at:
**http://localhost:8080/swagger-ui.html**

### How to Test:

1. **Register a User**:
   - Open Swagger UI
   - Navigate to "Authentication" section
   - Try `/api/v1/auth/register`
   - Click "Try it out"
   - Fill in the request body
   - Click "Execute"

2. **Login**:
   - Try `/api/v1/auth/login`
   - Use the registered email and password
   - Copy the JWT token from the response

3. **Authorize**:
   - Click the "Authorize" button (🔒) at the top
   - Enter: `Bearer <your_token>`
   - Click "Authorize"

4. **Test Protected Endpoints**:
   - All future endpoints will now include your token automatically

---

## 🧪 Testing with cURL

### Register User
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "tenantId": 1,
    "email": "test@example.com",
    "password": "Test1234!",
    "firstName": "Test",
    "lastName": "User"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test1234!"
  }'
```

### Use Token (example for future endpoints)
```bash
TOKEN="eyJhbGciOiJIUzUxMiJ9..."

curl -X GET http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer $TOKEN"
```

---

## 🧪 Testing with Postman

### Setup Environment

1. Create a new environment
2. Add variables:
   - `base_url`: `http://localhost:8080`
   - `token`: (leave empty initially)

### Test Collection

**1. Register**
```
POST {{base_url}}/api/v1/auth/register
Body (JSON):
{
  "tenantId": 1,
  "email": "postman@test.com",
  "password": "Postman123!",
  "firstName": "Postman",
  "lastName": "Test"
}
```

**2. Login**
```
POST {{base_url}}/api/v1/auth/login
Body (JSON):
{
  "email": "postman@test.com",
  "password": "Postman123!"
}

# In Tests tab, add:
pm.environment.set("token", pm.response.json().token);
```

**3. Use Token**
```
GET {{base_url}}/api/v1/users
Headers:
Authorization: Bearer {{token}}
```

---

## 📋 HTTP Status Codes

| Code | Meaning | When |
|------|---------|------|
| 200 | OK | Successful request |
| 201 | Created | Resource created successfully |
| 400 | Bad Request | Validation errors, invalid input |
| 401 | Unauthorized | Invalid or missing JWT token |
| 403 | Forbidden | User lacks required permissions |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Resource already exists |
| 500 | Internal Server Error | Server error |

---

## 🔄 CORS Configuration

**Allowed Origins** (Development):
- http://localhost:3000 (React)
- http://localhost:5173 (Vite)
- http://localhost:4200 (Angular)

**Allowed Methods**:
- GET, POST, PUT, DELETE, PATCH, OPTIONS

**Allowed Headers**: All

---

## 🚀 Coming Soon (Future Phases)

### Phase 1: Inventory Management
- GET `/api/v1/products` - List all products
- POST `/api/v1/products` - Create product
- GET `/api/v1/categories` - List categories
- POST `/api/v1/stock/incoming` - Add incoming stock

### Phase 2: POS Billing
- POST `/api/v1/bills` - Create bill
- GET `/api/v1/bills` - List bills
- GET `/api/v1/reports/daily` - Daily sales report

### Phase 3: Orders
- POST `/api/v1/orders` - Create order
- GET `/api/v1/orders/{id}` - Get order details
- PUT `/api/v1/orders/{id}/status` - Update order status

### Phase 4: Analytics
- GET `/api/v1/dashboard/kpis` - Dashboard KPIs
- GET `/api/v1/reports/top-products` - Top selling products
- GET `/api/v1/reports/sales-trend` - Sales trend graph

---

## 🛠️ Developer Notes

### Adding New Endpoints

1. **Create DTO classes** (request/response)
2. **Create Service** (business logic)
3. **Create Controller** (REST endpoints)
4. **Add to SecurityConfig** if public
5. **Document in this file**

### Security Best Practices

- Never log passwords
- Always use DTOs (never expose entities directly)
- Validate all inputs
- Use proper HTTP status codes
- Handle exceptions gracefully

---

*Last Updated: 2026-02-28*
*API Version: 0.1.0*
*Phase: 0 (Authentication Complete)*

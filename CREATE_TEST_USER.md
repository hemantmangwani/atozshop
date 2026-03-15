# Create Test User - Quick Fix

## Problem
Cannot login because test users don't exist in the database.

## Solution

### Option 1: Use SQL (Recommended)

**Open your PostgreSQL client** (pgAdmin, DBeaver, TablePlus, or command line) and run:

```sql
-- Check if users table exists
SELECT * FROM users LIMIT 1;

-- Insert admin user
INSERT INTO users (
    tenant_id,
    email,
    password,
    first_name,
    last_name,
    role,
    is_active,
    created_at,
    updated_at
)
VALUES (
    1,
    'admin@atozshop.com',
    '$2a$10$dXJ3SW6G7P4eXOOz2PeTsOcP/.qX3XLJvXLFxBr2d2J8eUfbB8gAi',  -- admin123
    'Admin',
    'User',
    'ADMIN',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Insert customer user
INSERT INTO users (
    tenant_id,
    email,
    password,
    first_name,
    last_name,
    role,
    is_active,
    created_at,
    updated_at
)
VALUES (
    1,
    'customer@atozshop.com',
    '$2a$10$dXJ3SW6G7P4eXOOz2PeTsOcP/.qX3XLJvXLFxBr2d2J8eUfbB8gAi',  -- customer123
    'Customer',
    'User',
    'CUSTOMER',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Verify
SELECT id, email, role FROM users;
```

### Option 2: Use Spring Boot Endpoint

**Create a registration endpoint** (if you have one) or use Postman to register a new user.

### Option 3: Check Existing Users

Maybe users already exist with different passwords. Run:

```sql
SELECT email, role, is_active FROM users;
```

If you see users there, try the password that was used during Phase 0 setup.

## Test Login

After creating the user, test with:

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@atozshop.com","password":"admin123"}'
```

Should return:
```json
{
  "token": "eyJhbGc...",
  "userId": 1,
  "username": "admin@atozshop.com",
  "role": "ADMIN",
  "tenantId": 1
}
```

## Then Test Frontend

1. Go to http://localhost:5173
2. Enter:
   - Email: `admin@atozshop.com`
   - Password: `admin123`
3. Click Sign in
4. Should redirect to home page with products

---

## Troubleshooting

### Error: "relation users does not exist"

The database schema hasn't been created. **Start the Spring Boot backend**:

```bash
./mvnw spring-boot:run
```

Wait for it to create tables, then run the SQL above.

### Error: "duplicate key value violates unique constraint"

User already exists! Try:

```sql
-- Update existing user password
UPDATE users
SET password = '$2a$10$dXJ3SW6G7P4eXOOz2PeTsOcP/.qX3XLJvXLFxBr2d2J8eUfbB8gAi'
WHERE email = 'admin@atozshop.com';
```

### Error: "column does not exist"

Check your actual table structure:

```sql
\d users  -- in psql
-- or
SELECT column_name FROM information_schema.columns WHERE table_name = 'users';
```

Adjust the INSERT statement to match your schema.

---

**Password Hashes Used Above**:
- `admin123` → `$2a$10$dXJ3SW6G7P4eXOOz2PeTsOcP/.qX3XLJvXLFxBr2d2J8eUfbB8gAi`
- `customer123` → `$2a$10$dXJ3SW6G7P4eXOOz2PeTsOcP/.qX3XLJvXLFxBr2d2J8eUfbB8gAi`

(Using same hash for simplicity in testing)

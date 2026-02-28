# Testing Guide - A to Z Shop Management

## ✅ Prerequisites Check

### 1. PostgreSQL - Ready ✓
```bash
# Container: atozshop-db is running
# Database: atozshop
# User: atozshop
# Password: atozshop123
# Port: 5432
```

### 2. Java - Ready ✓
```bash
# Version: OpenJDK 21.0.8
```

### 3. Maven - Need to Install

---

## 📦 Option 1: Install Maven (Recommended)

### macOS (using Homebrew):
```bash
brew install maven
```

### Verify Installation:
```bash
mvn --version
```

---

## 📦 Option 2: Use Maven Wrapper (No installation needed)

Since Maven is not installed, we can use the Maven wrapper that comes with the project.

### Download Maven Wrapper:
```bash
cd /Users/hemant.mangwani/gitproject/20jan/atozshop

# Download wrapper
curl -o mvnw https://raw.githubusercontent.com/apache/maven/master/maven-wrapper/mvnw
curl -o mvnw.cmd https://raw.githubusercontent.com/apache/maven/master/maven-wrapper/mvnw.cmd

# Make executable
chmod +x mvnw

# Use wrapper instead of mvn
./mvnw clean install
./mvnw spring-boot:run
```

---

## 🚀 Running the Application

### Step 1: Build the Project

**With Maven:**
```bash
mvn clean install
```

**With Wrapper:**
```bash
./mvnw clean install
```

Expected output:
```
[INFO] BUILD SUCCESS
[INFO] Total time:  XX s
[INFO] Finished at: YYYY-MM-DD
```

### Step 2: Run the Application

**With Maven:**
```bash
mvn spring-boot:run
```

**With Wrapper:**
```bash
./mvnw spring-boot:run
```

Expected output:
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.2)

╔══════════════════════════════════════════════════════════╗
║                                                          ║
║     A to Z Shop Management Application Started! 🚀      ║
║                                                          ║
║  API Documentation: http://localhost:8080/swagger-ui    ║
║                                                          ║
╚══════════════════════════════════════════════════════════╝

Started AtoZShopApplication in X.XXX seconds
```

### Step 3: Verify Database Tables Created

```bash
docker exec -it atozshop-db psql -U atozshop -d atozshop -c "\dt"
```

Expected tables:
- tenants
- stores
- roles
- users
- user_roles

---

## 🧪 Testing the API

### Option 1: Using Swagger UI (Easiest)

1. Open browser: **http://localhost:8080/swagger-ui.html**

2. **Register a User**:
   - Expand "Authentication" section
   - Click on `POST /api/v1/auth/register`
   - Click "Try it out"
   - Fill in the request body:
   ```json
   {
     "tenantId": 1,
     "email": "test@atozshop.com",
     "password": "Test1234!",
     "firstName": "Test",
     "lastName": "User"
   }
   ```
   - Click "Execute"
   - Should return: `{ "message": "User registered successfully" }`

3. **Create Tenant First** (if registration fails):
   ```bash
   docker exec -it atozshop-db psql -U atozshop -d atozshop -c \
     "INSERT INTO tenants (name, slug, timezone, is_active, created_at, updated_at)
      VALUES ('Test Shop', 'test-shop', 'UTC', true, NOW(), NOW());"
   ```

4. **Login**:
   - Click on `POST /api/v1/auth/login`
   - Click "Try it out"
   - Fill in:
   ```json
   {
     "email": "test@atozshop.com",
     "password": "Test1234!"
   }
   ```
   - Click "Execute"
   - Copy the JWT token from response

5. **Authorize**:
   - Click the green "Authorize" button (🔒) at top
   - Enter: `Bearer <your-token-here>`
   - Click "Authorize"
   - Now all endpoints will use this token automatically

---

### Option 2: Using cURL

#### 1. Create Tenant
```bash
docker exec -it atozshop-db psql -U atozshop -d atozshop -c \
  "INSERT INTO tenants (name, slug, timezone, is_active, created_at, updated_at)
   VALUES ('My Shop', 'my-shop', 'UTC', true, NOW(), NOW());"
```

#### 2. Register User
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "tenantId": 1,
    "email": "john@example.com",
    "password": "SecurePass123!",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

Expected response:
```json
{
  "message": "User registered successfully"
}
```

#### 3. Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "SecurePass123!"
  }'
```

Expected response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "id": 1,
  "email": "john@example.com",
  "username": null,
  "fullName": "John Doe",
  "tenantId": 1,
  "roles": ["USER"]
}
```

#### 4. Use Token (example)
```bash
TOKEN="eyJhbGciOiJIUzI1NiJ9..."

curl -X GET http://localhost:8080/api/v1/auth/health \
  -H "Authorization: Bearer $TOKEN"
```

---

### Option 3: Using Postman

1. **Import Collection**:
   - Create new collection: "AtoZShop API"

2. **Create Environment**:
   - base_url: `http://localhost:8080`
   - token: (leave empty)

3. **Add Requests**:

**Register:**
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

**Login:**
```
POST {{base_url}}/api/v1/auth/login
Body (JSON):
{
  "email": "postman@test.com",
  "password": "Postman123!"
}

# In "Tests" tab, add:
pm.environment.set("token", pm.response.json().token);
```

---

## 🔍 Verify Database

### Check Created Tables
```bash
docker exec -it atozshop-db psql -U atozshop -d atozshop -c "\dt"
```

### Check Users
```bash
docker exec -it atozshop-db psql -U atozshop -d atozshop -c \
  "SELECT id, email, first_name, last_name, is_active, created_at FROM users;"
```

### Check Roles
```bash
docker exec -it atozshop-db psql -U atozshop -d atozshop -c \
  "SELECT * FROM roles;"
```

### Check User Roles
```bash
docker exec -it atozshop-db psql -U atozshop -d atozshop -c \
  "SELECT u.email, r.name FROM users u
   JOIN user_roles ur ON u.id = ur.user_id
   JOIN roles r ON ur.role_id = r.id;"
```

---

## 🐛 Troubleshooting

### Issue: Port 8080 already in use
```bash
# Find process
lsof -ti:8080

# Kill process
lsof -ti:8080 | xargs kill

# Or change port in application.properties
server.port=8081
```

### Issue: Cannot connect to database
```bash
# Check container is running
docker ps | grep atozshop-db

# Check logs
docker logs atozshop-db

# Restart container
docker restart atozshop-db
```

### Issue: "Table 'tenants' doesn't exist"
**Solution**: Hibernate will create tables automatically on first run. Just restart the application.

### Issue: "Email already exists"
**Solution**: Email is already registered. Either:
1. Use a different email
2. Clear the database:
```bash
docker exec -it atozshop-db psql -U atozshop -d atozshop -c "TRUNCATE users, user_roles, roles, tenants CASCADE;"
```

### Issue: Build fails
```bash
# Clean and rebuild
mvn clean install -U

# Skip tests if needed
mvn clean install -DskipTests
```

---

## 📊 Expected Results

### After Starting Application:

1. **Console Output**:
   - Spring Boot banner
   - "Started AtoZShopApplication in X seconds"
   - No error messages

2. **Database Tables**:
   ```
   tenants
   stores
   roles
   users
   user_roles
   ```

3. **API Endpoints Available**:
   - http://localhost:8080/api/v1/auth/register
   - http://localhost:8080/api/v1/auth/login
   - http://localhost:8080/api/v1/auth/health
   - http://localhost:8080/swagger-ui.html
   - http://localhost:8080/api-docs

---

## ✅ Success Checklist

- [ ] PostgreSQL container running
- [ ] Application starts without errors
- [ ] Database tables created automatically
- [ ] Swagger UI accessible
- [ ] Can register a user
- [ ] Can login and get JWT token
- [ ] Token works for authenticated endpoints

---

## 🎯 Next Steps After Testing

Once everything works:

1. **Commit test results**:
   ```bash
   # Document any issues or successes
   ```

2. **Start Phase 1** - Inventory Management:
   - Product entities
   - Category management
   - Stock ledger
   - Incoming stock tab

3. **Add more users with different roles**:
   - Admin
   - Manager
   - Cashier

---

*Last Updated: 2026-02-28*
*Phase: 0 Testing*

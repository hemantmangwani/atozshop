# Quick Start Guide - A to Z Shop Management

## 🚀 Application is Running!

**Current Status:** ✅ RUNNING on http://localhost:8080

---

## 🔥 Quick Test (Right Now!)

### Option 1: Swagger UI (Easiest)
Open in browser: **http://localhost:8080/swagger-ui.html**

### Option 2: cURL Commands

#### Test User Already Created:
- Email: `test@atozshop.com`
- Password: `Test1234!`

#### Login and Get Token:
```bash
./test-login.sh
```

#### Test Authenticated Endpoint:
```bash
./test-health.sh
```

---

## 📱 Try These in Swagger UI

1. **Open Swagger:** http://localhost:8080/swagger-ui.html

2. **Login:**
   - Expand: `POST /api/v1/auth/login`
   - Click: "Try it out"
   - Use credentials:
     ```json
     {
       "email": "test@atozshop.com",
       "password": "Test1234!"
     }
     ```
   - Click "Execute"
   - Copy the `token` from response

3. **Authorize:**
   - Click the green 🔒 "Authorize" button at top
   - Enter: `Bearer <paste-your-token-here>`
   - Click "Authorize"

4. **Test Protected Endpoint:**
   - Expand: `GET /api/v1/auth/health`
   - Click: "Try it out"
   - Click: "Execute"
   - Should return: `{"message": "Auth service is running"}`

---

## 📊 What's Available

### Endpoints
- `POST /api/v1/auth/register` - Create new user
- `POST /api/v1/auth/login` - Login and get JWT token
- `GET /api/v1/auth/health` - Test authentication (requires token)

### Database
- Container: `atozshop-db`
- Database: `atozshop`
- Tables: `tenants`, `stores`, `users`, `roles`, `user_roles`

### Test User
- ID: 1
- Email: test@atozshop.com
- Name: Test User
- Tenant ID: 1
- Role: USER
- Active: Yes

---

## 🛠️ Control the Application

### Stop Application:
```bash
# Find the process
lsof -ti:8080

# Kill it
lsof -ti:8080 | xargs kill
```

### Start Application (if stopped):
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
mvn spring-boot:run
```

### Restart Application:
```bash
# Stop
lsof -ti:8080 | xargs kill

# Wait 2 seconds
sleep 2

# Start
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
mvn spring-boot:run
```

---

## 🗄️ Database Access

### View Tables:
```bash
docker exec atozshop-db psql -U atozshop -d atozshop -c "\dt"
```

### View Users:
```bash
docker exec atozshop-db psql -U atozshop -d atozshop -c \
  "SELECT id, email, first_name, last_name, is_active FROM users;"
```

### View User Roles:
```bash
docker exec atozshop-db psql -U atozshop -d atozshop -c \
  "SELECT u.email, r.name FROM users u
   JOIN user_roles ur ON u.id = ur.user_id
   JOIN roles r ON ur.role_id = r.id;"
```

### Clear Database (if needed):
```bash
docker exec atozshop-db psql -U atozshop -d atozshop -c \
  "TRUNCATE users, user_roles, roles, tenants, stores CASCADE;"
```

---

## 📝 Register a New User

### Using Swagger UI:
1. Go to: `POST /api/v1/auth/register`
2. Click "Try it out"
3. Use this JSON:
```json
{
  "tenantId": 1,
  "email": "newuser@example.com",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe"
}
```
4. Click "Execute"

### Using cURL:
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "tenantId": 1,
    "email": "newuser@example.com",
    "password": "SecurePass123!",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

---

## 🔍 Logs and Debugging

### View Application Logs:
The application is running in the background. To see logs:
```bash
tail -f /private/tmp/claude-503/-Users-hemant-mangwani-gitproject-20jan-atozshop/tasks/beb2c39.output
```

### Common Issues:

**Port 8080 already in use:**
```bash
lsof -ti:8080 | xargs kill
```

**Database connection refused:**
```bash
docker ps | grep atozshop-db
docker restart atozshop-db
```

**Java version issues:**
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
java -version  # Should show 21.0.8
```

---

## 📚 Documentation Files

- **README.md** - Project overview and features
- **API_DOCUMENTATION.md** - Complete API reference
- **TESTING_GUIDE.md** - Detailed testing instructions
- **TEST_RESULTS.md** - Test results report
- **PHASE0_PROGRESS.md** - Phase 0 progress tracker
- **QUICK_START.md** - This file

---

## 🎯 Phase 0 Status

✅ **COMPLETE & TESTED**

All features working:
- User authentication (JWT)
- User registration
- Role-based access control
- Multi-tenant architecture
- Database with auto-schema
- API documentation (Swagger)
- Error handling
- Security features

---

## 🚀 Ready for Phase 1!

Phase 1 will add:
- Product management
- Category management
- Stock Ledger (inventory tracking)
- Incoming stock tab
- Low stock alerts

---

*Quick Start Guide - Last Updated: 2026-02-28*

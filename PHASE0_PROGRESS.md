# Phase 0 Progress - Foundation Setup

## ✅ Completed Tasks

### 1. Spring Boot Project Structure
- [x] Created `pom.xml` with all required dependencies
- [x] Set up Maven project structure
- [x] Created main application class: `AtoZShopApplication.java`
- [x] Configured `application.properties`
- [x] Created complete package structure

### 2. Project Dependencies Added
- [x] Spring Boot Web
- [x] Spring Boot Data JPA
- [x] Spring Boot Security
- [x] Spring Boot Validation
- [x] PostgreSQL Driver
- [x] JWT (io.jsonwebtoken)
- [x] Lombok
- [x] Spring Boot DevTools
- [x] Springdoc OpenAPI (Swagger)
- [x] ModelMapper
- [x] Apache Commons Lang
- [x] Spring Boot Test
- [x] Spring Security Test

### 3. Package Structure Created

```
src/main/java/com/atozshop/
├── AtoZShopApplication.java    # Main application class
├── config/                      # Configuration classes
├── entity/                      # JPA entities
│   ├── BaseEntity.java         # Base entity with timestamps
│   ├── Tenant.java             # Multi-tenancy support
│   ├── Store.java              # Multi-branch support
│   ├── User.java               # User entity
│   └── Role.java               # Role-based access control
├── repository/                  # JPA repositories
│   ├── TenantRepository.java
│   ├── UserRepository.java
│   ├── RoleRepository.java
│   └── StoreRepository.java
├── dto/                         # Data Transfer Objects
│   ├── request/                # Request DTOs
│   └── response/               # Response DTOs
├── service/                     # Business logic layer
├── controller/                  # REST controllers
├── security/                    # Security configuration
├── exception/                   # Custom exceptions
├── util/                        # Utility classes
└── constant/                    # Constants
```

### 4. Core Entities Implemented

#### BaseEntity
- Provides `createdAt` and `updatedAt` timestamps
- Uses JPA auditing for automatic timestamp management

#### Tenant
- Supports multi-tenancy (multiple shops)
- Fields: id, name, slug, timezone, isActive

#### Store
- Supports multi-branch operations
- Fields: id, tenantId, name, code, address, city, state, postalCode, country, phone, email, gstNumber, logoUrl, isActive

#### User
- System users (admin, staff, customers)
- Fields: id, tenantId, storeId, username, email, phone, passwordHash, firstName, lastName, isActive, emailVerified, phoneVerified, lastLoginAt
- Many-to-Many relationship with Role

#### Role
- Role-based access control
- Fields: id, tenantId, name, description, isSystem
- System roles: ADMIN, MANAGER, CASHIER, STOCK_KEEPER, DELIVERY_AGENT

### 5. Repositories Implemented
- [x] TenantRepository - Find by slug, check existence
- [x] UserRepository - Find by email/phone/username with tenant, check existence
- [x] RoleRepository - Find by name with tenant
- [x] StoreRepository - Find by code, list active stores

---

## 🔄 In Progress

### Next Tasks

1. **Security Configuration**
   - [ ] JWT Token Provider
   - [ ] UserDetailsService implementation
   - [ ] Security Configuration class
   - [ ] CORS Configuration

2. **DTOs (Data Transfer Objects)**
   - [ ] LoginRequest/LoginResponse
   - [ ] RegisterRequest
   - [ ] UserResponse
   - [ ] JwtResponse

3. **Services**
   - [ ] AuthService (login, register, JWT generation)
   - [ ] UserService (CRUD operations)
   - [ ] RoleService

4. **Controllers**
   - [ ] AuthController (/api/v1/auth/login, /register)
   - [ ] UserController (/api/v1/users)

5. **Exception Handling**
   - [ ] Global Exception Handler
   - [ ] Custom Exceptions (ResourceNotFoundException, etc.)

---

## 📋 How to Run (After Setup Complete)

### Prerequisites
1. Java 17+
2. PostgreSQL 15+
3. Maven 3.6+

### Database Setup

```bash
# Option 1: Using Docker (Recommended)
docker run --name atozshop-db \
  -e POSTGRES_DB=atozshop \
  -e POSTGRES_USER=atozshop \
  -e POSTGRES_PASSWORD=atozshop123 \
  -p 5432:5432 \
  -d postgres:15

# Option 2: Using local PostgreSQL
createdb atozshop
psql -d atozshop -c "CREATE USER atozshop WITH PASSWORD 'atozshop123';"
psql -d atozshop -c "GRANT ALL PRIVILEGES ON DATABASE atozshop TO atozshop;"
```

### Run Application

```bash
# Clean and install dependencies
mvn clean install

# Run the application
mvn spring-boot:run
```

### Access Points (Once Running)

- **API Base URL**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs (JSON)**: http://localhost:8080/api-docs

---

## 🧪 Testing Database Connection

Once the app starts, you should see tables automatically created by Hibernate:
- tenants
- stores
- users
- roles
- user_roles

To verify:
```sql
# Connect to PostgreSQL
psql -U atozshop -d atozshop

# List tables
\dt

# Expected output:
# tenants, stores, users, roles, user_roles
```

---

## 📊 Database Schema (Current Phase)

```sql
-- Tenants Table
CREATE TABLE tenants (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    timezone VARCHAR(50) DEFAULT 'UTC',
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Stores Table
CREATE TABLE stores (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL,
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(255),
    gst_number VARCHAR(50),
    logo_url VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    UNIQUE(tenant_id, code)
);

-- Roles Table
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_system BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    UNIQUE(tenant_id, name)
);

-- Users Table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    store_id BIGINT,
    username VARCHAR(100),
    email VARCHAR(255),
    phone VARCHAR(20),
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT true,
    email_verified BOOLEAN NOT NULL DEFAULT false,
    phone_verified BOOLEAN NOT NULL DEFAULT false,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    UNIQUE(tenant_id, email),
    UNIQUE(tenant_id, phone)
);

-- User Roles (Many-to-Many)
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);
```

---

## 🎯 Next Steps (Immediate)

1. **Set up PostgreSQL database** (see instructions above)
2. **Test Maven build**: `mvn clean install`
3. **Start implementing security layer** (JWT + Spring Security)
4. **Create first API endpoints** (login, register)
5. **Test with Swagger UI**

---

## 📝 Notes

- **Lombok**: Reduces boilerplate code with @Getter, @Setter, @Builder
- **JPA Auditing**: Automatic `createdAt` and `updatedAt` timestamps
- **Multi-Tenancy**: Ready for SaaS deployment (multiple shops in one database)
- **Multi-Branch**: Each tenant can have multiple stores
- **Role-Based Access**: Flexible permission system

---

## 🐛 Troubleshooting

### Issue: "Cannot resolve symbol 'lombok'"
**Solution**:
- IntelliJ: Enable annotation processing (Settings → Build → Compiler → Annotation Processors)
- VS Code: Install Lombok extension

### Issue: "Could not connect to database"
**Solution**:
- Verify PostgreSQL is running: `pg_isready`
- Check credentials in `application.properties`
- Ensure database exists: `psql -l | grep atozshop`

### Issue: "Port 8080 already in use"
**Solution**:
- Change port in `application.properties`: `server.port=8081`
- Or kill process: `lsof -ti:8080 | xargs kill`

---

*Last Updated: 2026-02-28*
*Phase: 0 (Foundation)*
*Status: Core entities implemented, Security layer next*
# A to Z Shop Management Application

A comprehensive retail management system covering POS billing, inventory management, online ordering, delivery tracking, and business analytics.

---

## 🎯 Overview

This application provides end-to-end shop management capabilities:

- 🏪 **Point of Sale (POS)** - Fast billing with barcode scanning
- 📦 **Inventory Management** - Real-time stock tracking with ledger system
- 🌐 **E-commerce Website** - Customer ordering with live availability
- 📊 **Business Analytics** - Dashboard with sales, profit, and top products
- 🚚 **Delivery Management** - Order tracking and agent assignment
- 📈 **Profit Tracking** - Accurate cost-based profit calculation
- 📋 **Purchase Management** - Incoming stock with expected profit summary
- 🔐 **Multi-user Access** - Role-based permissions (Admin, Manager, Cashier, etc.)

---

## 📚 Documentation

| Document | Description |
|----------|-------------|
| [GETTING_STARTED.md](./GETTING_STARTED.md) | **Start here!** Step-by-step setup guide |
| [PROJECT_PLAN.md](./PROJECT_PLAN.md) | Complete project plan, technology stack, architecture |
| [DATABASE_SCHEMA.md](./DATABASE_SCHEMA.md) | Full database design (all tables, relationships) |
| [FEATURE_MATRIX.md](./FEATURE_MATRIX.md) | All 406 features organized by phase |

---

## 🚀 Quick Start

### Prerequisites
- Java 17+
- PostgreSQL 15+
- Node.js 18+ (for frontend)
- Maven or Gradle

### 1. Set Up Database

```bash
# Using Docker
docker run --name atozshop-db \
  -e POSTGRES_DB=atozshop \
  -e POSTGRES_USER=atozshop \
  -e POSTGRES_PASSWORD=atozshop123 \
  -p 5432:5432 \
  -d postgres:15
```

### 2. Clone & Configure

```bash
git clone <your-repo>
cd atozshop

# Configure application.properties
cp src/main/resources/application.properties.example \
   src/main/resources/application.properties

# Edit database credentials
```

### 3. Run Backend

```bash
mvn clean install
mvn spring-boot:run
```

Backend will start at: http://localhost:8080

### 4. Run Frontend (when ready)

```bash
cd atozshop-web
npm install
npm run dev
```

Frontend will start at: http://localhost:5173

---

## 🏗️ Technology Stack

### Backend
- **Framework**: Spring Boot 3.2+
- **Language**: Java 17+
- **Database**: PostgreSQL 15+
- **Security**: Spring Security + JWT
- **ORM**: Spring Data JPA (Hibernate)
- **Caching**: Redis (optional)
- **API Docs**: Swagger/OpenAPI

### Frontend
- **Admin Web**: React + TypeScript + Material-UI
- **Mobile Apps**: Flutter (Android, iOS, Windows)
- **State**: TanStack Query (React Query)
- **Charts**: Recharts or Chart.js

---

## 📋 Development Phases

### ✅ Phase 0: Foundation (Week 1-2)
- [x] Project setup
- [x] Database schema
- [ ] Authentication & authorization
- [ ] User & role management

### 🔄 Phase 1: Inventory + Incoming Stock (Week 2-4)
- [ ] Categories & products
- [ ] Stock ledger system
- [ ] **Incoming stock tab** with profit summary
- [ ] Barcode/QR storage
- [ ] Low stock alerts

### 📝 Phase 2: POS Billing (Week 4-6)
- [ ] Fast billing screen
- [ ] **Barcode scanning**
- [ ] Multi-payment support
- [ ] Invoice generation
- [ ] Returns/refunds
- [ ] **Daily sale & profit calculation**

### 🛒 Phase 3: Website + Orders (Week 6-9)
- [ ] Customer website
- [ ] **Real-time availability display**
- [ ] Shopping cart
- [ ] **Order management** (Accept → Deliver)
- [ ] Stock reservation

### 📊 Phase 4: Dashboard + Reports (Week 9-11)
- [ ] **Top N most selling products**
- [ ] Sales & profit graphs
- [ ] **Overall day sale report**
- [ ] Category-wise analytics

### 📦 Phase 5: Purchases + Suppliers (Week 11-13)
- [ ] Purchase orders
- [ ] Supplier management
- [ ] Payables tracking

### 🚚 Phase 6: Delivery (Week 13-15)
- [ ] Delivery agent management
- [ ] OTP confirmation
- [ ] COD tracking

### 🎯 Phase 7: Advanced (Week 15-18)
- [ ] FIFO/Batch costing
- [ ] Multi-branch support
- [ ] Serial number tracking

### 🔌 Phase 8: Integrations (Week 18-22)
- [ ] Payment gateway
- [ ] WhatsApp/SMS notifications
- [ ] Offline POS mode
- [ ] AI features (demand forecasting)

---

## 🎨 Key Features (Your Requirements)

### ✅ Implemented
- [x] Complete project structure
- [x] Database schema (all tables)
- [x] Feature roadmap (406 features)
- [x] Technology recommendations

### 🔨 To Be Built

#### Logo & Branding
- [ ] Shop logo upload
- [ ] Custom invoice branding
- [ ] Theme customization

#### Inventory Management
- [ ] **Incoming stock tab** with:
  - Total qty received
  - Total purchase value
  - Expected revenue
  - **Expected gross profit** (selling - cost)
- [ ] Real-time stock tracking
- [ ] **Availability display** on user screen
  - "In stock" / "Out of stock"
  - "Only X left" warning

#### Order System
- [ ] **Admin accepting orders**
- [ ] Order status flow: NEW → ACCEPTED → PACKED → DELIVERED
- [ ] Delivery management

#### Analytics & Reports
- [ ] **Top N most selling products** (by qty/revenue/profit)
- [ ] **Making bills** workflow
- [ ] **Calculating overall day sale**:
  - Gross sales
  - Net sales (after discounts)
  - **Profit** (selling price - cost price)
  - Payment method split
  - Tax collected

#### QR/Barcode Features
- [ ] **QR-based item addition**
- [ ] Barcode scanning in POS
- [ ] QR invoice verification
- [ ] Label printing

---

## 📊 Database Overview

### Core Tables
- **Catalog**: categories, products, product_variants, variant_prices
- **Inventory**: stock_ledger (event log), inventory_balances
- **Sales**: bills, bill_items, payments, refunds
- **Orders**: orders, order_items, order_status_history
- **Purchases**: purchase_receipts, purchase_receipt_items
- **Users**: users, roles, permissions
- **Delivery**: delivery_agents, deliveries

### Design Patterns
1. **Ledger Pattern** - All stock movements recorded (append-only)
2. **Snapshot Pattern** - Store price/cost at transaction time
3. **Multi-Tenancy** - Ready for SaaS deployment
4. **Soft Delete** - Use `is_active` flags

See [DATABASE_SCHEMA.md](./DATABASE_SCHEMA.md) for complete schema.

---

## 🔐 User Roles

| Role | Permissions |
|------|-------------|
| **Admin** | Full access to all features |
| **Manager** | Inventory, orders, reports (no user management) |
| **Cashier** | POS billing, returns only |
| **Stock Keeper** | Incoming stock, adjustments |
| **Delivery Agent** | Delivery management only |

---

## 📈 Success Metrics

### MVP (Phase 1-2)
- Bill 50 items in < 2 minutes
- Stock accuracy: 100%
- Daily closing matches cash

### Phase 3-4
- Process orders without overselling
- Dashboard loads < 2 seconds
- Top-N products accurate

### Phase 5-8
- Support 10,000+ products
- Handle 1,000 orders/day
- Offline POS works reliably

---

## 🧪 Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn verify
```

### E2E Tests (frontend)
```bash
npm run test:e2e
```

---

## 📦 Deployment

### Development
```bash
mvn spring-boot:run
```

### Production (Docker)
```bash
docker-compose up -d
```

See deployment guide for detailed instructions.

---

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open Pull Request

---

## 📄 License

[Choose your license]

---

## 🆘 Support

For questions or issues:
1. Check [GETTING_STARTED.md](./GETTING_STARTED.md)
2. Review [FEATURE_MATRIX.md](./FEATURE_MATRIX.md)
3. Consult [DATABASE_SCHEMA.md](./DATABASE_SCHEMA.md)
4. Open an issue on GitHub

---

## 🎯 Project Status

**Current Phase**: Phase 0 - Foundation Setup
**Features Completed**: 0 / 406 (0%)
**Target Launch**: [Your date]

---

## 🗺️ Roadmap

- [ ] **Q1 2026**: Phase 0-2 (MVP - POS + Inventory)
- [ ] **Q2 2026**: Phase 3-4 (Website + Analytics)
- [ ] **Q3 2026**: Phase 5-6 (Purchases + Delivery)
- [ ] **Q4 2026**: Phase 7-8 (Advanced + Integrations)

---

## 📸 Screenshots

_Coming soon..._

---

## 🙏 Acknowledgments

- Spring Boot Team
- PostgreSQL Community
- React Community
- Flutter Team

---

**Built with ❤️ for retail businesses**

*Last Updated: 2026-02-28*

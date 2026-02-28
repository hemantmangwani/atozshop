# Getting Started - A to Z Shop Management

## Quick Start Guide

You now have a complete plan for building a comprehensive shop management application. Here's how to proceed:

---

## 📚 Documentation You Have

1. **PROJECT_PLAN.md** - Complete project overview, technology stack, phases
2. **DATABASE_SCHEMA.md** - Full database design with all tables
3. **FEATURE_MATRIX.md** - All 406 features organized by phase
4. **GETTING_STARTED.md** - This file

---

## 🎯 Recommended Technology Stack

### Backend
```
Java Spring Boot 3.2+
PostgreSQL 15+
Spring Security + JWT
Spring Data JPA
Redis (caching)
RabbitMQ (optional - async tasks)
```

### Frontend - Hybrid Approach (Best Balance)
```
Admin Web: React + TypeScript + Vite + Material-UI
Mobile Apps: Flutter (Android + iOS + Windows)
POS Desktop: Flutter Windows or Electron
```

### Why This Combination?
- You're already strong in **Java + SQL** ✅
- **React** gives the best admin dashboard experience (graphs, tables, complex forms)
- **Flutter** gives you mobile + desktop from one codebase
- Backend APIs work for all clients

---

## 🚀 Step-by-Step Implementation

### Week 1-2: Phase 0 - Foundation

#### 1. Set Up Spring Boot Project

```bash
# Option A: Using Spring Initializr (https://start.spring.io)
# Select:
# - Project: Maven
# - Language: Java 17 or 21
# - Spring Boot: 3.2.x
# - Dependencies: Web, JPA, PostgreSQL, Security, Validation, Lombok

# Option B: Using Spring CLI
spring init --dependencies=web,data-jpa,postgresql,security,validation,lombok \
  --build=maven --java-version=17 --artifactId=atozshop atozshop

cd atozshop
```

#### 2. Set Up PostgreSQL

```bash
# Using Docker (recommended for development)
docker run --name atozshop-db \
  -e POSTGRES_DB=atozshop \
  -e POSTGRES_USER=atozshop \
  -e POSTGRES_PASSWORD=atozshop123 \
  -p 5432:5432 \
  -d postgres:15
```

#### 3. Configure application.properties

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/atozshop
spring.datasource.username=atozshop
spring.datasource.password=atozshop123

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Server
server.port=8080

# JWT
jwt.secret=your-secret-key-change-this-in-production
jwt.expiration=86400000

# File upload
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

#### 4. Project Structure

```
src/main/java/com/atozshop/
├── config/
│   ├── SecurityConfig.java
│   ├── JwtConfig.java
│   └── CorsConfig.java
├── entity/
│   ├── Tenant.java
│   ├── Store.java
│   ├── User.java
│   ├── Role.java
│   ├── Product.java
│   └── ... (all entities from DATABASE_SCHEMA.md)
├── repository/
│   ├── UserRepository.java
│   ├── ProductRepository.java
│   └── ... (one per entity)
├── dto/
│   ├── request/
│   │   ├── LoginRequest.java
│   │   ├── ProductCreateRequest.java
│   │   └── ...
│   └── response/
│       ├── LoginResponse.java
│       ├── ProductResponse.java
│       └── ...
├── service/
│   ├── AuthService.java
│   ├── UserService.java
│   ├── ProductService.java
│   └── ... (business logic)
├── controller/
│   ├── AuthController.java
│   ├── UserController.java
│   ├── ProductController.java
│   └── ... (REST endpoints)
├── security/
│   ├── JwtTokenProvider.java
│   └── UserDetailsServiceImpl.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   └── ...
└── AtozshopApplication.java
```

#### 5. Create First Entity (User)

```java
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    private String firstName;
    private String lastName;

    @Column(nullable = false)
    private Boolean isActive = true;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
}
```

#### 6. Test Your Setup

```bash
mvn spring-boot:run
```

Visit: http://localhost:8080

---

### Week 2-4: Phase 1 - Inventory System

**Focus**: Categories, Products, Stock Ledger, Incoming Stock

#### Key Entities to Build
1. Category
2. Product
3. ProductVariant
4. TaxCode
5. PriceList
6. VariantPrice
7. Warehouse
8. **StockLedger** (Most important!)
9. InventoryBalance
10. PurchaseReceipt (Your "Incoming Stock Tab")

#### Critical Service: StockLedgerService

```java
@Service
public class StockLedgerService {

    // Record any stock movement
    public void recordStockMovement(
        Long variantId,
        Long warehouseId,
        StockTxnType txnType,
        BigDecimal qty,
        BigDecimal unitCost,
        String referenceType,
        Long referenceId
    ) {
        StockLedger entry = new StockLedger();
        entry.setVariantId(variantId);
        entry.setWarehouseId(warehouseId);
        entry.setTxnType(txnType);
        entry.setQty(qty);
        entry.setUnitCost(unitCost);
        entry.setReferenceType(referenceType);
        entry.setReferenceId(referenceId);
        entry.setCreatedAt(LocalDateTime.now());

        stockLedgerRepository.save(entry);

        // Update balance cache
        updateInventoryBalance(variantId, warehouseId);
    }

    // Calculate current stock from ledger
    public BigDecimal getCurrentStock(Long variantId, Long warehouseId) {
        return stockLedgerRepository
            .sumQtyByVariantAndWarehouse(variantId, warehouseId);
    }
}
```

#### Incoming Stock Tab API

```java
@RestController
@RequestMapping("/api/v1/purchases")
public class PurchaseReceiptController {

    @PostMapping("/receipts")
    public ResponseEntity<PurchaseReceiptResponse> createReceipt(
        @RequestBody PurchaseReceiptRequest request
    ) {
        // Create receipt
        PurchaseReceipt receipt = purchaseService.createReceipt(request);

        // Calculate summary (your requirement)
        PurchaseReceiptSummary summary = new PurchaseReceiptSummary();
        summary.setTotalQty(receipt.getItems().stream()
            .map(PurchaseReceiptItem::getQtyReceived)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        summary.setTotalPurchaseValue(receipt.getGrandTotal());
        summary.setExpectedRevenue(receipt.getItems().stream()
            .map(item -> item.getQtyReceived()
                .multiply(item.getSellingPriceSnapshot()))
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        summary.setExpectedGrossProfit(
            summary.getExpectedRevenue()
                .subtract(summary.getTotalPurchaseValue())
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/receipts/{id}/post")
    public ResponseEntity<?> postReceipt(@PathVariable Long id) {
        // Finalize receipt and update stock
        purchaseService.postReceipt(id);
        return ResponseEntity.ok().build();
    }
}
```

---

### Week 4-6: Phase 2 - POS Billing

**Focus**: Billing, Payments, Returns, Daily Reports

#### Key Features
1. Fast billing screen
2. Barcode scanning
3. Auto tax calculation
4. Multi-payment
5. Invoice generation
6. Daily closing report
7. **Profit calculation**

#### BillingService (Core Logic)

```java
@Service
@Transactional
public class BillingService {

    public Bill createBill(BillRequest request) {
        Bill bill = new Bill();
        bill.setBillNo(generateBillNo());
        bill.setCustomerId(request.getCustomerId());

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal taxTotal = BigDecimal.ZERO;
        BigDecimal cogs = BigDecimal.ZERO;

        for (BillItemRequest itemReq : request.getItems()) {
            BillItem item = new BillItem();
            item.setVariantId(itemReq.getVariantId());
            item.setQty(itemReq.getQty());

            // Get current price
            VariantPrice price = priceService.getCurrentPrice(
                itemReq.getVariantId()
            );
            item.setUnitSellingPrice(price.getSellingPrice());

            // CRITICAL: Snapshot cost for accurate profit
            item.setUnitCostSnapshot(price.getCostPrice());

            // Calculate tax
            BigDecimal itemTotal = item.getUnitSellingPrice()
                .multiply(item.getQty());
            BigDecimal tax = itemTotal.multiply(
                price.getTaxPercent().divide(new BigDecimal("100"))
            );
            item.setTaxAmount(tax);
            item.setLineTotal(itemTotal.add(tax));

            bill.addItem(item);

            subtotal = subtotal.add(itemTotal);
            taxTotal = taxTotal.add(tax);

            // Calculate COGS
            BigDecimal itemCost = item.getUnitCostSnapshot()
                .multiply(item.getQty());
            cogs = cogs.add(itemCost);
        }

        bill.setSubtotal(subtotal);
        bill.setTaxTotal(taxTotal);
        bill.setDiscountTotal(request.getDiscountTotal());
        bill.setGrandTotal(
            subtotal.add(taxTotal).subtract(request.getDiscountTotal())
        );
        bill.setCostOfGoodsSold(cogs);
        bill.setGrossProfit(bill.getGrandTotal().subtract(cogs));

        Bill savedBill = billRepository.save(bill);

        // Update stock
        for (BillItem item : savedBill.getItems()) {
            stockLedgerService.recordStockMovement(
                item.getVariantId(),
                request.getWarehouseId(),
                StockTxnType.SALE,
                item.getQty().negate(), // Negative for sale
                item.getUnitCostSnapshot(),
                "bill",
                savedBill.getId()
            );
        }

        return savedBill;
    }
}
```

---

### Week 6-9: Phase 3 - Website + Orders

**Focus**: Customer website, Shopping cart, Order management

#### Technology
- **Frontend**: React + TypeScript + Vite
- **UI Library**: Material-UI or Ant Design
- **State Management**: TanStack Query (React Query)
- **Routing**: React Router

#### Create React App

```bash
npm create vite@latest atozshop-web -- --template react-ts
cd atozshop-web
npm install @mui/material @mui/icons-material @tanstack/react-query axios
npm run dev
```

#### Example: Product List Component

```typescript
import { useQuery } from '@tanstack/react-query';
import { Card, Grid, Typography, Chip } from '@mui/material';

interface Product {
  id: number;
  name: string;
  price: number;
  stockAvailable: number;
  imageUrl: string;
}

export function ProductList() {
  const { data: products } = useQuery({
    queryKey: ['products'],
    queryFn: () => fetch('/api/v1/products').then(r => r.json())
  });

  return (
    <Grid container spacing={2}>
      {products?.map((product: Product) => (
        <Grid item xs={12} sm={6} md={4} key={product.id}>
          <Card>
            <img src={product.imageUrl} alt={product.name} />
            <Typography variant="h6">{product.name}</Typography>
            <Typography>₹{product.price}</Typography>

            {/* Your requirement: Show availability */}
            {product.stockAvailable > 0 ? (
              product.stockAvailable <= 5 ? (
                <Chip
                  label={`Only ${product.stockAvailable} left`}
                  color="warning"
                />
              ) : (
                <Chip label="In Stock" color="success" />
              )
            ) : (
              <Chip label="Out of Stock" color="error" />
            )}
          </Card>
        </Grid>
      ))}
    </Grid>
  );
}
```

---

### Week 9-11: Phase 4 - Dashboard + Reports

**Focus**: Top-N products, graphs, profit reports

#### Dashboard API

```java
@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    @GetMapping("/kpis")
    public DashboardKPIs getKPIs(
        @RequestParam LocalDate startDate,
        @RequestParam LocalDate endDate
    ) {
        DashboardKPIs kpis = new DashboardKPIs();

        // Today's sales
        kpis.setGrossSales(
            billRepository.sumGrandTotalByDateRange(startDate, endDate)
        );

        // Today's profit
        kpis.setGrossProfit(
            billRepository.sumGrossProfitByDateRange(startDate, endDate)
        );

        // Payment split
        Map<String, BigDecimal> paymentSplit =
            paymentRepository.sumByMethodAndDateRange(startDate, endDate);
        kpis.setPaymentSplit(paymentSplit);

        // Top N products (YOUR KEY REQUIREMENT)
        List<TopProductDTO> topProducts =
            billItemRepository.findTopNByQuantitySold(startDate, endDate, 10);
        kpis.setTopProducts(topProducts);

        return kpis;
    }

    @GetMapping("/sales-trend")
    public List<SalesTrendDTO> getSalesTrend(
        @RequestParam LocalDate startDate,
        @RequestParam LocalDate endDate,
        @RequestParam String groupBy // HOUR, DAY, MONTH
    ) {
        return reportService.getSalesTrend(startDate, endDate, groupBy);
    }
}
```

#### Top N Products Query (PostgreSQL)

```java
@Repository
public interface BillItemRepository extends JpaRepository<BillItem, Long> {

    @Query("""
        SELECT new com.atozshop.dto.TopProductDTO(
            bi.variantId,
            v.name,
            SUM(bi.qty) as totalQty,
            SUM(bi.lineTotal) as totalRevenue,
            SUM(bi.lineTotal - (bi.unitCostSnapshot * bi.qty)) as totalProfit
        )
        FROM BillItem bi
        JOIN bi.bill b
        JOIN ProductVariant v ON v.id = bi.variantId
        WHERE b.createdAt BETWEEN :startDate AND :endDate
        AND b.billStatus IN ('PAID', 'PARTIAL')
        GROUP BY bi.variantId, v.name
        ORDER BY totalQty DESC
        LIMIT :limit
    """)
    List<TopProductDTO> findTopNByQuantitySold(
        LocalDate startDate,
        LocalDate endDate,
        int limit
    );
}
```

---

## 🛠️ Development Tools

### Backend Tools
```bash
# IntelliJ IDEA (Recommended)
# VS Code with Java extensions
# PostgreSQL GUI: pgAdmin or DBeaver
# API Testing: Postman or Insomnia
# Database migrations: Flyway or Liquibase
```

### Frontend Tools
```bash
# VS Code with extensions:
# - ESLint
# - Prettier
# - Vite
# - React Developer Tools (Chrome extension)
```

### DevOps Tools
```bash
# Docker Desktop
# Git
# Maven or Gradle
# Postman
```

---

## 📊 Testing Your Implementation

### Phase 1 Tests
- ✅ Create category → product appears in category
- ✅ Add incoming stock → stock ledger entry created
- ✅ Stock balance = sum of ledger entries
- ✅ Expected profit = (selling - cost) × qty

### Phase 2 Tests
- ✅ Create bill → stock decreases
- ✅ Bill total = subtotal + tax - discount
- ✅ Profit = grand total - COGS
- ✅ Daily report totals match bills

### Phase 3 Tests
- ✅ Place order → stock reserved
- ✅ Cancel order → stock released
- ✅ Two customers can't buy last item (no overselling)

### Phase 4 Tests
- ✅ Dashboard KPIs = report values
- ✅ Top-N products match raw data
- ✅ Sales trend graph accurate

---

## 📈 Progress Tracking

Use **FEATURE_MATRIX.md** to track your progress:

1. Copy to Excel/Google Sheets
2. Update status column as you complete features
3. Calculate completion percentage
4. Share with stakeholders

---

## 🆘 Common Issues & Solutions

### Issue: Stock mismatch
**Solution**: Always use `StockLedger` - never update `inventory_balances` directly

### Issue: Wrong profit calculation
**Solution**: Use `unit_cost_snapshot` at time of sale, not current cost

### Issue: Overselling
**Solution**: Implement stock reservation on order ACCEPT status

### Issue: Slow dashboard
**Solution**: Use materialized views or cache KPIs in Redis

---

## 📚 Learning Resources

### Spring Boot
- [Official Spring Boot Docs](https://spring.io/projects/spring-boot)
- [Spring Security JWT Tutorial](https://www.bezkoder.com/spring-boot-jwt-authentication/)

### React
- [React Official Docs](https://react.dev)
- [TanStack Query](https://tanstack.com/query)

### Flutter (for mobile)
- [Flutter Official Docs](https://flutter.dev)
- [Flutter Barcode Scanner](https://pub.dev/packages/flutter_barcode_scanner)

### PostgreSQL
- [PostgreSQL Tutorial](https://www.postgresqltutorial.com/)
- [Indexing Best Practices](https://www.postgresql.org/docs/current/indexes.html)

---

## 🎯 Next Immediate Steps

1. **Review all documentation** (PROJECT_PLAN, DATABASE_SCHEMA, FEATURE_MATRIX)
2. **Set up development environment** (Java 17, PostgreSQL, IDE)
3. **Create Spring Boot project** (use structure above)
4. **Implement Phase 0** (Users, Roles, Auth)
5. **Create first entities** (Tenant, Store, User, Role)
6. **Test authentication** (Login, JWT)
7. **Move to Phase 1** (Products, Stock Ledger)

---

## 💡 Pro Tips

1. **Start Small**: Don't try to build everything at once
2. **Test Early**: Write tests as you go
3. **Use Git**: Commit frequently with clear messages
4. **Document**: Add comments for complex business logic
5. **Review Schema**: Understand database relationships before coding
6. **API First**: Design API contracts before implementation
7. **Mobile Later**: Build backend + web first, then mobile

---

## 🎬 Demo Milestones

### Demo 1 (End of Phase 1)
- Show: Product catalog, incoming stock, stock ledger, expected profit

### Demo 2 (End of Phase 2)
- Show: POS billing, invoice, daily report, profit calculation

### Demo 3 (End of Phase 3)
- Show: Website, place order, availability, order status

### Demo 4 (End of Phase 4)
- Show: Dashboard graphs, top-N products, reports

---

**Good luck with your A to Z Shop Management Application!** 🚀

If you have questions about any specific feature or need help with implementation, feel free to ask!

---

*Last Updated: 2026-02-28*
*Ready to build!*

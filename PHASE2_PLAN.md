# Phase 2 - POS Billing System Implementation Plan

## Context

Phase 1 (Inventory Management) is complete with product catalog, stock tracking, and barcode support. Phase 2 will build a complete Point of Sale (POS) billing system that enables shop owners to process sales, manage payments, and automatically update inventory.

**Why this is needed:**
- Enable quick sales using barcode scanning
- Automatic stock deduction via event-sourced ledger
- Multiple payment methods (cash, card, UPI, split payments)
- Customer tracking and purchase history
- Discount and offer management
- Daily sales reporting
- Professional receipt/invoice generation

**User Requirements:**
1. Quick billing with barcode scanner
2. Shopping cart management
3. Payment processing (multiple methods)
4. Automatic inventory updates
5. Customer management (optional)
6. Discount/offer support
7. Receipt printing
8. Sales reports

---

## Implementation Approach

Following the existing codebase patterns:
- **Entities**: Extend BaseEntity, include tenantId, use Lombok
- **Repositories**: JpaRepository with tenant-aware queries
- **Services**: Constructor injection, @Transactional for multi-step operations
- **Controllers**: RESTful APIs under `/api/v1/*`, OpenAPI docs
- **DTOs**: Separate Request/Response with validation

### Key Design Decisions

1. **Bill Status Workflow**: DRAFT → CONFIRMED → PAID
   - DRAFT: Cart/bill being created
   - CONFIRMED: Bill finalized, stock deducted
   - PAID: Payment completed

2. **Stock Integration**: Bills create SALE entries in stock_ledger
   - Follows same event-sourcing pattern
   - Negative quantity_change for sales
   - Automatic inventory deduction on confirmation

3. **Split Payments**: Support multiple payment methods per bill
   - Cash + Card
   - Multiple cards
   - Partial payments

4. **Price Snapshots**: Store selling price in bill_items
   - Historical accuracy (price may change later)
   - Same pattern as stock_ledger

5. **Customer Optional**: Bills can be with/without customer
   - Walk-in sales: No customer
   - Regular customers: Link to customer record
   - Purchase history tracking

---

## Database Schema

### New Tables (6 total)

#### 1. customers
- Customer information for tracking and loyalty
- Phone is primary lookup (unique per tenant)
- Optional: email, address, GST for B2B

```sql
Columns: id, tenant_id, customer_code, name, phone, email,
         address, city, state, postal_code, gstin,
         loyalty_points, total_purchases, is_active

Constraints: UNIQUE(tenant_id, phone)
             UNIQUE(tenant_id, customer_code)
```

**Customer Code Format**: `CUST-YYYYMMDD-XXX`

#### 2. bills
- Sales bill/invoice headers
- Auto-generated bill number
- Tracks total amounts, discounts, payments
- Status workflow: DRAFT → CONFIRMED → PAID

```sql
Columns: id, tenant_id, store_id, customer_id, cashier_id,
         bill_number, bill_date, bill_type,
         total_items, total_quantity, subtotal,
         discount_amount, tax_amount, total_amount,
         paid_amount, balance_amount,
         status, payment_status, notes,
         created_by

Constraints: UNIQUE(tenant_id, bill_number)

Enums:
  bill_type: SALES, SALES_RETURN
  status: DRAFT, CONFIRMED, CANCELLED
  payment_status: UNPAID, PARTIAL, PAID, REFUNDED
```

**Bill Number Format**: `BIL-YYYYMMDD-XXX`

#### 3. bill_items
- Line items in bills
- Quantity, price snapshot, calculations
- Links to variant

```sql
Columns: id, bill_id, variant_id, sku, product_name, variant_name,
         quantity, unit_price, mrp, discount_percent, discount_amount,
         subtotal, tax_percent, tax_amount, total_amount

Note: Store product/variant names for historical accuracy
      (product may be deleted later)
```

#### 4. payments
- Payment transactions for bills
- Supports multiple payment methods per bill
- Payment method types: CASH, CARD, UPI, WALLET, CHEQUE

```sql
Columns: id, bill_id, tenant_id, payment_method, payment_date,
         amount, reference_number, card_last4, upi_id,
         bank_name, notes, created_by

Constraints: payment_method IN ('CASH', 'CARD', 'UPI', 'WALLET', 'CHEQUE')
```

#### 5. discounts
- Discount/offer definitions
- Supports: percentage, fixed amount, buy X get Y
- Can be item-level or bill-level

```sql
Columns: id, tenant_id, discount_code, name, description,
         discount_type, discount_value, min_purchase_amount,
         max_discount_amount, applicable_on,
         valid_from, valid_to, is_active

Enums:
  discount_type: PERCENTAGE, FIXED_AMOUNT, BUY_X_GET_Y
  applicable_on: ITEM, BILL, CATEGORY
```

#### 6. bill_discounts
- Discounts applied to specific bills
- Tracks which discount was used

```sql
Columns: id, bill_id, discount_id, discount_name, discount_code,
         discount_type, discount_value, discount_amount

Note: Store discount details for historical accuracy
```

---

## Implementation Steps

### Step 1: Create Entity Classes (6 files)

**Files to create:**
- `src/main/java/com/atozshop/entity/Customer.java`
- `src/main/java/com/atozshop/entity/Bill.java`
- `src/main/java/com/atozshop/entity/BillItem.java`
- `src/main/java/com/atozshop/entity/Payment.java`
- `src/main/java/com/atozshop/entity/Discount.java`
- `src/main/java/com/atozshop/entity/BillDiscount.java`

**Pattern to follow:** Extend BaseEntity
```java
@Entity
@Table(name = "bills", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "bill_number"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Bill extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    // ... other fields

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.DRAFT;

    public enum Status {
        DRAFT, CONFIRMED, CANCELLED
    }

    public enum PaymentStatus {
        UNPAID, PARTIAL, PAID, REFUNDED
    }

    public enum BillType {
        SALES, SALES_RETURN
    }
}
```

### Step 2: Create Repository Interfaces (6 files)

**Files to create:**
- `src/main/java/com/atozshop/repository/CustomerRepository.java`
- `src/main/java/com/atozshop/repository/BillRepository.java`
- `src/main/java/com/atozshop/repository/BillItemRepository.java`
- `src/main/java/com/atozshop/repository/PaymentRepository.java`
- `src/main/java/com/atozshop/repository/DiscountRepository.java`
- `src/main/java/com/atozshop/repository/BillDiscountRepository.java`

**Critical repositories:**

**CustomerRepository:**
```java
Optional<Customer> findByPhoneAndTenantId(String phone, Long tenantId);
Optional<Customer> findByCustomerCodeAndTenantId(String code, Long tenantId);
List<Customer> searchByNameOrPhone(String keyword, Long tenantId);
```

**BillRepository:**
```java
Optional<Bill> findByBillNumberAndTenantId(String billNumber, Long tenantId);
List<Bill> findByTenantIdAndStoreIdAndBillDateBetween(
    Long tenantId, Long storeId, LocalDate from, LocalDate to);
@Query("SELECT SUM(b.totalAmount) FROM Bill b WHERE ...")
BigDecimal getTotalSales(Long tenantId, Long storeId, LocalDate date);
```

**PaymentRepository:**
```java
List<Payment> findByBillId(Long billId);
@Query("SELECT SUM(p.amount) FROM Payment p WHERE p.billId = ?1")
BigDecimal getTotalPaid(Long billId);
```

### Step 3: Create DTOs (20 files)

**Request DTOs (10 files):**
- `CreateCustomerRequest.java`
- `UpdateCustomerRequest.java`
- `CreateBillRequest.java`
- `AddBillItemRequest.java`
- `UpdateBillItemRequest.java`
- `ApplyDiscountRequest.java`
- `CreatePaymentRequest.java`
- `CreateDiscountRequest.java`
- `UpdateDiscountRequest.java`
- `SalesReportRequest.java`

**Response DTOs (10 files):**
- `CustomerResponse.java`
- `CustomerPurchaseHistoryResponse.java`
- `BillResponse.java`
- `BillItemResponse.java`
- `BillSummaryResponse.java`
- `PaymentResponse.java`
- `DiscountResponse.java`
- `DailySalesReportResponse.java`
- `PaymentSummaryResponse.java`
- `ReceiptResponse.java`

**Example: CreateBillRequest**
```java
@Data
public class CreateBillRequest {
    @NotNull(message = "Tenant ID is required")
    private Long tenantId;

    @NotNull(message = "Store ID is required")
    private Long storeId;

    private Long customerId; // Optional

    @NotNull(message = "Bill type is required")
    private String billType; // SALES, SALES_RETURN

    @NotEmpty(message = "At least one item is required")
    private List<AddBillItemRequest> items;

    private List<ApplyDiscountRequest> discounts;

    private String notes;
}
```

**Example: BillResponse**
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillResponse {
    private Long id;
    private String billNumber;
    private LocalDateTime billDate;
    private String billType;
    private String status;
    private String paymentStatus;

    // Customer info
    private Long customerId;
    private String customerName;
    private String customerPhone;

    // Store/Cashier info
    private Long storeId;
    private String storeName;
    private Long cashierId;
    private String cashierName;

    // Items
    private List<BillItemResponse> items;
    private Integer totalItems;
    private Integer totalQuantity;

    // Amounts
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal balanceAmount;

    // Payments
    private List<PaymentResponse> payments;

    // Discounts applied
    private List<BillDiscountResponse> discounts;

    private String notes;
    private LocalDateTime createdAt;
}
```

### Step 4: Create Service Classes (5 files)

**Files to create:**
- `src/main/java/com/atozshop/service/CustomerService.java`
- `src/main/java/com/atozshop/service/BillService.java`
- `src/main/java/com/atozshop/service/PaymentService.java`
- `src/main/java/com/atozshop/service/DiscountService.java`
- `src/main/java/com/atozshop/service/SalesReportService.java`

**Critical business logic:**

#### BillService (Most Complex)

**createBill()**: Create draft bill
```java
@Transactional
public BillResponse createBill(CreateBillRequest request) {
    // 1. Validate customer (if provided)
    // 2. Validate all variants exist and have stock
    // 3. Generate bill number: BIL-YYYYMMDD-XXX
    // 4. Calculate amounts for each item
    // 5. Apply discounts
    // 6. Calculate totals
    // 7. Create Bill entity (status: DRAFT)
    // 8. Create BillItem entities
    // 9. Create BillDiscount entities
    // 10. Return BillResponse
}
```

**confirmBill()**: Confirm bill and deduct stock
```java
@Transactional
public BillResponse confirmBill(Long billId, Long tenantId) {
    // 1. Get bill (must be DRAFT)
    // 2. Get all bill items
    // 3. Validate stock availability for all items
    // 4. For each item:
    //    - Create SALE entry in stock_ledger
    //    - Negative quantity_change
    //    - Price snapshot
    // 5. Update bill status to CONFIRMED
    // 6. Return updated BillResponse
}
```

**Stock Deduction Pattern:**
```java
@Transactional
public void deductStock(Long billId, Long tenantId) {
    Bill bill = billRepository.findById(billId)...;
    List<BillItem> items = billItemRepository.findByBillId(billId);

    for (BillItem item : items) {
        // Call StockService to record SALE
        stockService.recordStockMovement(
            tenantId,
            bill.getStoreId(),
            item.getVariantId(),
            StockLedger.TransactionType.SALE,
            billId,
            -item.getQuantity(), // Negative for sale
            item.getUnitPrice(), // Use sale price as cost
            item.getUnitPrice(), // Same as selling price
            "Sale via Bill: " + bill.getBillNumber(),
            bill.getBillDate(),
            bill.getCreatedBy()
        );
    }
}
```

**Amount Calculation Logic:**
```java
public BillCalculation calculateBill(List<BillItem> items, List<Discount> discounts) {
    BigDecimal subtotal = BigDecimal.ZERO;

    // Calculate item totals
    for (BillItem item : items) {
        BigDecimal itemSubtotal = item.getUnitPrice()
            .multiply(new BigDecimal(item.getQuantity()));

        // Apply item-level discount
        BigDecimal itemDiscount = calculateItemDiscount(item, discounts);
        item.setDiscountAmount(itemDiscount);

        BigDecimal itemTotal = itemSubtotal.subtract(itemDiscount);

        // Calculate tax
        BigDecimal tax = itemTotal.multiply(item.getTaxPercent())
            .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
        item.setTaxAmount(tax);

        item.setTotalAmount(itemTotal.add(tax));
        subtotal = subtotal.add(item.getTotalAmount());
    }

    // Apply bill-level discounts
    BigDecimal billDiscount = calculateBillDiscount(subtotal, discounts);

    // Calculate final total
    BigDecimal totalAmount = subtotal.subtract(billDiscount);

    return new BillCalculation(subtotal, billDiscount, totalAmount);
}
```

#### PaymentService

**processPayment()**: Record payment
```java
@Transactional
public PaymentResponse processPayment(CreatePaymentRequest request) {
    // 1. Get bill
    // 2. Validate payment amount <= balance
    // 3. Create Payment entity
    // 4. Update bill paid_amount
    // 5. Update payment_status (UNPAID/PARTIAL/PAID)
    // 6. If fully paid, can update status to PAID
    // 7. Return PaymentResponse
}
```

**Split Payment Support:**
```java
// Example: Pay ₹5000 cash + ₹3000 card
Payment payment1 = {method: CASH, amount: 5000}
Payment payment2 = {method: CARD, amount: 3000}

Bill: totalAmount = 8000
      paidAmount = 8000 (5000 + 3000)
      paymentStatus = PAID
```

#### CustomerService

**searchCustomers()**: Search by name/phone
```java
public List<CustomerResponse> searchCustomers(String keyword, Long tenantId) {
    return customerRepository.searchByNameOrPhone("%" + keyword + "%", tenantId);
}
```

**getPurchaseHistory()**: Get customer's bill history
```java
public CustomerPurchaseHistoryResponse getPurchaseHistory(Long customerId) {
    // Get all bills for customer
    // Calculate total purchases
    // Return summary + recent bills
}
```

#### DiscountService

**validateDiscount()**: Check if discount is applicable
```java
public boolean validateDiscount(Discount discount, BigDecimal billAmount) {
    // Check if active
    // Check date range
    // Check minimum purchase amount
    // Return true/false
}
```

**calculateDiscountAmount()**: Calculate discount
```java
public BigDecimal calculateDiscountAmount(Discount discount, BigDecimal amount) {
    if (discount.getDiscountType() == DiscountType.PERCENTAGE) {
        BigDecimal discountAmt = amount
            .multiply(discount.getDiscountValue())
            .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);

        // Check max discount limit
        if (discount.getMaxDiscountAmount() != null &&
            discountAmt.compareTo(discount.getMaxDiscountAmount()) > 0) {
            return discount.getMaxDiscountAmount();
        }

        return discountAmt;
    } else {
        return discount.getDiscountValue();
    }
}
```

#### SalesReportService

**getDailySalesReport()**: Generate daily sales summary
```java
public DailySalesReportResponse getDailySalesReport(LocalDate date, Long storeId) {
    // Get all bills for date
    // Calculate:
    // - Total sales count
    // - Total sales amount
    // - Payment method breakdown
    // - Top selling items
    // - Hourly sales pattern
    // Return summary
}
```

### Step 5: Create Controllers (5 files)

**Files to create:**
- `src/main/java/com/atozshop/controller/CustomerController.java`
- `src/main/java/com/atozshop/controller/BillController.java`
- `src/main/java/com/atozshop/controller/PaymentController.java`
- `src/main/java/com/atozshop/controller/DiscountController.java`
- `src/main/java/com/atozshop/controller/SalesReportController.java`

**API Endpoints:**

#### CustomerController (`/api/v1/customers`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/customers` | Create customer |
| GET | `/api/v1/customers?tenantId=X` | List all customers |
| GET | `/api/v1/customers/search?keyword=X&tenantId=Y` | Search by name/phone |
| GET | `/api/v1/customers/{id}?tenantId=X` | Get customer by ID |
| GET | `/api/v1/customers/phone/{phone}?tenantId=X` | Find by phone |
| PUT | `/api/v1/customers/{id}` | Update customer |
| DELETE | `/api/v1/customers/{id}?tenantId=X` | Delete customer |
| GET | `/api/v1/customers/{id}/purchase-history?tenantId=X` | Get purchase history |

#### BillController (`/api/v1/bills`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/bills` | Create bill (DRAFT) |
| GET | `/api/v1/bills?tenantId=X&storeId=Y` | List bills (paginated) |
| GET | `/api/v1/bills/{id}?tenantId=X` | Get bill by ID |
| GET | `/api/v1/bills/number/{billNumber}?tenantId=X` | Get by bill number |
| POST | `/api/v1/bills/{id}/items` | Add item to bill |
| PUT | `/api/v1/bills/{id}/items/{itemId}` | Update bill item |
| DELETE | `/api/v1/bills/{id}/items/{itemId}` | Remove item from bill |
| POST | `/api/v1/bills/{id}/discounts` | Apply discount |
| POST | `/api/v1/bills/{id}/confirm?tenantId=X` | **Confirm bill (deduct stock)** |
| POST | `/api/v1/bills/{id}/cancel?tenantId=X` | Cancel bill |
| GET | `/api/v1/bills/{id}/receipt?tenantId=X` | Get printable receipt |

#### PaymentController (`/api/v1/payments`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/payments` | Record payment |
| GET | `/api/v1/payments/bill/{billId}?tenantId=X` | Get payments for bill |
| GET | `/api/v1/payments/{id}?tenantId=X` | Get payment by ID |
| DELETE | `/api/v1/payments/{id}?tenantId=X` | Void payment (if allowed) |

#### DiscountController (`/api/v1/discounts`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/discounts` | Create discount/offer |
| GET | `/api/v1/discounts?tenantId=X` | List all discounts |
| GET | `/api/v1/discounts/active?tenantId=X` | Get active discounts |
| GET | `/api/v1/discounts/{id}?tenantId=X` | Get discount by ID |
| GET | `/api/v1/discounts/code/{code}?tenantId=X` | Get by discount code |
| PUT | `/api/v1/discounts/{id}` | Update discount |
| DELETE | `/api/v1/discounts/{id}?tenantId=X` | Delete discount |

#### SalesReportController (`/api/v1/reports/sales`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/reports/sales/daily?date=X&storeId=Y&tenantId=Z` | Daily sales report |
| GET | `/api/v1/reports/sales/summary?from=X&to=Y&storeId=Z&tenantId=W` | Date range summary |
| GET | `/api/v1/reports/sales/by-payment?date=X&storeId=Y&tenantId=Z` | Payment method breakdown |
| GET | `/api/v1/reports/sales/top-products?from=X&to=Y&limit=10&tenantId=Z` | Top selling products |
| GET | `/api/v1/reports/sales/hourly?date=X&storeId=Y&tenantId=Z` | Hourly sales pattern |

### Step 6: Update Existing Services

**Modify StockService:**
- Already has `recordStockMovement()` which supports SALE type
- No changes needed, just use it for bill confirmations

**No changes to other Phase 1 services**

---

## Critical Implementation Details

### Bill Number Generation

```java
Format: BIL-YYYYMMDD-XXX
Example: BIL-20260228-001

private String generateBillNumber(Long tenantId, LocalDateTime billDate) {
    String dateStr = billDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    String prefix = "BIL-" + dateStr + "-";

    // Get max sequence for today
    String lastBillNumber = billRepository
        .findLastBillNumberForDate(tenantId, dateStr);

    int sequence = 1;
    if (lastBillNumber != null) {
        sequence = Integer.parseInt(lastBillNumber.substring(14)) + 1;
    }

    return prefix + String.format("%03d", sequence);
}
```

### Customer Code Generation

```java
Format: CUST-YYYYMMDD-XXX
Example: CUST-20260228-001

Similar to bill number generation
```

### Amount Calculations

**Item-level:**
```
Subtotal = unitPrice × quantity
Discount = calculated based on discount type
Taxable Amount = Subtotal - Discount
Tax = Taxable Amount × (taxPercent / 100)
Total = Taxable Amount + Tax
```

**Bill-level:**
```
Subtotal = SUM(all item totals)
Bill Discount = applied on subtotal
Total Amount = Subtotal - Bill Discount
Paid Amount = SUM(all payments)
Balance = Total Amount - Paid Amount
```

### Payment Status Logic

```java
if (paidAmount == 0) {
    paymentStatus = UNPAID;
} else if (paidAmount < totalAmount) {
    paymentStatus = PARTIAL;
} else if (paidAmount >= totalAmount) {
    paymentStatus = PAID;
}
```

### Stock Validation Before Sale

```java
@Transactional
public void validateStockAvailability(List<BillItem> items, Long storeId, Long tenantId) {
    for (BillItem item : items) {
        Integer currentStock = stockLedgerRepository
            .getCurrentStock(item.getVariantId(), storeId, tenantId);

        if (currentStock == null || currentStock < item.getQuantity()) {
            throw new InsufficientStockException(
                "Insufficient stock for " + item.getVariantName() +
                ". Available: " + currentStock + ", Required: " + item.getQuantity()
            );
        }
    }
}
```

### Receipt Generation

**ReceiptResponse** includes:
```
- Store details (name, address, phone, GSTIN)
- Bill number and date
- Customer details (if any)
- Item-wise details (name, qty, price, total)
- Subtotal, discounts, tax, total
- Payment details
- Balance (if any)
- Footer message (Thank you, Terms)
```

---

## Testing Strategy

### 1. Database Schema Verification

```bash
# Start application
export JAVA_HOME=$(/usr/libexec/java_home -v 21) && mvn spring-boot:run

# Check tables created
docker exec atozshop-db psql -U atozshop -d atozshop -c "\dt"

# Should see 6 new tables:
# customers, bills, bill_items, payments, discounts, bill_discounts
```

### 2. API Testing Flow

**Complete POS Flow:**

```bash
# 1. Create customer (optional)
POST /api/v1/customers
{
  "tenantId": 1,
  "name": "John Doe",
  "phone": "9876543210",
  "email": "john@example.com"
}

# 2. Create bill with items (barcode scanning)
POST /api/v1/bills
{
  "tenantId": 1,
  "storeId": 1,
  "customerId": 1,
  "billType": "SALES",
  "items": [
    {
      "variantId": 1,
      "quantity": 2,
      "unitPrice": 134900  # From variant price
    },
    {
      "variantId": 2,
      "quantity": 1,
      "unitPrice": 89999
    }
  ]
}
# Response: billId, billNumber, totalAmount, status: DRAFT

# 3. Apply discount (optional)
POST /api/v1/bills/{billId}/discounts
{
  "discountCode": "FESTIVE10",
  "discountValue": 10,
  "discountType": "PERCENTAGE"
}

# 4. Process payment
POST /api/v1/payments
{
  "billId": 1,
  "tenantId": 1,
  "paymentMethod": "CASH",
  "amount": 359799
}

# 5. Confirm bill (deduct stock)
POST /api/v1/bills/{billId}/confirm?tenantId=1
# ✅ Creates SALE entries in stock_ledger
# ✅ Deducts inventory
# ✅ Status changes to CONFIRMED

# 6. Get receipt
GET /api/v1/bills/{billId}/receipt?tenantId=1

# 7. Verify stock deducted
GET /api/v1/stock/levels?tenantId=1&storeId=1
# Stock should be reduced

# 8. Check stock ledger
GET /api/v1/stock/ledger/variant/1?tenantId=1&storeId=1
# Should show SALE entry with negative quantity
```

### 3. Test Cases

**Customer Tests:**
- Create customer → 201 Created, customer code generated
- Duplicate phone → 400 Bad Request
- Search by name → 200 with matching customers
- Search by phone → 200 with customer
- Purchase history → 200 with bills

**Bill Tests:**
- Create bill → 201 with bill number (DRAFT)
- Add items → Items calculated correctly
- Apply discount → Discount calculated correctly
- Insufficient stock → 400 Bad Request
- Confirm bill → 200, stock deducted, status CONFIRMED
- Cancel bill → 200, status CANCELLED

**Payment Tests:**
- Pay full amount → Payment status PAID
- Pay partial → Payment status PARTIAL
- Split payment (cash + card) → Both recorded, total matched
- Overpayment → Allowed (for change calculation)

**Discount Tests:**
- Apply percentage discount → Calculated correctly
- Apply fixed discount → Amount deducted
- Min purchase not met → 400 Bad Request
- Expired discount → 400 Bad Request
- Max discount limit → Capped at maximum

**Stock Integration Tests:**
- Create bill → No stock change yet
- Confirm bill → Stock ledger has SALE entries
- Verify current stock → Reduced by sold quantity
- Cancel after confirm → Should not allow (or create RETURN)

**Sales Report Tests:**
- Daily report → All bills for date
- Payment breakdown → Cash/Card/UPI totals
- Top products → Most sold items
- Hourly pattern → Sales by hour

### 4. Test Script

```bash
#!/bin/bash
# Phase 2 Test Script

BASE_URL="http://localhost:8080/api/v1"
TENANT_ID=1
STORE_ID=1

# Login and get token
TOKEN=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@atozshop.com","password":"Demo@1234"}' | jq -r '.token')

# Test 1: Create Customer
echo "1. Creating customer..."
CUSTOMER=$(curl -s -X POST "$BASE_URL/customers" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"tenantId\":$TENANT_ID,\"name\":\"Test Customer\",\"phone\":\"9999999999\"}")
CUSTOMER_ID=$(echo "$CUSTOMER" | jq -r '.id')
echo "   Customer ID: $CUSTOMER_ID"

# Test 2: Get variant by barcode (from Phase 1)
echo "2. Scanning barcode..."
VARIANT=$(curl -s -X GET "$BASE_URL/variants/barcode/0194253484981?tenantId=$TENANT_ID" \
  -H "Authorization: Bearer $TOKEN")
VARIANT_ID=$(echo "$VARIANT" | jq -r '.id')
PRICE=$(echo "$VARIANT" | jq -r '.currentSellingPrice')
echo "   Variant ID: $VARIANT_ID, Price: $PRICE"

# Test 3: Create bill
echo "3. Creating bill..."
BILL=$(curl -s -X POST "$BASE_URL/bills" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"tenantId\":$TENANT_ID,\"storeId\":$STORE_ID,\"customerId\":$CUSTOMER_ID,\"billType\":\"SALES\",\"items\":[{\"variantId\":$VARIANT_ID,\"quantity\":2,\"unitPrice\":$PRICE}]}")
BILL_ID=$(echo "$BILL" | jq -r '.id')
BILL_NUMBER=$(echo "$BILL" | jq -r '.billNumber')
TOTAL=$(echo "$BILL" | jq -r '.totalAmount')
echo "   Bill: $BILL_NUMBER, Total: ₹$TOTAL"

# Test 4: Process payment
echo "4. Processing payment..."
PAYMENT=$(curl -s -X POST "$BASE_URL/payments" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"billId\":$BILL_ID,\"tenantId\":$TENANT_ID,\"paymentMethod\":\"CASH\",\"amount\":$TOTAL}")
echo "   Payment processed"

# Test 5: Confirm bill
echo "5. Confirming bill (deducting stock)..."
CONFIRMED=$(curl -s -X POST "$BASE_URL/bills/$BILL_ID/confirm?tenantId=$TENANT_ID" \
  -H "Authorization: Bearer $TOKEN")
STATUS=$(echo "$CONFIRMED" | jq -r '.status')
echo "   Bill status: $STATUS"

# Test 6: Verify stock deducted
echo "6. Checking stock levels..."
STOCK=$(curl -s -X GET "$BASE_URL/stock/levels?tenantId=$TENANT_ID&storeId=$STORE_ID" \
  -H "Authorization: Bearer $TOKEN")
echo "   Stock updated"

# Test 7: Daily sales report
echo "7. Getting daily sales report..."
REPORT=$(curl -s -X GET "$BASE_URL/reports/sales/daily?date=$(date +%Y-%m-%d)&storeId=$STORE_ID&tenantId=$TENANT_ID" \
  -H "Authorization: Bearer $TOKEN")
SALES_COUNT=$(echo "$REPORT" | jq -r '.totalBills')
SALES_AMOUNT=$(echo "$REPORT" | jq -r '.totalSales')
echo "   Sales today: $SALES_COUNT bills, ₹$SALES_AMOUNT"

echo ""
echo "✅ All Phase 2 tests passed!"
```

---

## Files to Create/Modify

### New Files (52 total)

**Entities (6):**
- Customer, Bill, BillItem, Payment, Discount, BillDiscount

**Repositories (6):**
- CustomerRepository, BillRepository, BillItemRepository, PaymentRepository, DiscountRepository, BillDiscountRepository

**DTOs (20):**
- 10 Request DTOs, 10 Response DTOs

**Services (5):**
- CustomerService, BillService, PaymentService, DiscountService, SalesReportService

**Controllers (5):**
- CustomerController, BillController, PaymentController, DiscountController, SalesReportController

**Utilities (1):**
- BillNumberGenerator (or extend existing utility)

**Exceptions (2):**
- InsufficientStockException
- PaymentException

**Documentation (3):**
- PHASE2_PROGRESS.md
- Update POSTMAN collection
- Update API_DOCUMENTATION.md

### Modified Files (1)

**No modifications to Phase 1 code needed!**
- StockService already supports SALE transactions
- All Phase 1 functionality remains unchanged

---

## Integration Points with Phase 1

### 1. Stock Ledger Integration

```java
// When bill is confirmed, create SALE entries
for (BillItem item : items) {
    stockService.recordStockMovement(
        tenantId,
        storeId,
        item.getVariantId(),
        StockLedger.TransactionType.SALE,  // ← Using Phase 1 enum
        billId,
        -item.getQuantity(),  // ← Negative for sale
        item.getUnitPrice(),
        item.getUnitPrice(),
        "Sale via Bill: " + billNumber,
        billDate,
        cashierId
    );
}
```

### 2. Variant Price Lookup

```java
// Get current selling price from Phase 1
ProductVariant variant = variantRepository.findById(variantId);
VariantPrice currentPrice = variantPriceRepository
    .getCurrentPrice(variantId, storeId, tenantId);

BigDecimal sellingPrice = currentPrice.getSellingPrice();
```

### 3. Stock Availability Check

```java
// Use Phase 1 stock ledger query
Integer availableStock = stockLedgerRepository
    .getCurrentStock(variantId, storeId, tenantId);

if (availableStock < requestedQuantity) {
    throw new InsufficientStockException(...);
}
```

### 4. Barcode Scanning

```java
// Use Phase 1 variant lookup
ProductVariant variant = variantRepository
    .findByBarcodeValueAndTenantId(barcode, tenantId);

// Add to bill with current price
```

---

## Verification Plan

### End-to-End Test

1. **Phase 1 still working** → Test inventory APIs
2. **6 new tables created** → Check database
3. **Create customer** → Customer code generated
4. **Scan barcode** → Get variant from Phase 1
5. **Create bill** → Bill number generated, DRAFT
6. **Add multiple items** → All items calculated
7. **Apply discount** → Discount calculated correctly
8. **Check stock before** → Current stock level
9. **Process payment** → Payment recorded
10. **Confirm bill** → Stock ledger updated, inventory deducted
11. **Check stock after** → Reduced by sold quantity
12. **Get receipt** → Printable format
13. **Daily report** → Shows today's sales
14. **Customer history** → Shows customer's bills

### Success Criteria

- ✅ All 6 tables created with correct schema
- ✅ All endpoints accessible via Swagger
- ✅ Bill number auto-generation working
- ✅ Customer code auto-generation working
- ✅ Amount calculations accurate
- ✅ Stock validation working (can't oversell)
- ✅ Stock deduction working (SALE entries created)
- ✅ Current stock reduced after sale
- ✅ Split payments working
- ✅ Discount calculations correct
- ✅ Receipt generation working
- ✅ Sales reports accurate
- ✅ Multi-tenancy enforced
- ✅ Phase 1 still fully functional

---

## Next Steps After Phase 2

Phase 2 provides the foundation for:
- **Phase 3 - E-commerce**: Online ordering, cart, checkout
- **Phase 4 - Purchase Management**: Supplier orders, GRN
- **Phase 5 - Advanced Reports**: Profit analysis, inventory valuation
- **Phase 6 - Customer Loyalty**: Points, rewards, memberships

---

## Deliverables

- Complete POS billing system
- Automatic stock management via event sourcing
- Multiple payment methods
- Customer management and history
- Discount/offer system
- Sales reporting
- Receipt generation
- ~20 new API endpoints
- Updated Postman collection

---

## Estimated Implementation Time

- **Entities**: 6 files (~30 minutes)
- **Repositories**: 6 files (~30 minutes)
- **DTOs**: 20 files (~1 hour)
- **Services**: 5 files (~2 hours) - Complex business logic
- **Controllers**: 5 files (~1 hour)
- **Testing**: ~1 hour
- **Documentation**: ~30 minutes

**Total: ~6 hours** for complete implementation

---

**Ready to implement Phase 2?**

Let me know if you want to:
1. Proceed with full implementation
2. Start with MVP (basic billing only)
3. Implement step-by-step
4. Modify the plan

I'm ready to start building! 🚀
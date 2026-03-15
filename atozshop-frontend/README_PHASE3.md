# AtoZShop Frontend - Phase 3 Customer Website

**Status**: 🚧 **In Progress** (Foundation Complete ✅)

## Progress Overview

### Completed ✅

1. **Project Setup** (Task #20)
   - ✅ React 18 + TypeScript
   - ✅ Vite build tool
   - ✅ TailwindCSS for styling
   - ✅ React Router for navigation
   - ✅ Tanstack Query for data fetching
   - ✅ Axios for HTTP requests

2. **API Service Layer** (Task #21)
   - ✅ Complete TypeScript types matching backend DTOs
   - ✅ API service with interceptors
   - ✅ Authentication service
   - ✅ Product service
   - ✅ Order service
   - ✅ Address service
   - ✅ JWT token management
   - ✅ Error handling

3. **Authentication & Routing** (Task #27)
   - ✅ Auth context for state management
   - ✅ Cart context with localStorage persistence
   - ✅ Protected routes
   - ✅ Role-based access control
   - ✅ Login page
   - ✅ Placeholder pages (Home, Admin Dashboard)

### Next Steps 📋

4. **Customer Product Catalog** (Task #22) - **NEXT**
   - 🔲 Home page with featured products
   - 🔲 Product listing page
   - 🔲 Product detail page
   - 🔲 Search functionality
   - 🔲 Category browse

5. **Shopping Cart** (Task #23)
   - 🔲 Cart page
   - 🔲 Add/remove/update items
   - 🔲 Mini cart in header
   - 🔲 Stock validation

6. **Checkout Flow** (Task #24)
   - 🔲 Address selection/management
   - 🔲 Delivery slot selection
   - 🔲 Payment method selection
   - 🔲 Order review and confirmation

7. **Order Tracking** (Task #25)
   - 🔲 My Orders page
   - 🔲 Order detail with status timeline
   - 🔲 Cancel order functionality

8. **Admin Dashboard** (Task #26)
   - 🔲 Order list with filters
   - 🔲 Order detail view
   - 🔲 Order management actions (Accept/Pack/Dispatch/Deliver)

9. **Styling & Responsiveness** (Task #28)
   - 🔲 Component library
   - 🔲 Mobile-first design
   - 🔲 Loading states
   - 🔲 Toast notifications

10. **Testing** (Task #29)
    - 🔲 End-to-end flow testing
    - 🔲 API integration verification

---

## Project Structure

```
atozshop-frontend/
├── src/
│   ├── components/
│   │   ├── common/         # Shared components (ProtectedRoute)
│   │   ├── layout/         # Layout components (Header, Footer)
│   │   ├── customer/       # Customer-specific components
│   │   └── admin/          # Admin-specific components
│   ├── pages/
│   │   ├── auth/           # Login, Register
│   │   ├── customer/       # Customer pages (Home, Products, Cart, etc.)
│   │   └── admin/          # Admin pages (Dashboard, Orders)
│   ├── services/
│   │   ├── api.ts          # Axios configuration with interceptors
│   │   ├── authService.ts  # Authentication API calls
│   │   ├── productService.ts
│   │   ├── orderService.ts
│   │   └── addressService.ts
│   ├── types/
│   │   ├── auth.ts
│   │   ├── product.ts
│   │   ├── order.ts
│   │   ├── address.ts
│   │   └── cart.ts
│   ├── context/
│   │   ├── AuthContext.tsx # Authentication state
│   │   └── CartContext.tsx # Shopping cart state
│   ├── hooks/              # Custom hooks (to be added)
│   ├── utils/              # Utility functions
│   ├── constants/
│   │   └── api.ts          # API endpoints and config
│   ├── App.tsx             # Main app with routing
│   └── main.tsx            # Entry point
├── public/                 # Static assets
├── .env                    # Environment variables
├── tailwind.config.js      # Tailwind configuration
├── postcss.config.js       # PostCSS configuration
├── vite.config.ts          # Vite configuration
└── package.json
```

---

## Technologies Used

### Core
- **React 18** - UI library
- **TypeScript** - Type safety
- **Vite** - Build tool (fast HMR)

### Routing & State
- **React Router v6** - Navigation
- **Tanstack Query** - Server state management
- **Context API** - Client state (Auth, Cart)

### HTTP & Data
- **Axios** - HTTP client with interceptors
- **JWT** - Authentication

### Styling
- **TailwindCSS 4** - Utility-first CSS
- **Lucide React** - Icon library

### Forms & Validation
- **React Hook Form** - Form management
- **Zod** - Schema validation

---

## Getting Started

### Prerequisites
- Node.js 20.19+ or 22.12+ (current: 21.2.0 - shows warnings but works)
- Backend API running on `http://localhost:8080`

### Installation

```bash
cd atozshop-frontend
npm install
```

### Environment Setup

Create `.env` file:

```bash
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

### Development

```bash
npm run dev
```

App will run on `http://localhost:5173`

### Build

```bash
npm run build
```

Output in `dist/` folder

### Preview Production Build

```bash
npm run preview
```

---

## API Integration

### Authentication

All API calls automatically include JWT token via Axios interceptor:

```typescript
// Login sets token in localStorage
await authService.login({ username, password });

// All subsequent requests include: Authorization: Bearer <token>
```

### Error Handling

Centralized error handling in Axios interceptor:
- **401 Unauthorized** → Auto-redirect to login
- **Network errors** → User-friendly error message
- **Server errors** → Formatted error response

### Data Fetching Pattern

Using Tanstack Query for caching and state management:

```typescript
const { data, isLoading, error } = useQuery({
  queryKey: ['products'],
  queryFn: () => productService.getAllProducts(),
});
```

---

## State Management

### Auth Context

Manages user authentication state across the app:

```typescript
const { user, isAuthenticated, isAdmin, login, logout } = useAuth();
```

### Cart Context

Manages shopping cart with localStorage persistence:

```typescript
const { cart, addToCart, updateQuantity, removeFromCart, clearCart } = useCart();
```

**Features:**
- Automatic stock validation before adding items
- Real-time total calculation
- Persists across page refreshes
- Syncs with backend stock availability

---

## Routing Structure

### Public Routes
- `/login` - Login page

### Protected Routes (Customer)
- `/` - Home page
- `/products` - Product listing
- `/products/:id` - Product detail
- `/cart` - Shopping cart
- `/checkout` - Checkout flow
- `/orders` - My orders
- `/orders/:id` - Order detail

### Protected Routes (Admin Only)
- `/admin` - Admin dashboard
- `/admin/orders` - Order management
- `/admin/orders/:id` - Order detail with actions

---

## Key Features Implemented

### ✅ JWT Authentication
- Automatic token storage and retrieval
- Auto-logout on token expiration (401)
- Protected routes with role-based access

### ✅ Shopping Cart
- Add/remove/update items
- Stock availability checking
- Persistent cart (localStorage)
- Real-time subtotal calculation

### ✅ Type Safety
- Complete TypeScript types matching backend DTOs
- Compile-time error catching
- IDE autocomplete support

### ✅ Error Handling
- Network error handling
- User-friendly error messages
- Automatic retry logic

---

## Development Guidelines

### Code Style
- Use functional components with hooks
- TypeScript strict mode enabled
- ESLint configured
- Consistent file naming (PascalCase for components)

### Component Structure
```typescript
import type { FC } from 'react';

interface MyComponentProps {
  title: string;
  onClick?: () => void;
}

export const MyComponent: FC<MyComponentProps> = ({ title, onClick }) => {
  return (
    <div className="p-4">
      <h2>{title}</h2>
    </div>
  );
};
```

### API Service Pattern
```typescript
export const myService = {
  getData: async (): Promise<DataType> => {
    const response = await apiService.get<DataType>('/endpoint');
    return response.data;
  },
};
```

---

## Next Development Phase

### Immediate Tasks (This Week)

1. **Build Product Catalog Pages** (Task #22)
   - Implement home page with product grid
   - Create product listing with filters
   - Build product detail page with variant selection
   - Add search functionality

2. **Implement Shopping Cart UI** (Task #23)
   - Cart page with item list
   - Mini cart dropdown in header
   - Quantity update controls
   - Stock validation messages

3. **Create Layout Components**
   - Header with navigation and cart icon
   - Footer
   - Main layout wrapper

---

## Testing Checklist

### Manual Testing Steps

1. **Authentication**
   - ✅ Login with valid credentials
   - ✅ Invalid credentials show error
   - ✅ Protected routes redirect to login
   - ✅ Admin routes blocked for non-admin users
   - ✅ Logout clears token

2. **Product Browsing** (Not yet implemented)
   - [ ] Products load from API
   - [ ] Search works
   - [ ] Category filters work
   - [ ] Product details display correctly
   - [ ] Stock status shows (In Stock, Low Stock, Out of Stock)

3. **Shopping Cart** (Not yet implemented)
   - [ ] Add to cart works
   - [ ] Cart persists on page refresh
   - [ ] Stock validation prevents overselling
   - [ ] Update quantity works
   - [ ] Remove item works
   - [ ] Cart icon shows item count

4. **Checkout** (Not yet implemented)
   - [ ] Address selection/creation
   - [ ] Delivery slot selection
   - [ ] Payment method selection
   - [ ] Order placement
   - [ ] Stock reserved on order accept

5. **Order Tracking** (Not yet implemented)
   - [ ] Order list displays
   - [ ] Order detail shows status timeline
   - [ ] Cancel order works
   - [ ] Status updates in real-time

---

## Build Status

✅ **TypeScript Compilation**: SUCCESS
✅ **Vite Build**: SUCCESS
✅ **Bundle Size**: 300.88 kB (97.96 kB gzipped)

---

## Known Issues

1. **Node.js Version Warning**
   - Current: v21.2.0
   - Required: 20.19+ or 22.12+
   - **Impact**: Shows warnings but builds successfully
   - **Action**: Upgrade Node.js when convenient

2. **Missing Demo Data**
   - Login credentials placeholder shown
   - **Action**: Update with actual demo credentials after backend testing

---

## Demo Credentials

```
Customer:
  Email: customer@atozshop.com
  Password: customer123

Admin:
  Email: admin@atozshop.com
  Password: admin123
```

---

## API Endpoints Connected

### Auth
- ✅ POST `/api/v1/auth/login`

### Products (Ready to use)
- GET `/api/v1/public/products`
- GET `/api/v1/public/products/{id}`
- GET `/api/v1/public/products/search`
- GET `/api/v1/public/products/category/{id}`
- GET `/api/v1/public/products/variant/{id}/availability`

### Orders (Ready to use)
- POST `/api/v1/orders`
- GET `/api/v1/orders/customer/{customerId}`
- GET `/api/v1/orders/{id}`
- POST `/api/v1/orders/{id}/cancel`

### Admin Orders (Ready to use)
- GET `/api/v1/admin/orders`
- POST `/api/v1/admin/orders/{id}/accept`
- POST `/api/v1/admin/orders/{id}/pack`
- POST `/api/v1/admin/orders/{id}/dispatch`
- POST `/api/v1/admin/orders/{id}/deliver`

### Addresses (Ready to use)
- POST `/api/v1/customers/addresses`
- GET `/api/v1/customers/addresses/customer/{id}`
- PUT `/api/v1/customers/addresses/{id}`
- DELETE `/api/v1/customers/addresses/{id}`
- PUT `/api/v1/customers/addresses/{id}/default`

---

## Files Created (Foundation)

### Types (5 files)
- `src/types/auth.ts`
- `src/types/product.ts`
- `src/types/order.ts`
- `src/types/address.ts`
- `src/types/cart.ts`

### Services (5 files)
- `src/services/api.ts` - Axios configuration
- `src/services/authService.ts`
- `src/services/productService.ts`
- `src/services/orderService.ts`
- `src/services/addressService.ts`

### Context (2 files)
- `src/context/AuthContext.tsx`
- `src/context/CartContext.tsx`

### Components (1 file)
- `src/components/common/ProtectedRoute.tsx`

### Pages (3 files)
- `src/pages/auth/LoginPage.tsx`
- `src/pages/customer/HomePage.tsx`
- `src/pages/admin/AdminDashboard.tsx`

### Config (4 files)
- `.env`
- `src/constants/api.ts`
- `tailwind.config.js`
- `postcss.config.js`

### Core (2 files)
- `src/App.tsx` - Updated with routing
- `src/index.css` - Updated with Tailwind

**Total**: **22 files created/updated**

---

## Performance Considerations

### Code Splitting
- React Router lazy loading (to be added)
- Dynamic imports for large components

### Caching Strategy
- Tanstack Query caches API responses
- Cart persisted in localStorage
- JWT token stored in localStorage

### Bundle Optimization
- Vite tree-shaking
- Production build minification
- Gzip compression enabled

---

## Next Actions

1. ✅ Start development server: `npm run dev`
2. ✅ Test login functionality
3. 🔄 Build product catalog pages (Task #22)
4. 🔄 Implement shopping cart UI (Task #23)
5. 🔄 Create checkout flow (Task #24)

---

**Last Updated**: March 1, 2026
**Foundation Status**: ✅ **COMPLETE**
**Next Milestone**: Customer Product Catalog (Est. 2-3 days)

import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Suspense, lazy } from 'react';
import { AuthProvider } from './context/AuthContext';
import { CartProvider } from './context/CartContext';
import { ProtectedRoute } from './components/common/ProtectedRoute';
import { LoginPage } from './pages/auth/LoginPage';
import { HomePage } from './pages/customer/HomePage';
import { ProductDetailPage } from './pages/customer/ProductDetailPage';
import { CartPage } from './pages/customer/CartPage';
import { CheckoutPage } from './pages/customer/CheckoutPage';
import { MyOrdersPage } from './pages/customer/MyOrdersPage';
import { OrderDetailPage } from './pages/customer/OrderDetailPage';
import { AdminDashboard } from './pages/admin/AdminDashboard';
import { OrdersManagementPage } from './pages/admin/OrdersManagementPage';

// Phase 2: POS Billing - Lazy loaded to prevent import errors
const POSBillingPage = lazy(() => import('./pages/admin/pos/POSBillingPage').then(m => ({ default: m.POSBillingPage })));

// Phase 2: Customer Management - Lazy loaded
const CustomersListPage = lazy(() => import('./pages/admin/customers/CustomersListPage').then(m => ({ default: m.CustomersListPage })));
const CreateCustomerPage = lazy(() => import('./pages/admin/customers/CreateCustomerPage').then(m => ({ default: m.CreateCustomerPage })));
const EditCustomerPage = lazy(() => import('./pages/admin/customers/EditCustomerPage').then(m => ({ default: m.EditCustomerPage })));
const CustomerDetailPage = lazy(() => import('./pages/admin/customers/CustomerDetailPage').then(m => ({ default: m.CustomerDetailPage })));

// Phase 1: Stock Management - Lazy loaded
const StockDashboardPage = lazy(() => import('./pages/admin/stock/StockDashboardPage').then(m => ({ default: m.StockDashboardPage })));
const AddIncomingStockPage = lazy(() => import('./pages/admin/stock/AddIncomingStockPage').then(m => ({ default: m.AddIncomingStockPage })));
const StockLedgerPage = lazy(() => import('./pages/admin/stock/StockLedgerPage').then(m => ({ default: m.StockLedgerPage })));

// Phase 1: Products Management - Lazy loaded
const ProductsListPage = lazy(() => import('./pages/admin/products/ProductsListPage').then(m => ({ default: m.ProductsListPage })));
const CreateProductPage = lazy(() => import('./pages/admin/products/CreateProductPage').then(m => ({ default: m.CreateProductPage })));
const EditProductPage = lazy(() => import('./pages/admin/products/EditProductPage').then(m => ({ default: m.EditProductPage })));

// Phase 1: Categories Management - Lazy loaded
const CategoriesPage = lazy(() => import('./pages/admin/categories/CategoriesPage').then(m => ({ default: m.CategoriesPage })));

// Phase 1: Suppliers Management - Lazy loaded
const SuppliersPage = lazy(() => import('./pages/admin/suppliers/SuppliersPage').then(m => ({ default: m.SuppliersPage })));

// Phase 2: Bills Management - Lazy loaded
const BillsHistoryPage = lazy(() => import('./pages/admin/bills/BillsHistoryPage').then(m => ({ default: m.BillsHistoryPage })));
const BillDetailPage = lazy(() => import('./pages/admin/bills/BillDetailPage').then(m => ({ default: m.BillDetailPage })));

// Phase 2: Discounts Management - Lazy loaded
const DiscountsPage = lazy(() => import('./pages/admin/discounts/DiscountsPage').then(m => ({ default: m.DiscountsPage })));

// Phase 2: Sales Reports - Lazy loaded
const SalesReportsPage = lazy(() => import('./pages/admin/reports/SalesReportsPage').then(m => ({ default: m.SalesReportsPage })));

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
    },
  },
});

// Loading component for lazy routes
const PageLoader = () => (
  <div className="flex items-center justify-center min-h-screen">
    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
  </div>
);

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <CartProvider>
          <Router>
            <Routes>
              {/* Public routes */}
              <Route path="/login" element={<LoginPage />} />

              {/* Customer routes */}
              <Route
                path="/"
                element={
                  <ProtectedRoute>
                    <HomePage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/products/:id"
                element={
                  <ProtectedRoute>
                    <ProductDetailPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/cart"
                element={
                  <ProtectedRoute>
                    <CartPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/checkout"
                element={
                  <ProtectedRoute>
                    <CheckoutPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/orders"
                element={
                  <ProtectedRoute>
                    <MyOrdersPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/orders/:id"
                element={
                  <ProtectedRoute>
                    <OrderDetailPage />
                  </ProtectedRoute>
                }
              />

              {/* Admin routes */}
              <Route
                path="/admin"
                element={
                  <ProtectedRoute requireAdmin>
                    <AdminDashboard />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/orders"
                element={
                  <ProtectedRoute requireAdmin>
                    <OrdersManagementPage />
                  </ProtectedRoute>
                }
              />

              {/* Phase 2: POS Billing */}
              <Route
                path="/admin/pos"
                element={
                  <ProtectedRoute requireAdmin>
                    <Suspense fallback={<PageLoader />}>
                      <POSBillingPage />
                    </Suspense>
                  </ProtectedRoute>
                }
              />

              {/* Phase 2: Customer Management */}
              <Route
                path="/admin/customers"
                element={
                  <ProtectedRoute requireAdmin>
                    <Suspense fallback={<PageLoader />}>
                      <CustomersListPage />
                    </Suspense>
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/customers/new"
                element={
                  <ProtectedRoute requireAdmin>
                    <Suspense fallback={<PageLoader />}>
                      <CreateCustomerPage />
                    </Suspense>
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/customers/:id"
                element={
                  <ProtectedRoute requireAdmin>
                    <Suspense fallback={<PageLoader />}>
                      <CustomerDetailPage />
                    </Suspense>
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/customers/:id/edit"
                element={
                  <ProtectedRoute requireAdmin>
                    <Suspense fallback={<PageLoader />}>
                      <EditCustomerPage />
                    </Suspense>
                  </ProtectedRoute>
                }
              />

              {/* Phase 1: Stock Management */}
              <Route
                path="/admin/stock"
                element={
                  <ProtectedRoute requireAdmin>
                    <Suspense fallback={<PageLoader />}>
                      <StockDashboardPage />
                    </Suspense>
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/stock/add-incoming"
                element={
                  <ProtectedRoute requireAdmin>
                    <Suspense fallback={<PageLoader />}>
                      <AddIncomingStockPage />
                    </Suspense>
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/stock/ledger"
                element={
                  <ProtectedRoute requireAdmin>
                    <Suspense fallback={<PageLoader />}>
                      <StockLedgerPage />
                    </Suspense>
                  </ProtectedRoute>
                }
              />

              {/* Phase 1: Products Management */}
              <Route
                path="/admin/products"
                element={
                  <ProtectedRoute requireAdmin>
                    <Suspense fallback={<PageLoader />}>
                      <ProductsListPage />
                    </Suspense>
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/products/new"
                element={
                  <ProtectedRoute requireAdmin>
                    <Suspense fallback={<PageLoader />}>
                      <CreateProductPage />
                    </Suspense>
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/products/:id/edit"
                element={
                  <ProtectedRoute requireAdmin>
                    <Suspense fallback={<PageLoader />}>
                      <EditProductPage />
                    </Suspense>
                  </ProtectedRoute>
                }
              />

              {/* Phase 1: Categories Management */}
              <Route
                path="/admin/categories"
                element={
                  <ProtectedRoute requireAdmin>
                    <Suspense fallback={<PageLoader />}>
                      <CategoriesPage />
                    </Suspense>
                  </ProtectedRoute>
                }
              />

              {/* Phase 1: Suppliers Management */}
              <Route
                path="/admin/suppliers"
                element={
                  <ProtectedRoute requireAdmin>
                    <Suspense fallback={<PageLoader />}>
                      <SuppliersPage />
                    </Suspense>
                  </ProtectedRoute>
                }
              />

              {/* Phase 2: Bills Management */}
              <Route
                path="/admin/bills"
                element={
                  <ProtectedRoute requireAdmin>
                    <Suspense fallback={<PageLoader />}>
                      <BillsHistoryPage />
                    </Suspense>
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/bills/:id"
                element={
                  <ProtectedRoute requireAdmin>
                    <Suspense fallback={<PageLoader />}>
                      <BillDetailPage />
                    </Suspense>
                  </ProtectedRoute>
                }
              />

              {/* Phase 2: Discounts Management */}
              <Route
                path="/admin/discounts"
                element={
                  <ProtectedRoute requireAdmin>
                    <Suspense fallback={<PageLoader />}>
                      <DiscountsPage />
                    </Suspense>
                  </ProtectedRoute>
                }
              />

              {/* Phase 2: Sales Reports */}
              <Route
                path="/admin/reports"
                element={
                  <ProtectedRoute requireAdmin>
                    <Suspense fallback={<PageLoader />}>
                      <SalesReportsPage />
                    </Suspense>
                  </ProtectedRoute>
                }
              />

              {/* Catch all - redirect to home */}
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </Router>
        </CartProvider>
      </AuthProvider>
    </QueryClientProvider>
  );
}

export default App;

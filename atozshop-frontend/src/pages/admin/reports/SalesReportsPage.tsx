import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { MainLayout } from '../../../components/layout/MainLayout';
import { salesReportService } from '../../../services/salesReportService';
import { authService } from '../../../services/authService';
import {
  TrendingUp,
  DollarSign,
  ShoppingCart,
  Users,
  Calendar,
  Download,
  CreditCard,
} from 'lucide-react';

export const SalesReportsPage = () => {
  const today = new Date().toISOString().split('T')[0];
  const lastMonth = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];

  const [dateRange, setDateRange] = useState({
    fromDate: lastMonth,
    toDate: today,
  });

  const [selectedStore] = useState(authService.getStoreId());

  const { data: dailyReport } = useQuery({
    queryKey: ['daily-sales', today],
    queryFn: () => salesReportService.getDailySalesReport(today),
  });

  const { data: salesSummary } = useQuery({
    queryKey: ['sales-summary', dateRange.fromDate, dateRange.toDate],
    queryFn: () => salesReportService.getSalesSummary(dateRange.fromDate, dateRange.toDate),
  });

  const { data: paymentBreakdown } = useQuery({
    queryKey: ['payment-breakdown', dateRange.fromDate, dateRange.toDate],
    queryFn: () => salesReportService.getPaymentBreakdown(dateRange.fromDate, dateRange.toDate),
  });

  const { data: topCustomers } = useQuery({
    queryKey: ['top-customers'],
    queryFn: () => salesReportService.getTopCustomers(10),
  });

  const handleExport = () => {
    alert('Export functionality coming soon!');
  };

  return (
    <MainLayout>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Sales Reports</h1>
            <p className="text-gray-600 mt-1">View sales analytics and performance metrics</p>
          </div>
          <button
            onClick={handleExport}
            className="flex items-center gap-2 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50"
          >
            <Download className="h-5 w-5" />
            Export Report
          </button>
        </div>

        {/* Date Range Filter */}
        <div className="bg-white rounded-lg shadow p-4">
          <div className="flex items-center gap-4">
            <Calendar className="h-5 w-5 text-gray-400" />
            <label className="text-sm font-medium text-gray-700">Date Range:</label>
            <input
              type="date"
              value={dateRange.fromDate}
              onChange={(e) => setDateRange({ ...dateRange, fromDate: e.target.value })}
              className="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
            />
            <span className="text-gray-500">to</span>
            <input
              type="date"
              value={dateRange.toDate}
              onChange={(e) => setDateRange({ ...dateRange, toDate: e.target.value })}
              className="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
            />
          </div>
        </div>

        {/* Today's Summary */}
        <div>
          <h2 className="text-xl font-bold text-gray-900 mb-4">Today's Summary</h2>
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div className="bg-white rounded-lg shadow p-6">
              <div className="flex items-center justify-between mb-2">
                <p className="text-sm text-gray-600">Total Sales</p>
                <DollarSign className="h-5 w-5 text-primary-600" />
              </div>
              <p className="text-3xl font-bold text-gray-900">
                ₹{dailyReport?.totalSales.toLocaleString() || 0}
              </p>
            </div>

            <div className="bg-white rounded-lg shadow p-6">
              <div className="flex items-center justify-between mb-2">
                <p className="text-sm text-gray-600">Transactions</p>
                <ShoppingCart className="h-5 w-5 text-green-600" />
              </div>
              <p className="text-3xl font-bold text-gray-900">
                {dailyReport?.transactionCount || 0}
              </p>
            </div>

            <div className="bg-white rounded-lg shadow p-6">
              <div className="flex items-center justify-between mb-2">
                <p className="text-sm text-gray-600">Items Sold</p>
                <Package className="h-5 w-5 text-blue-600" />
              </div>
              <p className="text-3xl font-bold text-gray-900">
                {dailyReport?.totalItemsSold || 0}
              </p>
            </div>

            <div className="bg-white rounded-lg shadow p-6">
              <div className="flex items-center justify-between mb-2">
                <p className="text-sm text-gray-600">Avg Order Value</p>
                <TrendingUp className="h-5 w-5 text-purple-600" />
              </div>
              <p className="text-3xl font-bold text-gray-900">
                ₹{dailyReport?.averageOrderValue.toLocaleString() || 0}
              </p>
            </div>
          </div>
        </div>

        {/* Period Summary */}
        <div>
          <h2 className="text-xl font-bold text-gray-900 mb-4">Period Summary</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="bg-white rounded-lg shadow p-6">
              <div className="flex items-center justify-between mb-2">
                <p className="text-sm text-gray-600">Total Revenue</p>
                <DollarSign className="h-5 w-5 text-primary-600" />
              </div>
              <p className="text-3xl font-bold text-primary-600">
                ₹{salesSummary?.totalRevenue.toLocaleString() || 0}
              </p>
            </div>

            <div className="bg-white rounded-lg shadow p-6">
              <div className="flex items-center justify-between mb-2">
                <p className="text-sm text-gray-600">Total Transactions</p>
                <ShoppingCart className="h-5 w-5 text-green-600" />
              </div>
              <p className="text-3xl font-bold text-gray-900">
                {salesSummary?.totalTransactions || 0}
              </p>
            </div>

            <div className="bg-white rounded-lg shadow p-6">
              <div className="flex items-center justify-between mb-2">
                <p className="text-sm text-gray-600">Avg Transaction</p>
                <TrendingUp className="h-5 w-5 text-purple-600" />
              </div>
              <p className="text-3xl font-bold text-gray-900">
                ₹{salesSummary?.averageTransactionValue.toLocaleString() || 0}
              </p>
            </div>
          </div>
        </div>

        {/* Payment Methods Breakdown */}
        <div className="bg-white rounded-lg shadow">
          <div className="px-6 py-4 border-b border-gray-200">
            <div className="flex items-center gap-2">
              <CreditCard className="h-5 w-5 text-primary-600" />
              <h2 className="text-lg font-semibold text-gray-900">Payment Methods</h2>
            </div>
          </div>
          <div className="p-6">
            {paymentBreakdown && paymentBreakdown.length > 0 ? (
              <div className="space-y-4">
                {paymentBreakdown.map((payment: any) => {
                  const total = paymentBreakdown.reduce(
                    (sum: number, p: any) => sum + p.amount,
                    0
                  );
                  const percentage = ((payment.amount / total) * 100).toFixed(1);

                  return (
                    <div key={payment.paymentMethod}>
                      <div className="flex items-center justify-between mb-2">
                        <div>
                          <p className="text-sm font-medium text-gray-900">
                            {payment.paymentMethod}
                          </p>
                          <p className="text-xs text-gray-500">{payment.count} transactions</p>
                        </div>
                        <div className="text-right">
                          <p className="text-sm font-semibold text-gray-900">
                            ₹{payment.amount.toLocaleString()}
                          </p>
                          <p className="text-xs text-gray-500">{percentage}%</p>
                        </div>
                      </div>
                      <div className="w-full bg-gray-200 rounded-full h-2">
                        <div
                          className="bg-primary-600 h-2 rounded-full"
                          style={{ width: `${percentage}%` }}
                        />
                      </div>
                    </div>
                  );
                })}
              </div>
            ) : (
              <p className="text-center text-gray-500 py-4">No payment data available</p>
            )}
          </div>
        </div>

        {/* Top Selling Products */}
        <div className="bg-white rounded-lg shadow">
          <div className="px-6 py-4 border-b border-gray-200">
            <h2 className="text-lg font-semibold text-gray-900">Top Selling Products</h2>
          </div>
          <div className="overflow-x-auto">
            {salesSummary?.topSellingProducts && salesSummary.topSellingProducts.length > 0 ? (
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                      Rank
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                      Product
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                      SKU
                    </th>
                    <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">
                      Qty Sold
                    </th>
                    <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">
                      Revenue
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {salesSummary.topSellingProducts.map((product: any, index: number) => (
                    <tr key={product.variantId}>
                      <td className="px-6 py-4">
                        <div className="text-sm font-semibold text-gray-900">#{index + 1}</div>
                      </td>
                      <td className="px-6 py-4">
                        <div className="text-sm font-medium text-gray-900">
                          {product.productName}
                        </div>
                        {product.variantName && (
                          <div className="text-sm text-gray-500">{product.variantName}</div>
                        )}
                      </td>
                      <td className="px-6 py-4">
                        <div className="text-sm font-mono text-gray-900">{product.sku}</div>
                      </td>
                      <td className="px-6 py-4 text-right">
                        <div className="text-sm font-medium text-gray-900">
                          {product.quantitySold}
                        </div>
                      </td>
                      <td className="px-6 py-4 text-right">
                        <div className="text-sm font-semibold text-primary-600">
                          ₹{product.revenue.toLocaleString()}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            ) : (
              <div className="p-12 text-center text-gray-500">No sales data available</div>
            )}
          </div>
        </div>

        {/* Top Customers */}
        <div className="bg-white rounded-lg shadow">
          <div className="px-6 py-4 border-b border-gray-200">
            <div className="flex items-center gap-2">
              <Users className="h-5 w-5 text-primary-600" />
              <h2 className="text-lg font-semibold text-gray-900">Top Customers</h2>
            </div>
          </div>
          <div className="overflow-x-auto">
            {topCustomers && topCustomers.length > 0 ? (
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                      Rank
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                      Customer
                    </th>
                    <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">
                      Total Purchases
                    </th>
                    <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">
                      Orders
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {topCustomers.map((customer: any, index: number) => (
                    <tr key={customer.id}>
                      <td className="px-6 py-4">
                        <div className="text-sm font-semibold text-gray-900">#{index + 1}</div>
                      </td>
                      <td className="px-6 py-4">
                        <div className="text-sm font-medium text-gray-900">{customer.name}</div>
                        <div className="text-sm text-gray-500">{customer.phone}</div>
                      </td>
                      <td className="px-6 py-4 text-right">
                        <div className="text-sm font-semibold text-primary-600">
                          ₹{customer.totalPurchases?.toLocaleString() || 0}
                        </div>
                      </td>
                      <td className="px-6 py-4 text-right">
                        <div className="text-sm text-gray-900">{customer.orderCount || 0}</div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            ) : (
              <div className="p-12 text-center text-gray-500">No customer data available</div>
            )}
          </div>
        </div>
      </div>
    </MainLayout>
  );
};

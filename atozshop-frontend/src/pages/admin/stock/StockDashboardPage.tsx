import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { MainLayout } from '../../../components/layout/MainLayout';
import { stockService } from '../../../services/stockService';
import { authService } from '../../../services/authService';
import { Package, AlertTriangle, TrendingUp, Calendar, Plus, Eye } from 'lucide-react';
import { Link } from 'react-router-dom';

export const StockDashboardPage = () => {
  const [selectedStore] = useState(authService.getStoreId());

  const { data: currentStock, isLoading: stockLoading } = useQuery({
    queryKey: ['current-stock', selectedStore],
    queryFn: () => stockService.getCurrentStock(selectedStore),
  });

  const { data: lowStockAlerts, isLoading: alertsLoading } = useQuery({
    queryKey: ['low-stock-alerts', selectedStore],
    queryFn: () => stockService.getLowStockAlerts(selectedStore),
  });

  const totalStockValue = currentStock?.reduce(
    (sum, item) => sum + item.currentStock * (item.costPrice || 0),
    0
  );

  const totalStockUnits = currentStock?.reduce((sum, item) => sum + item.currentStock, 0);

  const criticalStockItems = lowStockAlerts?.filter(
    (item) => item.currentStock <= item.reorderLevel * 0.5
  );

  return (
    <MainLayout>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Stock Dashboard</h1>
            <p className="text-gray-600 mt-1">Monitor inventory levels and stock movements</p>
          </div>
          <Link
            to="/admin/stock/add-incoming"
            className="flex items-center gap-2 px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700"
          >
            <Plus className="h-5 w-5" />
            Add Incoming Stock
          </Link>
        </div>

        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between mb-2">
              <p className="text-sm text-gray-600">Total Stock Value</p>
              <TrendingUp className="h-5 w-5 text-primary-600" />
            </div>
            <p className="text-3xl font-bold text-gray-900">
              ₹{totalStockValue?.toLocaleString() || 0}
            </p>
            <p className="text-xs text-gray-500 mt-1">Current inventory value</p>
          </div>

          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between mb-2">
              <p className="text-sm text-gray-600">Total Units</p>
              <Package className="h-5 w-5 text-blue-600" />
            </div>
            <p className="text-3xl font-bold text-gray-900">
              {totalStockUnits?.toLocaleString() || 0}
            </p>
            <p className="text-xs text-gray-500 mt-1">Across all products</p>
          </div>

          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between mb-2">
              <p className="text-sm text-gray-600">Low Stock Items</p>
              <AlertTriangle className="h-5 w-5 text-yellow-600" />
            </div>
            <p className="text-3xl font-bold text-yellow-600">
              {lowStockAlerts?.length || 0}
            </p>
            <p className="text-xs text-gray-500 mt-1">Need attention</p>
          </div>

          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between mb-2">
              <p className="text-sm text-gray-600">Critical Stock</p>
              <AlertTriangle className="h-5 w-5 text-red-600" />
            </div>
            <p className="text-3xl font-bold text-red-600">
              {criticalStockItems?.length || 0}
            </p>
            <p className="text-xs text-gray-500 mt-1">Below 50% reorder level</p>
          </div>
        </div>

        {/* Low Stock Alerts */}
        {lowStockAlerts && lowStockAlerts.length > 0 && (
          <div className="bg-white rounded-lg shadow">
            <div className="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
              <div className="flex items-center gap-2">
                <AlertTriangle className="h-5 w-5 text-yellow-600" />
                <h2 className="text-xl font-bold text-gray-900">Low Stock Alerts</h2>
              </div>
              <Link
                to="/admin/stock/ledger"
                className="text-primary-600 hover:text-primary-700 text-sm font-medium"
              >
                View Stock Ledger →
              </Link>
            </div>

            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Product
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      SKU
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Current Stock
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Reorder Level
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Status
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Last Updated
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {lowStockAlerts.map((item) => {
                    const stockPercentage = (item.currentStock / item.reorderLevel) * 100;
                    const isCritical = stockPercentage <= 50;

                    return (
                      <tr key={item.variantId} className="hover:bg-gray-50">
                        <td className="px-6 py-4">
                          <div className="text-sm font-medium text-gray-900">
                            {item.productName}
                          </div>
                          {item.variantName && (
                            <div className="text-sm text-gray-500">{item.variantName}</div>
                          )}
                        </td>
                        <td className="px-6 py-4">
                          <div className="text-sm font-mono text-gray-900">{item.sku}</div>
                        </td>
                        <td className="px-6 py-4">
                          <div
                            className={`text-sm font-semibold ${
                              isCritical ? 'text-red-600' : 'text-yellow-600'
                            }`}
                          >
                            {item.currentStock}
                          </div>
                        </td>
                        <td className="px-6 py-4">
                          <div className="text-sm text-gray-900">{item.reorderLevel}</div>
                        </td>
                        <td className="px-6 py-4">
                          <span
                            className={`px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${
                              isCritical
                                ? 'bg-red-100 text-red-800'
                                : 'bg-yellow-100 text-yellow-800'
                            }`}
                          >
                            {isCritical ? 'Critical' : 'Low Stock'}
                          </span>
                        </td>
                        <td className="px-6 py-4">
                          <div className="text-sm text-gray-500">
                            {new Date(item.lastUpdated).toLocaleDateString()}
                          </div>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* Current Stock Overview */}
        <div className="bg-white rounded-lg shadow">
          <div className="px-6 py-4 border-b border-gray-200">
            <h2 className="text-xl font-bold text-gray-900">Current Stock Overview</h2>
          </div>

          {stockLoading ? (
            <div className="p-12 text-center text-gray-500">Loading stock data...</div>
          ) : currentStock && currentStock.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Product
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      SKU
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Stock
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Cost Price
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Stock Value
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Status
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {currentStock.slice(0, 20).map((item) => {
                    const stockValue = item.currentStock * (item.costPrice || 0);
                    const isLowStock = lowStockAlerts?.some((a) => a.variantId === item.variantId);

                    return (
                      <tr key={item.variantId} className="hover:bg-gray-50">
                        <td className="px-6 py-4">
                          <div className="text-sm font-medium text-gray-900">
                            {item.productName}
                          </div>
                          {item.variantName && (
                            <div className="text-sm text-gray-500">{item.variantName}</div>
                          )}
                        </td>
                        <td className="px-6 py-4">
                          <div className="text-sm font-mono text-gray-900">{item.sku}</div>
                        </td>
                        <td className="px-6 py-4">
                          <div className="text-sm font-semibold text-gray-900">
                            {item.currentStock}
                          </div>
                        </td>
                        <td className="px-6 py-4">
                          <div className="text-sm text-gray-900">
                            ₹{item.costPrice?.toLocaleString() || 0}
                          </div>
                        </td>
                        <td className="px-6 py-4">
                          <div className="text-sm font-medium text-gray-900">
                            ₹{stockValue.toLocaleString()}
                          </div>
                        </td>
                        <td className="px-6 py-4">
                          {isLowStock ? (
                            <span className="px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full bg-yellow-100 text-yellow-800">
                              Low Stock
                            </span>
                          ) : item.currentStock === 0 ? (
                            <span className="px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full bg-red-100 text-red-800">
                              Out of Stock
                            </span>
                          ) : (
                            <span className="px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full bg-green-100 text-green-800">
                              In Stock
                            </span>
                          )}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="p-12 text-center">
              <Package className="h-16 w-16 mx-auto text-gray-400 mb-4" />
              <p className="text-gray-500">No stock data available</p>
            </div>
          )}

          {currentStock && currentStock.length > 20 && (
            <div className="px-6 py-4 border-t border-gray-200 text-center">
              <Link
                to="/admin/stock/ledger"
                className="text-primary-600 hover:text-primary-700 text-sm font-medium"
              >
                View All Stock ({currentStock.length} items) →
              </Link>
            </div>
          )}
        </div>
      </div>
    </MainLayout>
  );
};

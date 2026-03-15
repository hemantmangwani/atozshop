import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { MainLayout } from '../../../components/layout/MainLayout';
import { stockService } from '../../../services/stockService';
import { authService } from '../../../services/authService';
import { ArrowLeft, Calendar, Filter, Download, TrendingUp, TrendingDown } from 'lucide-react';
import { Link } from 'react-router-dom';

type TransactionTypeFilter = 'ALL' | 'INCOMING' | 'SALE' | 'ADJUSTMENT' | 'RETURN';

export const StockLedgerPage = () => {
  const [selectedStore] = useState(authService.getStoreId());
  const [typeFilter, setTypeFilter] = useState<TransactionTypeFilter>('ALL');
  const [dateFilter, setDateFilter] = useState({
    fromDate: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
    toDate: new Date().toISOString().split('T')[0],
  });

  const { data: ledgerEntries, isLoading } = useQuery({
    queryKey: ['stock-ledger', selectedStore, dateFilter.fromDate, dateFilter.toDate],
    queryFn: () =>
      stockService.getStockLedger(
        selectedStore,
        new Date(dateFilter.fromDate),
        new Date(dateFilter.toDate)
      ),
  });

  const filteredEntries =
    typeFilter === 'ALL'
      ? ledgerEntries
      : ledgerEntries?.filter((entry) => entry.transactionType === typeFilter);

  const getTransactionBadge = (type: string) => {
    switch (type) {
      case 'INCOMING':
        return 'bg-green-100 text-green-800';
      case 'SALE':
        return 'bg-blue-100 text-blue-800';
      case 'ADJUSTMENT':
        return 'bg-yellow-100 text-yellow-800';
      case 'RETURN':
        return 'bg-purple-100 text-purple-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  return (
    <MainLayout>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-4">
            <Link to="/admin/stock" className="p-2 hover:bg-gray-100 rounded-lg transition-colors">
              <ArrowLeft className="h-5 w-5 text-gray-600" />
            </Link>
            <div>
              <h1 className="text-3xl font-bold text-gray-900">Stock Ledger</h1>
              <p className="text-gray-600 mt-1">Complete history of all stock movements</p>
            </div>
          </div>
          <button
            className="flex items-center gap-2 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50"
            onClick={() => {
              // Export functionality can be added here
              alert('Export functionality coming soon!');
            }}
          >
            <Download className="h-5 w-5" />
            Export
          </button>
        </div>

        {/* Filters */}
        <div className="bg-white rounded-lg shadow p-4">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            {/* Date Range */}
            <div className="md:col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-1">Date Range</label>
              <div className="flex items-center gap-2">
                <input
                  type="date"
                  value={dateFilter.fromDate}
                  onChange={(e) => setDateFilter({ ...dateFilter, fromDate: e.target.value })}
                  className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
                />
                <span className="text-gray-500">to</span>
                <input
                  type="date"
                  value={dateFilter.toDate}
                  onChange={(e) => setDateFilter({ ...dateFilter, toDate: e.target.value })}
                  className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
                />
              </div>
            </div>

            {/* Transaction Type Filter */}
            <div className="md:col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Transaction Type
              </label>
              <div className="flex items-center gap-2">
                <Filter className="h-5 w-5 text-gray-400" />
                <select
                  value={typeFilter}
                  onChange={(e) => setTypeFilter(e.target.value as TransactionTypeFilter)}
                  className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
                >
                  <option value="ALL">All Transactions</option>
                  <option value="INCOMING">Incoming Stock</option>
                  <option value="SALE">Sales</option>
                  <option value="ADJUSTMENT">Adjustments</option>
                  <option value="RETURN">Returns</option>
                </select>
              </div>
            </div>
          </div>
        </div>

        {/* Summary Stats */}
        {filteredEntries && filteredEntries.length > 0 && (
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div className="bg-white rounded-lg shadow p-4">
              <p className="text-sm text-gray-600">Total Transactions</p>
              <p className="text-2xl font-bold text-gray-900">{filteredEntries.length}</p>
            </div>
            <div className="bg-white rounded-lg shadow p-4">
              <p className="text-sm text-gray-600">Stock In</p>
              <p className="text-2xl font-bold text-green-600">
                +
                {filteredEntries
                  .filter((e) => e.quantityChange > 0)
                  .reduce((sum, e) => sum + e.quantityChange, 0)}
              </p>
            </div>
            <div className="bg-white rounded-lg shadow p-4">
              <p className="text-sm text-gray-600">Stock Out</p>
              <p className="text-2xl font-bold text-red-600">
                {filteredEntries
                  .filter((e) => e.quantityChange < 0)
                  .reduce((sum, e) => sum + Math.abs(e.quantityChange), 0)}
              </p>
            </div>
            <div className="bg-white rounded-lg shadow p-4">
              <p className="text-sm text-gray-600">Net Change</p>
              <p className="text-2xl font-bold text-primary-600">
                {filteredEntries.reduce((sum, e) => sum + e.quantityChange, 0) >= 0 ? '+' : ''}
                {filteredEntries.reduce((sum, e) => sum + e.quantityChange, 0)}
              </p>
            </div>
          </div>
        )}

        {/* Ledger Table */}
        <div className="bg-white rounded-lg shadow overflow-hidden">
          {isLoading ? (
            <div className="p-12 text-center text-gray-500">Loading ledger entries...</div>
          ) : filteredEntries && filteredEntries.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Date & Time
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Product
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      SKU
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Type
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Quantity Change
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Balance After
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Reference
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Notes
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {filteredEntries.map((entry) => (
                    <tr key={entry.id} className="hover:bg-gray-50">
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm text-gray-900">
                          {new Date(entry.transactionDate).toLocaleDateString()}
                        </div>
                        <div className="text-xs text-gray-500">
                          {new Date(entry.transactionDate).toLocaleTimeString()}
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <div className="text-sm font-medium text-gray-900">
                          {entry.productName}
                        </div>
                        {entry.variantName && (
                          <div className="text-sm text-gray-500">{entry.variantName}</div>
                        )}
                      </td>
                      <td className="px-6 py-4">
                        <div className="text-sm font-mono text-gray-900">{entry.sku}</div>
                      </td>
                      <td className="px-6 py-4">
                        <span
                          className={`px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${getTransactionBadge(
                            entry.transactionType
                          )}`}
                        >
                          {entry.transactionType}
                        </span>
                      </td>
                      <td className="px-6 py-4">
                        <div
                          className={`text-sm font-semibold flex items-center gap-1 ${
                            entry.quantityChange > 0 ? 'text-green-600' : 'text-red-600'
                          }`}
                        >
                          {entry.quantityChange > 0 ? (
                            <>
                              <TrendingUp className="h-4 w-4" />
                              +{entry.quantityChange}
                            </>
                          ) : (
                            <>
                              <TrendingDown className="h-4 w-4" />
                              {entry.quantityChange}
                            </>
                          )}
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <div className="text-sm font-medium text-gray-900">
                          {entry.balanceAfter}
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <div className="text-sm text-gray-900">{entry.referenceId || '-'}</div>
                      </td>
                      <td className="px-6 py-4">
                        <div className="text-sm text-gray-500 max-w-xs truncate">
                          {entry.notes || '-'}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="p-12 text-center">
              <Calendar className="h-16 w-16 mx-auto text-gray-400 mb-4" />
              <p className="text-gray-500">No ledger entries found</p>
              <p className="text-sm text-gray-400 mt-2">Try adjusting your filters</p>
            </div>
          )}
        </div>
      </div>
    </MainLayout>
  );
};

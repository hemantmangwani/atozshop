import React, { useState, useEffect } from 'react';
import { orderService } from '../../services/orderService';
import { authService } from '../../services/authService';
import type { OrderSummaryResponse } from '../../types/order';
import { OrderStatusBadge } from '../../components/OrderStatusBadge';
import { AdminOrderActions } from '../../components/AdminOrderActions';
import { OrderDetailsModal } from '../../components/OrderDetailsModal';

type StatusFilter = 'ALL' | 'NEW' | 'ACCEPTED' | 'PACKED' | 'OUT_FOR_DELIVERY' | 'DELIVERED' | 'CANCELLED';

export const OrdersManagementPage: React.FC = () => {
  const [orders, setOrders] = useState<OrderSummaryResponse[]>([]);
  const [filteredOrders, setFilteredOrders] = useState<OrderSummaryResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('ALL');
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedOrder, setSelectedOrder] = useState<number | null>(null);
  const [showDetailsModal, setShowDetailsModal] = useState(false);

  const user = authService.getCurrentUser();

  useEffect(() => {
    loadOrders();
  }, [statusFilter]);

  useEffect(() => {
    filterOrders();
  }, [orders, searchTerm, statusFilter]);

  const loadOrders = async () => {
    try {
      setLoading(true);
      setError(null);

      const status = statusFilter === 'ALL' ? undefined : statusFilter;
      const data = await orderService.getAllOrders(status);

      setOrders(data);
    } catch (err) {
      setError('Failed to load orders');
      console.error('Error loading orders:', err);
    } finally {
      setLoading(false);
    }
  };

  const filterOrders = () => {
    let filtered = [...orders];

    // Filter by status
    if (statusFilter !== 'ALL') {
      filtered = filtered.filter(order => order.status === statusFilter);
    }

    // Filter by search term
    if (searchTerm) {
      const term = searchTerm.toLowerCase();
      filtered = filtered.filter(order =>
        order.orderNumber.toLowerCase().includes(term) ||
        order.customerName.toLowerCase().includes(term) ||
        order.customerPhone.toLowerCase().includes(term)
      );
    }

    setFilteredOrders(filtered);
  };

  const handleOrderUpdate = async () => {
    await loadOrders();
  };

  const handleViewDetails = (orderId: number) => {
    setSelectedOrder(orderId);
    setShowDetailsModal(true);
  };

  const getStatusCounts = () => {
    return {
      ALL: orders.length,
      NEW: orders.filter(o => o.status === 'NEW').length,
      ACCEPTED: orders.filter(o => o.status === 'ACCEPTED').length,
      PACKED: orders.filter(o => o.status === 'PACKED').length,
      OUT_FOR_DELIVERY: orders.filter(o => o.status === 'OUT_FOR_DELIVERY').length,
      DELIVERED: orders.filter(o => o.status === 'DELIVERED').length,
      CANCELLED: orders.filter(o => o.status === 'CANCELLED').length,
    };
  };

  const statusCounts = getStatusCounts();

  if (loading && orders.length === 0) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="flex justify-center items-center h-64">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-2">Order Management</h1>
        <p className="text-gray-600">Manage and fulfill customer orders</p>
      </div>

      {/* Statistics Cards */}
      <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-7 gap-4 mb-6">
        <StatCard
          label="All Orders"
          count={statusCounts.ALL}
          active={statusFilter === 'ALL'}
          onClick={() => setStatusFilter('ALL')}
          color="gray"
        />
        <StatCard
          label="New"
          count={statusCounts.NEW}
          active={statusFilter === 'NEW'}
          onClick={() => setStatusFilter('NEW')}
          color="blue"
          badge={statusCounts.NEW > 0}
        />
        <StatCard
          label="Accepted"
          count={statusCounts.ACCEPTED}
          active={statusFilter === 'ACCEPTED'}
          onClick={() => setStatusFilter('ACCEPTED')}
          color="purple"
        />
        <StatCard
          label="Packed"
          count={statusCounts.PACKED}
          active={statusFilter === 'PACKED'}
          onClick={() => setStatusFilter('PACKED')}
          color="indigo"
        />
        <StatCard
          label="Dispatched"
          count={statusCounts.OUT_FOR_DELIVERY}
          active={statusFilter === 'OUT_FOR_DELIVERY'}
          onClick={() => setStatusFilter('OUT_FOR_DELIVERY')}
          color="yellow"
        />
        <StatCard
          label="Delivered"
          count={statusCounts.DELIVERED}
          active={statusFilter === 'DELIVERED'}
          onClick={() => setStatusFilter('DELIVERED')}
          color="green"
        />
        <StatCard
          label="Cancelled"
          count={statusCounts.CANCELLED}
          active={statusFilter === 'CANCELLED'}
          onClick={() => setStatusFilter('CANCELLED')}
          color="red"
        />
      </div>

      {/* Search and Filters */}
      <div className="bg-white rounded-lg shadow p-4 mb-6">
        <div className="flex flex-col md:flex-row gap-4">
          <div className="flex-1">
            <input
              type="text"
              placeholder="Search by order number, customer name, or phone..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          <button
            onClick={loadOrders}
            disabled={loading}
            className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
          >
            {loading ? (
              <>
                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                Refreshing...
              </>
            ) : (
              <>
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                </svg>
                Refresh
              </>
            )}
          </button>
        </div>
      </div>

      {/* Error Message */}
      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-6">
          {error}
        </div>
      )}

      {/* Orders Table */}
      <div className="bg-white rounded-lg shadow overflow-hidden">
        {filteredOrders.length === 0 ? (
          <div className="text-center py-12">
            <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
            <p className="mt-2 text-gray-500">
              {searchTerm ? 'No orders found matching your search' : `No ${statusFilter.toLowerCase()} orders`}
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Order
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Customer
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Date
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Items
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Amount
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Status
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Payment
                  </th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {filteredOrders.map((order) => (
                  <tr key={order.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-medium text-gray-900">
                        {order.orderNumber}
                      </div>
                      <div className="text-sm text-gray-500">
                        {order.deliverySlot}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-medium text-gray-900">
                        {order.customerName}
                      </div>
                      <div className="text-sm text-gray-500">
                        {order.customerPhone}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">
                        {new Date(order.orderDate).toLocaleDateString()}
                      </div>
                      <div className="text-sm text-gray-500">
                        {new Date(order.orderDate).toLocaleTimeString()}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {order.totalItems} {order.totalItems === 1 ? 'item' : 'items'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-semibold text-gray-900">
                        ₹{order.totalAmount.toLocaleString()}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <OrderStatusBadge status={order.status} />
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${
                        order.paymentStatus === 'PAID'
                          ? 'bg-green-100 text-green-800'
                          : order.paymentStatus === 'PENDING'
                          ? 'bg-yellow-100 text-yellow-800'
                          : 'bg-gray-100 text-gray-800'
                      }`}>
                        {order.paymentStatus}
                      </span>
                      <div className="text-xs text-gray-500 mt-1">
                        {order.paymentMethod}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                      <div className="flex items-center justify-end gap-2">
                        <button
                          onClick={() => handleViewDetails(order.id)}
                          className="text-blue-600 hover:text-blue-900"
                          title="View Details"
                        >
                          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                          </svg>
                        </button>
                        <AdminOrderActions
                          order={order}
                          userId={user?.id || 0}
                          onUpdate={handleOrderUpdate}
                        />
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Order Details Modal */}
      {showDetailsModal && selectedOrder && (
        <OrderDetailsModal
          orderId={selectedOrder}
          onClose={() => {
            setShowDetailsModal(false);
            setSelectedOrder(null);
          }}
          onUpdate={handleOrderUpdate}
          isAdmin={true}
        />
      )}
    </div>
  );
};

interface StatCardProps {
  label: string;
  count: number;
  active: boolean;
  onClick: () => void;
  color: string;
  badge?: boolean;
}

const StatCard: React.FC<StatCardProps> = ({ label, count, active, onClick, color, badge }) => {
  const colorClasses = {
    gray: active ? 'bg-gray-100 border-gray-400' : 'bg-white border-gray-200 hover:border-gray-300',
    blue: active ? 'bg-blue-100 border-blue-400' : 'bg-white border-gray-200 hover:border-blue-300',
    purple: active ? 'bg-purple-100 border-purple-400' : 'bg-white border-gray-200 hover:border-purple-300',
    indigo: active ? 'bg-indigo-100 border-indigo-400' : 'bg-white border-gray-200 hover:border-indigo-300',
    yellow: active ? 'bg-yellow-100 border-yellow-400' : 'bg-white border-gray-200 hover:border-yellow-300',
    green: active ? 'bg-green-100 border-green-400' : 'bg-white border-gray-200 hover:border-green-300',
    red: active ? 'bg-red-100 border-red-400' : 'bg-white border-gray-200 hover:border-red-300',
  };

  return (
    <button
      onClick={onClick}
      className={`relative p-4 border-2 rounded-lg transition-all ${colorClasses[color as keyof typeof colorClasses]} cursor-pointer`}
    >
      {badge && (
        <span className="absolute -top-2 -right-2 flex h-5 w-5">
          <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-red-400 opacity-75"></span>
          <span className="relative inline-flex rounded-full h-5 w-5 bg-red-500"></span>
        </span>
      )}
      <div className="text-sm text-gray-600 mb-1">{label}</div>
      <div className="text-2xl font-bold text-gray-900">{count}</div>
    </button>
  );
};

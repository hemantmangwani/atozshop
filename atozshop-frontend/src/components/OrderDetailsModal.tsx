import React, { useState, useEffect } from 'react';
import { orderService } from '../services/orderService';
import { authService } from '../services/authService';
import type { OrderResponse } from '../types/order';
import { OrderStatusBadge } from './OrderStatusBadge';
import { AdminOrderActions } from './AdminOrderActions';

interface OrderDetailsModalProps {
  orderId: number;
  onClose: () => void;
  onUpdate?: () => void;
  isAdmin?: boolean;
}

export const OrderDetailsModal: React.FC<OrderDetailsModalProps> = ({
  orderId,
  onClose,
  onUpdate,
  isAdmin = false,
}) => {
  const [order, setOrder] = useState<OrderResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const user = authService.getCurrentUser();

  useEffect(() => {
    loadOrderDetails();
  }, [orderId]);

  const loadOrderDetails = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await orderService.getOrderById(orderId);
      setOrder(data);
    } catch (err) {
      setError('Failed to load order details');
      console.error('Error loading order:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleUpdate = async () => {
    await loadOrderDetails();
    if (onUpdate) {
      onUpdate();
    }
  };

  const getStatusTimeline = () => {
    if (!order) return [];

    const timeline: Array<{ status: string; date: string | null; label: string }> = [
      { status: 'NEW', date: order.orderDate, label: 'Order Placed' },
      { status: 'ACCEPTED', date: order.acceptedAt, label: 'Order Accepted' },
      { status: 'PACKED', date: order.packedAt, label: 'Order Packed' },
      { status: 'DISPATCHED', date: order.dispatchedAt, label: 'Order Dispatched' },
      { status: 'DELIVERED', date: order.deliveredAt, label: 'Order Delivered' },
    ];

    if (order.status === 'CANCELLED') {
      return [
        { status: 'NEW', date: order.orderDate, label: 'Order Placed' },
        { status: 'CANCELLED', date: order.cancelledAt, label: 'Order Cancelled' },
      ];
    }

    return timeline;
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg max-w-4xl w-full max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 flex items-center justify-between">
          <div>
            <h2 className="text-2xl font-bold text-gray-900">Order Details</h2>
            {order && (
              <p className="text-sm text-gray-600 mt-1">
                {order.orderNumber} • {new Date(order.orderDate).toLocaleString()}
              </p>
            )}
          </div>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 transition-colors"
          >
            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        {/* Content */}
        <div className="px-6 py-4">
          {loading ? (
            <div className="flex justify-center items-center h-64">
              <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
            </div>
          ) : error ? (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
              {error}
            </div>
          ) : order ? (
            <div className="space-y-6">
              {/* Status and Actions */}
              <div className="flex items-center justify-between">
                <div>
                  <OrderStatusBadge status={order.status} />
                  <p className="text-sm text-gray-600 mt-2">
                    Payment: <span className="font-semibold">{order.paymentMethod}</span> •{' '}
                    <span className={order.paymentStatus === 'PAID' ? 'text-green-600' : 'text-yellow-600'}>
                      {order.paymentStatus}
                    </span>
                  </p>
                </div>
                {isAdmin && user && (
                  <AdminOrderActions
                    order={{
                      id: order.id,
                      orderNumber: order.orderNumber,
                      orderDate: order.orderDate,
                      status: order.status,
                      paymentMethod: order.paymentMethod,
                      paymentStatus: order.paymentStatus,
                      totalAmount: order.totalAmount,
                      totalItems: order.totalItems,
                      customerName: order.customerName,
                      customerPhone: order.customerPhone,
                      deliverySlot: order.deliverySlot,
                    }}
                    userId={user.id}
                    onUpdate={handleUpdate}
                  />
                )}
              </div>

              {/* Status Timeline */}
              <div className="bg-gray-50 rounded-lg p-4">
                <h3 className="font-semibold text-gray-900 mb-4">Order Timeline</h3>
                <div className="space-y-3">
                  {getStatusTimeline().map((item, index) => (
                    <div key={index} className="flex items-center gap-3">
                      <div className={`w-3 h-3 rounded-full ${
                        item.date ? 'bg-green-500' : 'bg-gray-300'
                      }`} />
                      <div className="flex-1">
                        <p className={`text-sm font-medium ${
                          item.date ? 'text-gray-900' : 'text-gray-400'
                        }`}>
                          {item.label}
                        </p>
                        {item.date && (
                          <p className="text-xs text-gray-500">
                            {new Date(item.date).toLocaleString()}
                          </p>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Customer Information */}
              <div>
                <h3 className="font-semibold text-gray-900 mb-3">Customer Information</h3>
                <div className="bg-gray-50 rounded-lg p-4 space-y-2">
                  <div className="flex items-center gap-2">
                    <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                    </svg>
                    <span className="text-gray-900">{order.customerName}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                    </svg>
                    <span className="text-gray-900">{order.customerEmail}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                    </svg>
                    <span className="text-gray-900">{order.customerPhone}</span>
                  </div>
                </div>
              </div>

              {/* Delivery Address */}
              <div>
                <h3 className="font-semibold text-gray-900 mb-3">Delivery Address</h3>
                <div className="bg-gray-50 rounded-lg p-4">
                  <p className="text-gray-900">{order.deliveryAddress.formattedAddress}</p>
                  <p className="text-sm text-gray-600 mt-2">
                    Contact: {order.deliveryAddress.phone}
                  </p>
                  <p className="text-sm text-gray-600">
                    Delivery Slot: <span className="font-medium">{order.deliverySlot}</span>
                  </p>
                  {order.deliveryFee > 0 && (
                    <p className="text-sm text-gray-600">
                      Delivery Fee: <span className="font-medium">₹{order.deliveryFee.toLocaleString()}</span>
                    </p>
                  )}
                </div>
              </div>

              {/* Order Items */}
              <div>
                <h3 className="font-semibold text-gray-900 mb-3">Order Items ({order.totalItems})</h3>
                <div className="bg-gray-50 rounded-lg overflow-hidden">
                  <table className="min-w-full">
                    <thead className="bg-gray-100">
                      <tr>
                        <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Product</th>
                        <th className="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase">Qty</th>
                        <th className="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase">Price</th>
                        <th className="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase">Total</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-200">
                      {order.items.map((item) => (
                        <tr key={item.id}>
                          <td className="px-4 py-3">
                            <div className="text-sm font-medium text-gray-900">
                              {item.productName}
                            </div>
                            <div className="text-xs text-gray-500">
                              {item.variantName}
                            </div>
                            <div className="text-xs text-gray-400">
                              SKU: {item.sku}
                            </div>
                          </td>
                          <td className="px-4 py-3 text-center text-sm text-gray-900">
                            {item.quantity}
                          </td>
                          <td className="px-4 py-3 text-right text-sm text-gray-900">
                            ₹{item.unitPrice.toLocaleString()}
                          </td>
                          <td className="px-4 py-3 text-right text-sm font-semibold text-gray-900">
                            ₹{item.totalAmount.toLocaleString()}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>

              {/* Order Summary */}
              <div className="bg-gray-50 rounded-lg p-4">
                <h3 className="font-semibold text-gray-900 mb-3">Order Summary</h3>
                <div className="space-y-2">
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">Subtotal</span>
                    <span className="text-gray-900">₹{order.subtotal.toLocaleString()}</span>
                  </div>
                  {order.discountAmount > 0 && (
                    <div className="flex justify-between text-sm">
                      <span className="text-gray-600">Discount</span>
                      <span className="text-green-600">-₹{order.discountAmount.toLocaleString()}</span>
                    </div>
                  )}
                  {order.taxAmount > 0 && (
                    <div className="flex justify-between text-sm">
                      <span className="text-gray-600">Tax</span>
                      <span className="text-gray-900">₹{order.taxAmount.toLocaleString()}</span>
                    </div>
                  )}
                  {order.deliveryFee > 0 && (
                    <div className="flex justify-between text-sm">
                      <span className="text-gray-600">Delivery Fee</span>
                      <span className="text-gray-900">₹{order.deliveryFee.toLocaleString()}</span>
                    </div>
                  )}
                  <div className="border-t border-gray-300 pt-2">
                    <div className="flex justify-between">
                      <span className="font-semibold text-gray-900">Total</span>
                      <span className="font-bold text-lg text-gray-900">₹{order.totalAmount.toLocaleString()}</span>
                    </div>
                  </div>
                </div>
              </div>

              {/* Customer Notes */}
              {order.customerNotes && (
                <div>
                  <h3 className="font-semibold text-gray-900 mb-3">Customer Notes</h3>
                  <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
                    <p className="text-sm text-gray-700">{order.customerNotes}</p>
                  </div>
                </div>
              )}

              {/* Cancel Reason */}
              {order.status === 'CANCELLED' && order.cancelReason && (
                <div>
                  <h3 className="font-semibold text-gray-900 mb-3">Cancellation Reason</h3>
                  <div className="bg-red-50 border border-red-200 rounded-lg p-4">
                    <p className="text-sm text-gray-700">{order.cancelReason}</p>
                  </div>
                </div>
              )}
            </div>
          ) : null}
        </div>

        {/* Footer */}
        <div className="sticky bottom-0 bg-gray-50 border-t border-gray-200 px-6 py-4 flex justify-end">
          <button
            onClick={onClose}
            className="px-6 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700 transition-colors"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
};

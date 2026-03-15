import { useParams, Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { MainLayout } from '../../components/layout/MainLayout';
import { orderService } from '../../services/orderService';
import { authService } from '../../services/authService';
import { useAuth } from '../../context/AuthContext';
import { Loader2, ArrowLeft, Package, MapPin, Check, X } from 'lucide-react';
import { format } from 'date-fns';

export const OrderDetailPage = () => {
  const { id } = useParams<{ id: string }>();
  const { user } = useAuth();
  const queryClient = useQueryClient();

  const { data: order, isLoading, error } = useQuery({
    queryKey: ['order', id],
    queryFn: () => orderService.getOrderById(Number(id)),
    enabled: !!id,
  });

  const cancelOrderMutation = useMutation({
    mutationFn: (reason: string) =>
      orderService.cancelOrder(Number(id), {
        tenantId: authService.getTenantId(),
        cancelReason: reason,
        cancelledBy: user!.id,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['order', id] });
      queryClient.invalidateQueries({ queryKey: ['orders'] });
    },
  });

  const handleCancelOrder = () => {
    const reason = prompt('Please provide a reason for cancellation:');
    if (reason) {
      cancelOrderMutation.mutate(reason);
    }
  };

  const statusSteps = [
    { key: 'placedAt', label: 'Order Placed', status: 'NEW' },
    { key: 'acceptedAt', label: 'Order Accepted', status: 'ACCEPTED' },
    { key: 'packedAt', label: 'Packed', status: 'PACKED' },
    { key: 'dispatchedAt', label: 'Out for Delivery', status: 'OUT_FOR_DELIVERY' },
    { key: 'deliveredAt', label: 'Delivered', status: 'DELIVERED' },
  ];

  const getCurrentStepIndex = () => {
    if (!order) return -1;
    return statusSteps.findIndex((step) => step.status === order.status);
  };

  if (isLoading) {
    return (
      <MainLayout>
        <div className="flex items-center justify-center py-12">
          <Loader2 className="h-8 w-8 animate-spin text-primary-600" />
          <span className="ml-2 text-gray-600">Loading order...</span>
        </div>
      </MainLayout>
    );
  }

  if (error || !order) {
    return (
      <MainLayout>
        <div className="max-w-4xl mx-auto">
          <div className="bg-red-50 border border-red-200 rounded-lg p-4 text-red-700">
            <p className="font-semibold">Order not found</p>
            <Link to="/orders" className="text-primary-600 hover:underline mt-2 inline-block">
              ← Back to orders
            </Link>
          </div>
        </div>
      </MainLayout>
    );
  }

  const currentStepIndex = getCurrentStepIndex();
  const canCancel = order.status === 'NEW' || order.status === 'ACCEPTED';

  return (
    <MainLayout>
      <div className="max-w-4xl mx-auto">
        <Link
          to="/orders"
          className="text-primary-600 hover:text-primary-700 flex items-center space-x-1 mb-6"
        >
          <ArrowLeft className="h-4 w-4" />
          <span>Back to orders</span>
        </Link>

        {/* Order Header */}
        <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
          <div className="flex items-start justify-between mb-4">
            <div>
              <h1 className="text-2xl font-bold text-gray-900">Order #{order.orderNumber}</h1>
              <p className="text-sm text-gray-600 mt-1">
                Placed on {format(new Date(order.orderDate), 'MMMM dd, yyyy')}
              </p>
            </div>
            <div className="text-right">
              <p className="text-2xl font-bold text-gray-900">₹{order.totalAmount.toFixed(2)}</p>
              <p className="text-sm text-gray-600 mt-1">{order.paymentMethod}</p>
            </div>
          </div>

          {canCancel && (
            <button
              onClick={handleCancelOrder}
              disabled={cancelOrderMutation.isPending}
              className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 disabled:opacity-50 text-sm"
            >
              {cancelOrderMutation.isPending ? 'Cancelling...' : 'Cancel Order'}
            </button>
          )}
        </div>

        {/* Order Status Timeline */}
        {order.status !== 'CANCELLED' && (
          <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-6">Order Status</h2>
            <div className="relative">
              {statusSteps.map((step, index) => {
                const isCompleted = index <= currentStepIndex;
                const isCurrent = index === currentStepIndex;
                const timestamp = order[step.key as keyof typeof order];

                return (
                  <div key={step.key} className="relative pb-8 last:pb-0">
                    {index < statusSteps.length - 1 && (
                      <div
                        className={`absolute left-4 top-8 -ml-px h-full w-0.5 ${
                          isCompleted ? 'bg-primary-600' : 'bg-gray-200'
                        }`}
                      />
                    )}
                    <div className="relative flex items-start">
                      <div className="flex items-center">
                        <div
                          className={`h-8 w-8 rounded-full flex items-center justify-center ${
                            isCompleted
                              ? 'bg-primary-600'
                              : isCurrent
                              ? 'bg-primary-200 border-2 border-primary-600'
                              : 'bg-gray-200'
                          }`}
                        >
                          {isCompleted && <Check className="h-5 w-5 text-white" />}
                        </div>
                      </div>
                      <div className="ml-4 flex-1">
                        <p
                          className={`font-medium ${
                            isCompleted ? 'text-gray-900' : 'text-gray-500'
                          }`}
                        >
                          {step.label}
                        </p>
                        {timestamp && (
                          <p className="text-sm text-gray-500 mt-1">
                            {format(new Date(timestamp as string), 'MMM dd, yyyy hh:mm a')}
                          </p>
                        )}
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        )}

        {/* Cancelled Order */}
        {order.status === 'CANCELLED' && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-6 mb-6">
            <div className="flex items-center">
              <X className="h-6 w-6 text-red-600 mr-2" />
              <div>
                <p className="font-semibold text-red-900">Order Cancelled</p>
                {order.cancelReason && (
                  <p className="text-sm text-red-700 mt-1">Reason: {order.cancelReason}</p>
                )}
                {order.cancelledAt && (
                  <p className="text-sm text-red-600 mt-1">
                    Cancelled on {format(new Date(order.cancelledAt), 'MMM dd, yyyy hh:mm a')}
                  </p>
                )}
              </div>
            </div>
          </div>
        )}

        {/* Delivery Address */}
        <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
            <MapPin className="h-5 w-5 mr-2" />
            Delivery Address
          </h2>
          <div className="text-gray-600">
            <p>{order.deliveryAddress}</p>
            {order.deliverySlot && (
              <p className="mt-2 text-sm">
                <span className="font-medium">Delivery Slot:</span> {order.deliverySlot}
              </p>
            )}
          </div>
          {order.customerNotes && (
            <div className="mt-4 p-3 bg-yellow-50 rounded-lg">
              <p className="text-sm font-medium text-yellow-900">Customer Notes:</p>
              <p className="text-sm text-yellow-800 mt-1">{order.customerNotes}</p>
            </div>
          )}
        </div>

        {/* Order Items */}
        <div className="bg-white rounded-lg shadow-sm p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
            <Package className="h-5 w-5 mr-2" />
            Order Items
          </h2>
          <div className="space-y-4">
            {order.items.map((item) => (
              <div key={item.id} className="flex items-start pb-4 border-b last:border-b-0">
                <div className="w-16 h-16 bg-gray-100 rounded flex items-center justify-center flex-shrink-0">
                  <Package className="h-8 w-8 text-gray-400" />
                </div>
                <div className="ml-4 flex-1">
                  <h3 className="font-medium text-gray-900">{item.productName}</h3>
                  <p className="text-sm text-gray-600">{item.variantName}</p>
                  <p className="text-xs text-gray-500 mt-1">SKU: {item.sku}</p>
                  <p className="text-sm text-gray-600 mt-1">Quantity: {item.quantity}</p>
                </div>
                <div className="text-right">
                  <p className="font-medium text-gray-900">₹{item.totalPrice.toFixed(2)}</p>
                  <p className="text-sm text-gray-600">@₹{item.unitPrice.toFixed(2)} each</p>
                </div>
              </div>
            ))}
          </div>

          {/* Order Summary */}
          <div className="mt-6 pt-6 border-t space-y-2">
            <div className="flex justify-between text-gray-600">
              <span>Subtotal</span>
              <span>₹{order.subtotal.toFixed(2)}</span>
            </div>
            <div className="flex justify-between text-gray-600">
              <span>Delivery Fee</span>
              <span>{order.deliveryFee > 0 ? `₹${order.deliveryFee.toFixed(2)}` : 'FREE'}</span>
            </div>
            <div className="flex justify-between text-lg font-bold text-gray-900 pt-2 border-t">
              <span>Total</span>
              <span>₹{order.totalAmount.toFixed(2)}</span>
            </div>
          </div>
        </div>
      </div>
    </MainLayout>
  );
};

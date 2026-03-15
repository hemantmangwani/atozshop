import React, { useState } from 'react';
import { orderService } from '../services/orderService';
import type { OrderSummaryResponse } from '../types/order';

interface AdminOrderActionsProps {
  order: OrderSummaryResponse;
  userId: number;
  onUpdate: () => void;
}

export const AdminOrderActions: React.FC<AdminOrderActionsProps> = ({ order, userId, onUpdate }) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [showMenu, setShowMenu] = useState(false);

  const handleAction = async (action: () => Promise<void>, actionName: string) => {
    try {
      setLoading(true);
      setError(null);
      await action();
      onUpdate();
      setShowMenu(false);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : `Failed to ${actionName}`;
      setError(errorMessage);
      console.error(`Error ${actionName}:`, err);
      // Show error for 3 seconds
      setTimeout(() => setError(null), 3000);
    } finally {
      setLoading(false);
    }
  };

  const acceptOrder = () => handleAction(
    () => orderService.acceptOrder(order.id, userId),
    'accept order'
  );

  const packOrder = () => handleAction(
    () => orderService.packOrder(order.id, userId),
    'mark as packed'
  );

  const dispatchOrder = () => handleAction(
    () => orderService.dispatchOrder(order.id, userId),
    'mark as dispatched'
  );

  const deliverOrder = () => handleAction(
    () => orderService.deliverOrder(order.id, userId),
    'mark as delivered'
  );

  const getAvailableActions = () => {
    const actions: Array<{ label: string; onClick: () => void; icon: React.ReactNode; color: string }> = [];

    switch (order.status) {
      case 'NEW':
        actions.push({
          label: 'Accept Order',
          onClick: acceptOrder,
          icon: (
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
            </svg>
          ),
          color: 'text-green-600 hover:bg-green-50',
        });
        break;

      case 'ACCEPTED':
        actions.push({
          label: 'Mark as Packed',
          onClick: packOrder,
          icon: (
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
            </svg>
          ),
          color: 'text-indigo-600 hover:bg-indigo-50',
        });
        break;

      case 'PACKED':
        actions.push({
          label: 'Mark as Dispatched',
          onClick: dispatchOrder,
          icon: (
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path d="M9 17a2 2 0 11-4 0 2 2 0 014 0zM19 17a2 2 0 11-4 0 2 2 0 014 0z" />
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16V6a1 1 0 00-1-1H4a1 1 0 00-1 1v10a1 1 0 001 1h1m8-1a1 1 0 01-1 1H9m4-1V8a1 1 0 011-1h2.586a1 1 0 01.707.293l3.414 3.414a1 1 0 01.293.707V16a1 1 0 01-1 1h-1m-6-1a1 1 0 001 1h1M5 17a2 2 0 104 0m-4 0a2 2 0 114 0m6 0a2 2 0 104 0m-4 0a2 2 0 114 0" />
            </svg>
          ),
          color: 'text-yellow-600 hover:bg-yellow-50',
        });
        break;

      case 'OUT_FOR_DELIVERY':
        actions.push({
          label: 'Mark as Delivered',
          onClick: deliverOrder,
          icon: (
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          ),
          color: 'text-green-600 hover:bg-green-50',
        });
        break;
    }

    return actions;
  };

  const availableActions = getAvailableActions();

  if (availableActions.length === 0) {
    return (
      <span className="text-gray-400 text-xs">
        {order.status === 'DELIVERED' ? 'Completed' : order.status === 'CANCELLED' ? 'Cancelled' : 'No actions'}
      </span>
    );
  }

  if (availableActions.length === 1) {
    const action = availableActions[0];
    return (
      <div className="relative">
        <button
          onClick={action.onClick}
          disabled={loading}
          className={`flex items-center gap-1 px-3 py-1.5 text-sm font-medium rounded-lg transition-colors ${action.color} disabled:opacity-50 disabled:cursor-not-allowed`}
          title={action.label}
        >
          {loading ? (
            <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-current"></div>
          ) : (
            <>
              {action.icon}
              <span className="hidden lg:inline">{action.label}</span>
            </>
          )}
        </button>
        {error && (
          <div className="absolute top-full right-0 mt-1 bg-red-50 border border-red-200 text-red-700 px-3 py-2 rounded-lg shadow-lg text-xs whitespace-nowrap z-10">
            {error}
          </div>
        )}
      </div>
    );
  }

  return (
    <div className="relative">
      <button
        onClick={() => setShowMenu(!showMenu)}
        className="flex items-center gap-1 px-3 py-1.5 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50"
        title="More actions"
      >
        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 5v.01M12 12v.01M12 19v.01M12 6a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2z" />
        </svg>
      </button>

      {showMenu && (
        <>
          <div
            className="fixed inset-0 z-10"
            onClick={() => setShowMenu(false)}
          />
          <div className="absolute right-0 mt-1 w-48 bg-white border border-gray-200 rounded-lg shadow-lg z-20">
            {availableActions.map((action, index) => (
              <button
                key={index}
                onClick={action.onClick}
                disabled={loading}
                className={`w-full flex items-center gap-2 px-4 py-2 text-sm ${action.color} disabled:opacity-50 disabled:cursor-not-allowed first:rounded-t-lg last:rounded-b-lg`}
              >
                {loading ? (
                  <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-current"></div>
                ) : (
                  action.icon
                )}
                {action.label}
              </button>
            ))}
          </div>
        </>
      )}

      {error && (
        <div className="absolute top-full right-0 mt-1 bg-red-50 border border-red-200 text-red-700 px-3 py-2 rounded-lg shadow-lg text-xs whitespace-nowrap z-10">
          {error}
        </div>
      )}
    </div>
  );
};

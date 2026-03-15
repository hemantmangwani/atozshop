import { Link, useNavigate } from 'react-router-dom';
import { MainLayout } from '../../components/layout/MainLayout';
import { useCart } from '../../context/CartContext';
import { Trash2, Plus, Minus, ShoppingBag, ArrowLeft } from 'lucide-react';
import { useState } from 'react';

export const CartPage = () => {
  const { cart, updateQuantity, removeFromCart } = useCart();
  const navigate = useNavigate();
  const [updatingItems, setUpdatingItems] = useState<Set<number>>(new Set());

  const handleUpdateQuantity = async (variantId: number, newQuantity: number) => {
    setUpdatingItems((prev) => new Set(prev).add(variantId));
    try {
      await updateQuantity(variantId, newQuantity);
    } catch (error: any) {
      alert(error.message || 'Failed to update quantity');
    } finally {
      setUpdatingItems((prev) => {
        const newSet = new Set(prev);
        newSet.delete(variantId);
        return newSet;
      });
    }
  };

  const handleRemoveItem = (variantId: number) => {
    if (confirm('Remove this item from cart?')) {
      removeFromCart(variantId);
    }
  };

  if (cart.items.length === 0) {
    return (
      <MainLayout>
        <div className="max-w-4xl mx-auto">
          <div className="text-center py-16">
            <ShoppingBag className="h-24 w-24 text-gray-300 mx-auto mb-4" />
            <h2 className="text-2xl font-bold text-gray-900 mb-2">Your cart is empty</h2>
            <p className="text-gray-600 mb-6">Add some products to get started!</p>
            <Link
              to="/"
              className="inline-flex items-center space-x-2 bg-primary-600 text-white px-6 py-3 rounded-lg hover:bg-primary-700 transition-colors"
            >
              <ArrowLeft className="h-5 w-5" />
              <span>Continue Shopping</span>
            </Link>
          </div>
        </div>
      </MainLayout>
    );
  }

  return (
    <MainLayout>
      <div className="max-w-6xl mx-auto">
        <div className="flex items-center justify-between mb-6">
          <h1 className="text-3xl font-bold text-gray-900">Shopping Cart</h1>
          <Link
            to="/"
            className="text-primary-600 hover:text-primary-700 flex items-center space-x-1"
          >
            <ArrowLeft className="h-4 w-4" />
            <span>Continue Shopping</span>
          </Link>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Cart Items */}
          <div className="lg:col-span-2 space-y-4">
            {cart.items.map((item) => {
              const isUpdating = updatingItems.has(item.variantId);

              return (
                <div
                  key={item.variantId}
                  className="bg-white rounded-lg shadow-sm p-4 flex flex-col sm:flex-row gap-4"
                >
                  {/* Product Image Placeholder */}
                  <div className="w-24 h-24 bg-gray-100 rounded-lg flex items-center justify-center flex-shrink-0">
                    <ShoppingBag className="h-10 w-10 text-gray-400" />
                  </div>

                  {/* Product Info */}
                  <div className="flex-1">
                    <h3 className="font-semibold text-gray-900 mb-1">
                      {item.productName}
                    </h3>
                    <p className="text-sm text-gray-600 mb-2">{item.variantName}</p>
                    <p className="text-xs text-gray-500">SKU: {item.sku}</p>

                    {/* Price */}
                    <div className="flex items-baseline space-x-2 mt-2">
                      <span className="text-lg font-bold text-gray-900">
                        ₹{item.unitPrice.toFixed(2)}
                      </span>
                      {item.mrp > item.unitPrice && (
                        <span className="text-sm text-gray-500 line-through">
                          ₹{item.mrp.toFixed(2)}
                        </span>
                      )}
                    </div>

                    {/* Stock Info */}
                    {item.availableStock <= 5 && (
                      <p className="text-xs text-yellow-600 mt-1">
                        Only {item.availableStock} left in stock
                      </p>
                    )}
                  </div>

                  {/* Quantity Controls */}
                  <div className="flex flex-col items-end justify-between">
                    <button
                      onClick={() => handleRemoveItem(item.variantId)}
                      className="text-red-600 hover:text-red-700 p-1"
                      title="Remove item"
                    >
                      <Trash2 className="h-5 w-5" />
                    </button>

                    <div className="flex items-center space-x-2">
                      <button
                        onClick={() => handleUpdateQuantity(item.variantId, item.quantity - 1)}
                        disabled={isUpdating || item.quantity <= 1}
                        className="p-1 rounded border border-gray-300 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                      >
                        <Minus className="h-4 w-4" />
                      </button>

                      <span className="w-12 text-center font-semibold">
                        {isUpdating ? '...' : item.quantity}
                      </span>

                      <button
                        onClick={() => handleUpdateQuantity(item.variantId, item.quantity + 1)}
                        disabled={isUpdating || item.quantity >= item.availableStock}
                        className="p-1 rounded border border-gray-300 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                      >
                        <Plus className="h-4 w-4" />
                      </button>
                    </div>

                    {/* Item Total */}
                    <div className="text-right mt-2">
                      <p className="text-sm text-gray-600">Total</p>
                      <p className="text-lg font-bold text-gray-900">
                        ₹{item.totalPrice.toFixed(2)}
                      </p>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>

          {/* Order Summary */}
          <div className="lg:col-span-1">
            <div className="bg-white rounded-lg shadow-sm p-6 sticky top-20">
              <h2 className="text-xl font-bold text-gray-900 mb-4">Order Summary</h2>

              <div className="space-y-3 mb-6">
                <div className="flex justify-between text-gray-600">
                  <span>Items ({cart.totalItems})</span>
                  <span>{cart.totalQuantity} units</span>
                </div>

                <div className="flex justify-between text-gray-600">
                  <span>Subtotal</span>
                  <span>₹{cart.subtotal.toFixed(2)}</span>
                </div>

                <div className="flex justify-between text-gray-600">
                  <span>Delivery</span>
                  <span className="text-green-600">FREE</span>
                </div>

                <div className="border-t pt-3">
                  <div className="flex justify-between items-baseline">
                    <span className="text-lg font-bold text-gray-900">Total</span>
                    <span className="text-2xl font-bold text-gray-900">
                      ₹{cart.subtotal.toFixed(2)}
                    </span>
                  </div>
                </div>
              </div>

              <button
                onClick={() => navigate('/checkout')}
                className="w-full bg-primary-600 text-white py-3 px-4 rounded-lg hover:bg-primary-700 transition-colors font-semibold"
              >
                Proceed to Checkout
              </button>

              <p className="text-xs text-gray-500 mt-4 text-center">
                Taxes calculated at checkout
              </p>
            </div>
          </div>
        </div>
      </div>
    </MainLayout>
  );
};

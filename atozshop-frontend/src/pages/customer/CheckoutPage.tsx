import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { MainLayout } from '../../components/layout/MainLayout';
import { useCart } from '../../context/CartContext';
import { useAuth } from '../../context/AuthContext';
import { addressService } from '../../services/addressService';
import { orderService } from '../../services/orderService';
import { authService } from '../../services/authService';
import { customerService } from '../../services/customerService';
import type { PaymentMethod } from '../../types/address';
import type { CreateOrderRequest } from '../../types/order';
import { MapPin, Plus, Check, CreditCard, Wallet, Banknote } from 'lucide-react';

export const CheckoutPage = () => {
  const { cart, clearCart } = useCart();
  const { user } = useAuth();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const [selectedAddressId, setSelectedAddressId] = useState<number | null>(null);
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>('COD');
  const [deliverySlot, setDeliverySlot] = useState('9 AM - 12 PM');
  const [customerNotes, setCustomerNotes] = useState('');
  const [showAddressForm, setShowAddressForm] = useState(false);

  // Address form state
  const [newAddress, setNewAddress] = useState({
    addressLine1: '',
    addressLine2: '',
    city: '',
    state: '',
    postalCode: '',
    phone: '',
    landmark: '',
  });

  // Fetch addresses
  const { data: addresses } = useQuery({
    queryKey: ['addresses', user?.customerId],
    queryFn: () => addressService.getCustomerAddresses(user!.customerId!),
    enabled: !!user && !!user.customerId,
  });

  // Add address mutation
  const addAddressMutation = useMutation({
    mutationFn: addressService.addAddress,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['addresses'] });
      setShowAddressForm(false);
      setNewAddress({
        addressLine1: '',
        addressLine2: '',
        city: '',
        state: '',
        postalCode: '',
        phone: '',
        landmark: '',
      });
    },
  });

  // Place order mutation
  const placeOrderMutation = useMutation({
    mutationFn: orderService.placeOrder,
    onSuccess: (data) => {
      clearCart();
      navigate(`/orders/${data.id}`);
    },
  });

  const handleAddAddress = async (e: React.FormEvent) => {
    e.preventDefault();
    console.log('Handle Add Address - User:', user);
    console.log('Handle Add Address - Customer ID:', user?.customerId);

    if (!user) {
      console.error('No user found');
      alert('Please log in to add an address');
      return;
    }

    if (!user.customerId) {
      console.error('No customer ID found for user');
      alert('Please log out and log back in to refresh your session');
      return;
    }

    try {
      await addAddressMutation.mutateAsync({
        customerId: user.customerId,
        ...newAddress,
        isDefault: addresses?.length === 0,
      });
    } catch (error: any) {
      console.error('Error adding address:', error);
      alert(error?.message || 'Failed to add address');
    }
  };

  const handlePlaceOrder = async () => {
    if (!selectedAddressId || !user) {
      alert('Please select a delivery address');
      return;
    }

    if (cart.items.length === 0) {
      alert('Your cart is empty');
      return;
    }

    try {
      // Get the correct customer ID for the logged-in user
      const customerId = await customerService.getCurrentUserCustomerId();

      const request: CreateOrderRequest = {
        tenantId: authService.getTenantId(),
        storeId: authService.getStoreId(),
        customerId: customerId,
        deliveryAddressId: selectedAddressId,
        deliverySlot,
        paymentMethod,
        items: cart.items.map((item) => ({
          variantId: item.variantId,
          quantity: item.quantity,
        })),
        customerNotes: customerNotes || undefined,
      };

      await placeOrderMutation.mutateAsync(request);
    } catch (error: any) {
      alert(error.message || 'Failed to place order');
    }
  };

  if (cart.items.length === 0) {
    navigate('/cart');
    return null;
  }

  const deliveryFee = 0;
  const total = cart.subtotal + deliveryFee;

  return (
    <MainLayout>
      <div className="max-w-6xl mx-auto">
        <h1 className="text-3xl font-bold text-gray-900 mb-6">Checkout</h1>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Main Content */}
          <div className="lg:col-span-2 space-y-6">
            {/* Delivery Address */}
            <div className="bg-white rounded-lg shadow-sm p-6">
              <div className="flex items-center justify-between mb-4">
                <h2 className="text-xl font-semibold text-gray-900 flex items-center">
                  <MapPin className="h-5 w-5 mr-2" />
                  Delivery Address
                </h2>
                <button
                  onClick={() => setShowAddressForm(!showAddressForm)}
                  className="text-primary-600 hover:text-primary-700 text-sm flex items-center"
                >
                  <Plus className="h-4 w-4 mr-1" />
                  Add New
                </button>
              </div>

              {/* Add Address Form */}
              {showAddressForm && (
                <form onSubmit={handleAddAddress} className="mb-4 p-4 bg-gray-50 rounded-lg">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <input
                      type="text"
                      placeholder="Address Line 1 *"
                      value={newAddress.addressLine1}
                      onChange={(e) =>
                        setNewAddress({ ...newAddress, addressLine1: e.target.value })
                      }
                      required
                      className="col-span-2 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
                    />
                    <input
                      type="text"
                      placeholder="Address Line 2"
                      value={newAddress.addressLine2}
                      onChange={(e) =>
                        setNewAddress({ ...newAddress, addressLine2: e.target.value })
                      }
                      className="col-span-2 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
                    />
                    <input
                      type="text"
                      placeholder="Landmark"
                      value={newAddress.landmark}
                      onChange={(e) =>
                        setNewAddress({ ...newAddress, landmark: e.target.value })
                      }
                      className="col-span-2 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
                    />
                    <input
                      type="text"
                      placeholder="City *"
                      value={newAddress.city}
                      onChange={(e) => setNewAddress({ ...newAddress, city: e.target.value })}
                      required
                      className="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
                    />
                    <input
                      type="text"
                      placeholder="State *"
                      value={newAddress.state}
                      onChange={(e) => setNewAddress({ ...newAddress, state: e.target.value })}
                      required
                      className="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
                    />
                    <input
                      type="text"
                      placeholder="Postal Code *"
                      value={newAddress.postalCode}
                      onChange={(e) =>
                        setNewAddress({ ...newAddress, postalCode: e.target.value })
                      }
                      required
                      className="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
                    />
                    <input
                      type="tel"
                      placeholder="Phone *"
                      value={newAddress.phone}
                      onChange={(e) => setNewAddress({ ...newAddress, phone: e.target.value })}
                      required
                      className="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
                    />
                  </div>
                  <div className="flex space-x-2 mt-4">
                    <button
                      type="submit"
                      disabled={addAddressMutation.isPending}
                      className="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 disabled:opacity-50"
                    >
                      {addAddressMutation.isPending ? 'Saving...' : 'Save Address'}
                    </button>
                    <button
                      type="button"
                      onClick={() => setShowAddressForm(false)}
                      className="px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300"
                    >
                      Cancel
                    </button>
                  </div>
                </form>
              )}

              {/* Address List */}
              <div className="space-y-3">
                {addresses?.map((address) => (
                  <div
                    key={address.id}
                    onClick={() => setSelectedAddressId(address.id)}
                    className={`
                      p-4 border-2 rounded-lg cursor-pointer transition-all
                      ${
                        selectedAddressId === address.id
                          ? 'border-primary-600 bg-primary-50'
                          : 'border-gray-200 hover:border-gray-300'
                      }
                    `}
                  >
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <p className="font-medium text-gray-900">{address.addressLine1}</p>
                        {address.addressLine2 && (
                          <p className="text-sm text-gray-600">{address.addressLine2}</p>
                        )}
                        {address.landmark && (
                          <p className="text-sm text-gray-600">Landmark: {address.landmark}</p>
                        )}
                        <p className="text-sm text-gray-600">
                          {address.city}, {address.state} - {address.postalCode}
                        </p>
                        <p className="text-sm text-gray-600">Phone: {address.phone}</p>
                        {address.isDefault && (
                          <span className="inline-block mt-2 text-xs bg-green-100 text-green-800 px-2 py-1 rounded">
                            Default
                          </span>
                        )}
                      </div>
                      {selectedAddressId === address.id && (
                        <Check className="h-6 w-6 text-primary-600 flex-shrink-0" />
                      )}
                    </div>
                  </div>
                ))}

                {addresses?.length === 0 && !showAddressForm && (
                  <p className="text-gray-500 text-center py-4">
                    No saved addresses. Please add a new address.
                  </p>
                )}
              </div>
            </div>

            {/* Delivery Slot */}
            <div className="bg-white rounded-lg shadow-sm p-6">
              <h2 className="text-xl font-semibold text-gray-900 mb-4">Delivery Slot</h2>
              <div className="grid grid-cols-2 md:grid-cols-3 gap-3">
                {['9 AM - 12 PM', '12 PM - 3 PM', '3 PM - 6 PM', '6 PM - 9 PM'].map(
                  (slot) => (
                    <button
                      key={slot}
                      onClick={() => setDeliverySlot(slot)}
                      className={`
                        p-3 rounded-lg border-2 text-sm font-medium transition-all
                        ${
                          deliverySlot === slot
                            ? 'border-primary-600 bg-primary-50 text-primary-700'
                            : 'border-gray-200 text-gray-700 hover:border-gray-300'
                        }
                      `}
                    >
                      {slot}
                    </button>
                  )
                )}
              </div>
            </div>

            {/* Payment Method */}
            <div className="bg-white rounded-lg shadow-sm p-6">
              <h2 className="text-xl font-semibold text-gray-900 mb-4 flex items-center">
                <CreditCard className="h-5 w-5 mr-2" />
                Payment Method
              </h2>
              <div className="space-y-3">
                {[
                  { value: 'COD', label: 'Cash on Delivery', icon: Banknote },
                  { value: 'UPI', label: 'UPI', icon: Wallet },
                  { value: 'ONLINE', label: 'Online Payment', icon: CreditCard },
                ].map((method) => {
                  const Icon = method.icon;
                  return (
                    <button
                      key={method.value}
                      onClick={() => setPaymentMethod(method.value as PaymentMethod)}
                      className={`
                        w-full p-4 rounded-lg border-2 text-left transition-all flex items-center
                        ${
                          paymentMethod === method.value
                            ? 'border-primary-600 bg-primary-50'
                            : 'border-gray-200 hover:border-gray-300'
                        }
                      `}
                    >
                      <Icon className="h-6 w-6 mr-3 text-gray-600" />
                      <span className="font-medium">{method.label}</span>
                      {paymentMethod === method.value && (
                        <Check className="h-5 w-5 ml-auto text-primary-600" />
                      )}
                    </button>
                  );
                })}
              </div>
            </div>

            {/* Order Notes */}
            <div className="bg-white rounded-lg shadow-sm p-6">
              <h2 className="text-xl font-semibold text-gray-900 mb-4">
                Order Notes (Optional)
              </h2>
              <textarea
                value={customerNotes}
                onChange={(e) => setCustomerNotes(e.target.value)}
                placeholder="Special instructions for delivery..."
                rows={3}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
              />
            </div>
          </div>

          {/* Order Summary */}
          <div className="lg:col-span-1">
            <div className="bg-white rounded-lg shadow-sm p-6 sticky top-20">
              <h2 className="text-xl font-semibold text-gray-900 mb-4">Order Summary</h2>

              <div className="space-y-3 mb-6">
                <div className="flex justify-between text-gray-600">
                  <span>Items ({cart.totalItems})</span>
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
                      ₹{total.toFixed(2)}
                    </span>
                  </div>
                </div>
              </div>

              <button
                onClick={handlePlaceOrder}
                disabled={!selectedAddressId || placeOrderMutation.isPending}
                className="w-full bg-primary-600 text-white py-3 px-4 rounded-lg hover:bg-primary-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors font-semibold"
              >
                {placeOrderMutation.isPending ? 'Placing Order...' : 'Place Order'}
              </button>
            </div>
          </div>
        </div>
      </div>
    </MainLayout>
  );
};

import { useState, useEffect } from 'react';
import { MainLayout } from '../../../components/layout/MainLayout';
import { billService, CreateBillRequest, Bill, BillItem as BillItemType } from '../../../services/billService';
import { productService } from '../../../services/productService';
import { customerService, Customer } from '../../../services/customerService';
import { discountService, Discount } from '../../../services/discountService';
import { Search, ShoppingCart, Plus, Minus, Trash2, User, DollarSign, Printer, X } from 'lucide-react';
import toast from 'react-hot-toast';

interface CartItem {
  variantId: number;
  sku: string;
  productName: string;
  variantName: string;
  quantity: number;
  unitPrice: number;
  mrp: number;
  availableStock: number;
  subtotal: number;
}

export const POSBillingPage = () => {
  const [searchQuery, setSearchQuery] = useState('');
  const [products, setProducts] = useState<any[]>([]);
  const [cart, setCart] = useState<CartItem[]>([]);
  const [customer, setCustomer] = useState<Customer | null>(null);
  const [showCustomerSearch, setShowCustomerSearch] = useState(false);
  const [customerSearch, setCustomerSearch] = useState('');
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [showPaymentModal, setShowPaymentModal] = useState(false);
  const [currentBill, setCurrentBill] = useState<Bill | null>(null);
  const [loading, setLoading] = useState(false);

  // Search products
  const handleProductSearch = async (query: string) => {
    if (query.length < 2) {
      setProducts([]);
      return;
    }

    try {
      const results = await productService.searchProducts(query);
      setProducts(results);
    } catch (error) {
      console.error('Search error:', error);
    }
  };

  // Add item to cart
  const addToCart = async (variant: any) => {
    try {
      // Check stock
      const stockInfo = await productService.checkStockAvailability(variant.id);

      if (stockInfo.availableStock < 1) {
        toast.error('Out of stock!');
        return;
      }

      const existingItem = cart.find(item => item.variantId === variant.id);

      if (existingItem) {
        if (existingItem.quantity >= stockInfo.availableStock) {
          toast.error(`Only ${stockInfo.availableStock} units available`);
          return;
        }
        updateQuantity(variant.id, existingItem.quantity + 1);
      } else {
        const newItem: CartItem = {
          variantId: variant.id,
          sku: variant.sku,
          productName: variant.productName,
          variantName: variant.variantName || '',
          quantity: 1,
          unitPrice: stockInfo.sellingPrice,
          mrp: variant.mrp || stockInfo.sellingPrice,
          availableStock: stockInfo.availableStock,
          subtotal: stockInfo.sellingPrice,
        };
        setCart([...cart, newItem]);
        toast.success('Added to cart');
      }

      setSearchQuery('');
      setProducts([]);
    } catch (error) {
      toast.error('Failed to add item');
    }
  };

  // Update quantity
  const updateQuantity = (variantId: number, newQuantity: number) => {
    if (newQuantity < 1) {
      removeFromCart(variantId);
      return;
    }

    setCart(cart.map(item => {
      if (item.variantId === variantId) {
        if (newQuantity > item.availableStock) {
          toast.error(`Only ${item.availableStock} units available`);
          return item;
        }
        return {
          ...item,
          quantity: newQuantity,
          subtotal: item.unitPrice * newQuantity,
        };
      }
      return item;
    }));
  };

  // Remove from cart
  const removeFromCart = (variantId: number) => {
    setCart(cart.filter(item => item.variantId !== variantId));
  };

  // Calculate totals
  const calculateTotals = () => {
    const subtotal = cart.reduce((sum, item) => sum + item.subtotal, 0);
    const taxAmount = 0; // Can add tax calculation here
    const total = subtotal + taxAmount;

    return { subtotal, taxAmount, total };
  };

  // Search customers
  const handleCustomerSearch = async (query: string) => {
    if (query.length < 2) {
      setCustomers([]);
      return;
    }

    try {
      const results = await customerService.searchCustomers(query);
      setCustomers(results);
    } catch (error) {
      console.error('Customer search error:', error);
    }
  };

  // Select customer
  const selectCustomer = (cust: Customer) => {
    setCustomer(cust);
    setShowCustomerSearch(false);
    setCustomerSearch('');
    setCustomers([]);
    toast.success(`Customer: ${cust.name}`);
  };

  // Clear cart
  const clearCart = () => {
    setCart([]);
    setCustomer(null);
    setCurrentBill(null);
  };

  // Create bill
  const handleCheckout = async () => {
    if (cart.length === 0) {
      toast.error('Cart is empty');
      return;
    }

    setLoading(true);
    try {
      const billData: CreateBillRequest = {
        tenantId: 1,
        storeId: 1,
        customerId: customer?.id,
        cashierId: 1, // Will be replaced with actual user ID
        billType: 'SALES',
        items: cart.map(item => ({
          variantId: item.variantId,
          quantity: item.quantity,
        })),
      };

      const bill = await billService.createBill(billData);
      setCurrentBill(bill);
      setShowPaymentModal(true);
      toast.success(`Bill created: ${bill.billNumber}`);
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to create bill');
    } finally {
      setLoading(false);
    }
  };

  const totals = calculateTotals();

  return (
    <MainLayout>
      <div className="h-screen flex flex-col bg-gray-50">
        {/* Header */}
        <div className="bg-white border-b px-6 py-4">
          <div className="flex items-center justify-between">
            <h1 className="text-2xl font-bold text-gray-900">POS Billing</h1>
            <div className="flex items-center gap-4">
              {customer ? (
                <div className="flex items-center gap-2 bg-primary-50 px-4 py-2 rounded-lg">
                  <User className="h-5 w-5 text-primary-600" />
                  <div>
                    <p className="text-sm font-medium text-primary-900">{customer.name}</p>
                    <p className="text-xs text-primary-600">{customer.phone}</p>
                  </div>
                  <button
                    onClick={() => setCustomer(null)}
                    className="ml-2 text-primary-600 hover:text-primary-800"
                  >
                    <X className="h-4 w-4" />
                  </button>
                </div>
              ) : (
                <button
                  onClick={() => setShowCustomerSearch(true)}
                  className="flex items-center gap-2 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50"
                >
                  <User className="h-5 w-5" />
                  Add Customer
                </button>
              )}
            </div>
          </div>
        </div>

        <div className="flex-1 flex overflow-hidden">
          {/* Left - Product Search & Selection */}
          <div className="flex-1 flex flex-col p-6 overflow-hidden">
            {/* Search Bar */}
            <div className="relative mb-6">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
              <input
                type="text"
                placeholder="Search products by name, SKU, or barcode..."
                value={searchQuery}
                onChange={(e) => {
                  setSearchQuery(e.target.value);
                  handleProductSearch(e.target.value);
                }}
                className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 text-lg"
                autoFocus
              />
            </div>

            {/* Product Results */}
            {products.length > 0 && (
              <div className="bg-white rounded-lg shadow-lg border border-gray-200 overflow-y-auto max-h-96">
                {products.map((product) =>
                  product.variants?.map((variant: any) => (
                    <button
                      key={variant.id}
                      onClick={() => addToCart(variant)}
                      className="w-full p-4 border-b hover:bg-primary-50 text-left transition-colors"
                    >
                      <div className="flex items-center justify-between">
                        <div>
                          <p className="font-medium text-gray-900">{product.name}</p>
                          <p className="text-sm text-gray-600">
                            {variant.variantName} • SKU: {variant.sku}
                          </p>
                        </div>
                        <div className="text-right">
                          <p className="text-lg font-bold text-primary-600">
                            ₹{variant.sellingPrice || 0}
                          </p>
                          <p className="text-sm text-gray-500">
                            Stock: {variant.currentStock || 0}
                          </p>
                        </div>
                      </div>
                    </button>
                  ))
                )}
              </div>
            )}

            {/* Instructions when empty */}
            {searchQuery === '' && products.length === 0 && (
              <div className="flex-1 flex items-center justify-center">
                <div className="text-center text-gray-400">
                  <ShoppingCart className="h-24 w-24 mx-auto mb-4 opacity-50" />
                  <p className="text-lg">Start typing to search products</p>
                  <p className="text-sm mt-2">Search by name, SKU, or scan barcode</p>
                </div>
              </div>
            )}
          </div>

          {/* Right - Cart & Checkout */}
          <div className="w-[450px] bg-white border-l flex flex-col">
            {/* Cart Header */}
            <div className="p-6 border-b">
              <div className="flex items-center justify-between mb-2">
                <h2 className="text-xl font-bold text-gray-900">Current Bill</h2>
                {cart.length > 0 && (
                  <button
                    onClick={clearCart}
                    className="text-sm text-red-600 hover:text-red-800"
                  >
                    Clear All
                  </button>
                )}
              </div>
              <p className="text-sm text-gray-600">{cart.length} items</p>
            </div>

            {/* Cart Items */}
            <div className="flex-1 overflow-y-auto p-6 space-y-3">
              {cart.length === 0 ? (
                <div className="flex flex-col items-center justify-center h-full text-gray-400">
                  <ShoppingCart className="h-16 w-16 mb-2" />
                  <p>Cart is empty</p>
                </div>
              ) : (
                cart.map((item) => (
                  <div
                    key={item.variantId}
                    className="bg-gray-50 rounded-lg p-4"
                  >
                    <div className="flex items-start justify-between mb-2">
                      <div className="flex-1">
                        <p className="font-medium text-gray-900">{item.productName}</p>
                        <p className="text-sm text-gray-600">{item.variantName}</p>
                        <p className="text-xs text-gray-500 mt-1">SKU: {item.sku}</p>
                      </div>
                      <button
                        onClick={() => removeFromCart(item.variantId)}
                        className="text-red-600 hover:text-red-800"
                      >
                        <Trash2 className="h-4 w-4" />
                      </button>
                    </div>

                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-2">
                        <button
                          onClick={() => updateQuantity(item.variantId, item.quantity - 1)}
                          className="p-1 rounded bg-gray-200 hover:bg-gray-300"
                        >
                          <Minus className="h-4 w-4" />
                        </button>
                        <span className="w-12 text-center font-medium">{item.quantity}</span>
                        <button
                          onClick={() => updateQuantity(item.variantId, item.quantity + 1)}
                          className="p-1 rounded bg-gray-200 hover:bg-gray-300"
                        >
                          <Plus className="h-4 w-4" />
                        </button>
                      </div>

                      <div className="text-right">
                        <p className="text-lg font-bold text-primary-600">
                          ₹{item.subtotal.toFixed(2)}
                        </p>
                        <p className="text-xs text-gray-500">
                          @ ₹{item.unitPrice.toFixed(2)}
                        </p>
                      </div>
                    </div>
                  </div>
                ))
              )}
            </div>

            {/* Totals & Checkout */}
            {cart.length > 0 && (
              <div className="border-t p-6 space-y-4">
                {/* Totals */}
                <div className="space-y-2">
                  <div className="flex justify-between text-gray-600">
                    <span>Subtotal:</span>
                    <span>₹{totals.subtotal.toFixed(2)}</span>
                  </div>
                  {totals.taxAmount > 0 && (
                    <div className="flex justify-between text-gray-600">
                      <span>Tax:</span>
                      <span>₹{totals.taxAmount.toFixed(2)}</span>
                    </div>
                  )}
                  <div className="flex justify-between text-2xl font-bold text-gray-900 pt-2 border-t">
                    <span>Total:</span>
                    <span>₹{totals.total.toFixed(2)}</span>
                  </div>
                </div>

                {/* Checkout Button */}
                <button
                  onClick={handleCheckout}
                  disabled={loading}
                  className="w-full bg-primary-600 text-white py-4 rounded-lg font-semibold text-lg hover:bg-primary-700 disabled:bg-gray-400 flex items-center justify-center gap-2"
                >
                  {loading ? (
                    'Processing...'
                  ) : (
                    <>
                      <DollarSign className="h-6 w-6" />
                      Checkout
                    </>
                  )}
                </button>
              </div>
            )}
          </div>
        </div>

        {/* Customer Search Modal */}
        {showCustomerSearch && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg p-6 w-full max-w-md">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-bold">Search Customer</h3>
                <button
                  onClick={() => {
                    setShowCustomerSearch(false);
                    setCustomerSearch('');
                    setCustomers([]);
                  }}
                  className="text-gray-400 hover:text-gray-600"
                >
                  <X className="h-6 w-6" />
                </button>
              </div>

              <input
                type="text"
                placeholder="Search by name or phone..."
                value={customerSearch}
                onChange={(e) => {
                  setCustomerSearch(e.target.value);
                  handleCustomerSearch(e.target.value);
                }}
                className="w-full px-4 py-2 border rounded-lg mb-4 focus:outline-none focus:ring-2 focus:ring-primary-500"
                autoFocus
              />

              <div className="max-h-64 overflow-y-auto">
                {customers.map((cust) => (
                  <button
                    key={cust.id}
                    onClick={() => selectCustomer(cust)}
                    className="w-full p-3 border-b hover:bg-gray-50 text-left"
                  >
                    <p className="font-medium">{cust.name}</p>
                    <p className="text-sm text-gray-600">{cust.phone}</p>
                    <p className="text-xs text-gray-500">{cust.customerCode}</p>
                  </button>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* Payment Modal */}
        {showPaymentModal && currentBill && (
          <PaymentModal
            bill={currentBill}
            onClose={() => {
              setShowPaymentModal(false);
              clearCart();
            }}
          />
        )}
      </div>
    </MainLayout>
  );
};

// Payment Modal Component
const PaymentModal = ({ bill, onClose }: { bill: Bill; onClose: () => void }) => {
  const [paymentMethod, setPaymentMethod] = useState<'CASH' | 'CARD' | 'UPI'>('CASH');
  const [amountReceived, setAmountReceived] = useState(bill.totalAmount.toString());
  const [processing, setProcessing] = useState(false);

  const handlePayment = async () => {
    setProcessing(true);
    try {
      // Process payment
      await billService.processPayment({
        billId: bill.id,
        tenantId: bill.tenantId,
        paymentMethod,
        amount: parseFloat(amountReceived),
        paymentDate: new Date().toISOString(),
      });

      // Confirm bill (deduct stock)
      await billService.confirmBill(bill.id);

      toast.success('Payment successful! Bill confirmed.');
      onClose();
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Payment failed');
    } finally {
      setProcessing(false);
    }
  };

  const changeAmount = parseFloat(amountReceived) - bill.totalAmount;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg p-8 w-full max-w-md">
        <h3 className="text-2xl font-bold mb-6">Process Payment</h3>

        {/* Bill Summary */}
        <div className="bg-gray-50 rounded-lg p-4 mb-6">
          <p className="text-sm text-gray-600">Bill Number</p>
          <p className="text-lg font-bold">{bill.billNumber}</p>
          <p className="text-3xl font-bold text-primary-600 mt-2">
            ₹{bill.totalAmount.toFixed(2)}
          </p>
        </div>

        {/* Payment Method */}
        <div className="mb-6">
          <label className="block text-sm font-medium mb-2">Payment Method</label>
          <div className="grid grid-cols-3 gap-2">
            {(['CASH', 'CARD', 'UPI'] as const).map((method) => (
              <button
                key={method}
                onClick={() => setPaymentMethod(method)}
                className={`px-4 py-3 border rounded-lg font-medium ${
                  paymentMethod === method
                    ? 'bg-primary-600 text-white border-primary-600'
                    : 'bg-white text-gray-700 border-gray-300 hover:bg-gray-50'
                }`}
              >
                {method}
              </button>
            ))}
          </div>
        </div>

        {/* Amount Received */}
        <div className="mb-6">
          <label className="block text-sm font-medium mb-2">Amount Received</label>
          <input
            type="number"
            value={amountReceived}
            onChange={(e) => setAmountReceived(e.target.value)}
            className="w-full px-4 py-3 border rounded-lg text-lg font-bold focus:outline-none focus:ring-2 focus:ring-primary-500"
            step="0.01"
          />
        </div>

        {/* Change */}
        {changeAmount >= 0 && (
          <div className="bg-green-50 rounded-lg p-4 mb-6">
            <p className="text-sm text-green-700">Change to Return</p>
            <p className="text-2xl font-bold text-green-700">
              ₹{changeAmount.toFixed(2)}
            </p>
          </div>
        )}

        {/* Actions */}
        <div className="flex gap-3">
          <button
            onClick={onClose}
            className="flex-1 px-6 py-3 border border-gray-300 rounded-lg font-medium hover:bg-gray-50"
          >
            Cancel
          </button>
          <button
            onClick={handlePayment}
            disabled={processing || parseFloat(amountReceived) < bill.totalAmount}
            className="flex-1 px-6 py-3 bg-primary-600 text-white rounded-lg font-medium hover:bg-primary-700 disabled:bg-gray-400"
          >
            {processing ? 'Processing...' : 'Confirm Payment'}
          </button>
        </div>
      </div>
    </div>
  );
};

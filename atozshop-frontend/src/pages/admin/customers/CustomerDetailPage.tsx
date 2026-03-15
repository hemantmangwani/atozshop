import { useQuery } from '@tanstack/react-query';
import { useParams, Link } from 'react-router-dom';
import { MainLayout } from '../../../components/layout/MainLayout';
import { customerService } from '../../../services/customerService';
import { billService } from '../../../services/billService';
import {
  User,
  ArrowLeft,
  Edit,
  Phone,
  Mail,
  MapPin,
  CreditCard,
  Calendar,
  ShoppingBag,
  Star,
  Receipt,
  Eye,
} from 'lucide-react';

export const CustomerDetailPage = () => {
  const { id } = useParams<{ id: string }>();

  const { data: customer, isLoading: customerLoading } = useQuery({
    queryKey: ['customer', id],
    queryFn: () => customerService.getCustomerById(Number(id)),
    enabled: !!id,
  });

  const { data: purchaseHistory, isLoading: historyLoading } = useQuery({
    queryKey: ['customer-purchases', id],
    queryFn: () => customerService.getPurchaseHistory(Number(id)),
    enabled: !!id,
  });

  if (customerLoading) {
    return (
      <MainLayout>
        <div className="flex items-center justify-center h-64">
          <div className="text-gray-500">Loading customer details...</div>
        </div>
      </MainLayout>
    );
  }

  if (!customer) {
    return (
      <MainLayout>
        <div className="flex items-center justify-center h-64">
          <div className="text-gray-500">Customer not found</div>
        </div>
      </MainLayout>
    );
  }

  return (
    <MainLayout>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-4">
            <Link
              to="/admin/customers"
              className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
            >
              <ArrowLeft className="h-5 w-5 text-gray-600" />
            </Link>
            <div>
              <h1 className="text-3xl font-bold text-gray-900">Customer Details</h1>
              <p className="text-gray-600 mt-1">{customer.customerCode}</p>
            </div>
          </div>
          <Link
            to={`/admin/customers/${id}/edit`}
            className="flex items-center gap-2 px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700"
          >
            <Edit className="h-5 w-5" />
            Edit Customer
          </Link>
        </div>

        {/* Customer Info Cards */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Basic Info */}
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center gap-4 mb-6">
              <div className="flex-shrink-0 h-16 w-16 bg-primary-100 rounded-full flex items-center justify-center">
                <User className="h-8 w-8 text-primary-600" />
              </div>
              <div>
                <h2 className="text-xl font-bold text-gray-900">{customer.name}</h2>
                <span
                  className={`inline-flex items-center px-2 py-1 text-xs font-semibold rounded-full ${
                    customer.isActive
                      ? 'bg-green-100 text-green-800'
                      : 'bg-red-100 text-red-800'
                  }`}
                >
                  {customer.isActive ? 'Active' : 'Inactive'}
                </span>
              </div>
            </div>

            <div className="space-y-3">
              <div className="flex items-center gap-3 text-gray-600">
                <Phone className="h-5 w-5 flex-shrink-0" />
                <span>{customer.phone}</span>
              </div>
              {customer.email && (
                <div className="flex items-center gap-3 text-gray-600">
                  <Mail className="h-5 w-5 flex-shrink-0" />
                  <span className="truncate">{customer.email}</span>
                </div>
              )}
              {(customer.address || customer.city || customer.state) && (
                <div className="flex items-start gap-3 text-gray-600">
                  <MapPin className="h-5 w-5 flex-shrink-0 mt-0.5" />
                  <div className="text-sm">
                    {customer.address && <p>{customer.address}</p>}
                    {(customer.city || customer.state) && (
                      <p>
                        {customer.city}
                        {customer.city && customer.state && ', '}
                        {customer.state} {customer.postalCode}
                      </p>
                    )}
                  </div>
                </div>
              )}
              {customer.gstin && (
                <div className="flex items-center gap-3 text-gray-600">
                  <CreditCard className="h-5 w-5 flex-shrink-0" />
                  <div className="text-sm">
                    <p className="text-xs text-gray-500">GSTIN</p>
                    <p className="font-mono">{customer.gstin}</p>
                  </div>
                </div>
              )}
              <div className="flex items-center gap-3 text-gray-600 pt-3 border-t">
                <Calendar className="h-5 w-5 flex-shrink-0" />
                <div className="text-sm">
                  <p className="text-xs text-gray-500">Member Since</p>
                  <p>{new Date(customer.createdAt).toLocaleDateString()}</p>
                </div>
              </div>
            </div>
          </div>

          {/* Stats Cards */}
          <div className="lg:col-span-2 grid grid-cols-1 sm:grid-cols-3 gap-6">
            <div className="bg-white rounded-lg shadow p-6">
              <div className="flex items-center justify-between mb-2">
                <p className="text-sm text-gray-600">Total Purchases</p>
                <ShoppingBag className="h-5 w-5 text-primary-600" />
              </div>
              <p className="text-3xl font-bold text-gray-900">
                ₹{customer.totalPurchases.toLocaleString()}
              </p>
            </div>

            <div className="bg-white rounded-lg shadow p-6">
              <div className="flex items-center justify-between mb-2">
                <p className="text-sm text-gray-600">Loyalty Points</p>
                <Star className="h-5 w-5 text-purple-600" />
              </div>
              <p className="text-3xl font-bold text-purple-600">{customer.loyaltyPoints}</p>
            </div>

            <div className="bg-white rounded-lg shadow p-6">
              <div className="flex items-center justify-between mb-2">
                <p className="text-sm text-gray-600">Total Orders</p>
                <Receipt className="h-5 w-5 text-green-600" />
              </div>
              <p className="text-3xl font-bold text-gray-900">
                {purchaseHistory?.length || 0}
              </p>
            </div>
          </div>
        </div>

        {/* Purchase History */}
        <div className="bg-white rounded-lg shadow">
          <div className="px-6 py-4 border-b border-gray-200">
            <h2 className="text-xl font-bold text-gray-900">Purchase History</h2>
          </div>

          {historyLoading ? (
            <div className="p-12 text-center text-gray-500">Loading purchase history...</div>
          ) : purchaseHistory && purchaseHistory.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Bill Number
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Date
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Items
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Total Amount
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Payment Status
                    </th>
                    <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {purchaseHistory.map((bill) => (
                    <tr key={bill.id} className="hover:bg-gray-50">
                      <td className="px-6 py-4">
                        <div className="text-sm font-medium text-gray-900">{bill.billNumber}</div>
                      </td>
                      <td className="px-6 py-4">
                        <div className="text-sm text-gray-900">
                          {new Date(bill.billDate).toLocaleDateString()}
                        </div>
                        <div className="text-xs text-gray-500">
                          {new Date(bill.billDate).toLocaleTimeString()}
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <div className="text-sm text-gray-900">
                          {bill.totalItems} items ({bill.totalQuantity} units)
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <div className="text-sm font-medium text-gray-900">
                          ₹{bill.totalAmount.toLocaleString()}
                        </div>
                        {bill.discountAmount > 0 && (
                          <div className="text-xs text-green-600">
                            -₹{bill.discountAmount.toLocaleString()} discount
                          </div>
                        )}
                      </td>
                      <td className="px-6 py-4">
                        <span
                          className={`px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${
                            bill.paymentStatus === 'PAID'
                              ? 'bg-green-100 text-green-800'
                              : bill.paymentStatus === 'PARTIAL'
                              ? 'bg-yellow-100 text-yellow-800'
                              : 'bg-red-100 text-red-800'
                          }`}
                        >
                          {bill.paymentStatus}
                        </span>
                      </td>
                      <td className="px-6 py-4 text-right">
                        <Link
                          to={`/admin/bills/${bill.id}`}
                          className="text-primary-600 hover:text-primary-900"
                          title="View Bill"
                        >
                          <Eye className="h-5 w-5 inline" />
                        </Link>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="p-12 text-center">
              <Receipt className="h-16 w-16 mx-auto text-gray-400 mb-4" />
              <p className="text-gray-500">No purchase history yet</p>
            </div>
          )}
        </div>
      </div>
    </MainLayout>
  );
};

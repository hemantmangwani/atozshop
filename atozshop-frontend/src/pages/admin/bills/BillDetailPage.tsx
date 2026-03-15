import { useQuery } from '@tanstack/react-query';
import { useParams, Link } from 'react-router-dom';
import { MainLayout } from '../../../components/layout/MainLayout';
import { billService } from '../../../services/billService';
import {
  ArrowLeft,
  Receipt,
  User,
  Calendar,
  Package,
  CreditCard,
  Printer,
  Download,
} from 'lucide-react';

export const BillDetailPage = () => {
  const { id } = useParams<{ id: string }>();

  const { data: bill, isLoading } = useQuery({
    queryKey: ['bill', id],
    queryFn: () => billService.getBillById(Number(id)),
    enabled: !!id,
  });

  const handlePrint = () => {
    window.print();
  };

  const handleDownloadReceipt = async () => {
    if (!bill) return;
    try {
      const receipt = await billService.getReceipt(bill.id);
      // Create a blob and download
      const blob = new Blob([JSON.stringify(receipt, null, 2)], { type: 'application/json' });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `receipt-${bill.billNumber}.json`;
      a.click();
    } catch (error) {
      console.error('Failed to download receipt:', error);
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'CONFIRMED':
        return 'bg-green-100 text-green-800';
      case 'DRAFT':
        return 'bg-yellow-100 text-yellow-800';
      case 'CANCELLED':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const getPaymentBadge = (status: string) => {
    switch (status) {
      case 'PAID':
        return 'bg-green-100 text-green-800';
      case 'PARTIAL':
        return 'bg-yellow-100 text-yellow-800';
      case 'UNPAID':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  if (isLoading) {
    return (
      <MainLayout>
        <div className="flex items-center justify-center h-64">
          <div className="text-gray-500">Loading bill details...</div>
        </div>
      </MainLayout>
    );
  }

  if (!bill) {
    return (
      <MainLayout>
        <div className="flex items-center justify-center h-64">
          <div className="text-gray-500">Bill not found</div>
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
              to="/admin/bills"
              className="p-2 hover:bg-gray-100 rounded-lg transition-colors print:hidden"
            >
              <ArrowLeft className="h-5 w-5 text-gray-600" />
            </Link>
            <div>
              <h1 className="text-3xl font-bold text-gray-900">Bill Details</h1>
              <p className="text-gray-600 mt-1">{bill.billNumber}</p>
            </div>
          </div>
          <div className="flex items-center gap-2 print:hidden">
            <button
              onClick={handleDownloadReceipt}
              className="flex items-center gap-2 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50"
            >
              <Download className="h-5 w-5" />
              Download
            </button>
            <button
              onClick={handlePrint}
              className="flex items-center gap-2 px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700"
            >
              <Printer className="h-5 w-5" />
              Print Receipt
            </button>
          </div>
        </div>

        {/* Bill Info Cards */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Bill Status */}
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center gap-3 mb-4">
              <Receipt className="h-6 w-6 text-primary-600" />
              <h2 className="text-lg font-semibold text-gray-900">Bill Information</h2>
            </div>
            <div className="space-y-3">
              <div>
                <p className="text-sm text-gray-600">Bill Number</p>
                <p className="text-lg font-semibold text-gray-900">{bill.billNumber}</p>
              </div>
              <div>
                <p className="text-sm text-gray-600">Date & Time</p>
                <p className="text-sm text-gray-900">
                  {new Date(bill.billDate).toLocaleString()}
                </p>
              </div>
              <div>
                <p className="text-sm text-gray-600">Status</p>
                <span
                  className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusBadge(
                    bill.status
                  )}`}
                >
                  {bill.status}
                </span>
              </div>
              <div>
                <p className="text-sm text-gray-600">Payment Status</p>
                <span
                  className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getPaymentBadge(
                    bill.paymentStatus
                  )}`}
                >
                  {bill.paymentStatus}
                </span>
              </div>
            </div>
          </div>

          {/* Customer Info */}
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center gap-3 mb-4">
              <User className="h-6 w-6 text-primary-600" />
              <h2 className="text-lg font-semibold text-gray-900">Customer</h2>
            </div>
            {bill.customerId ? (
              <div className="space-y-3">
                <div>
                  <p className="text-sm text-gray-600">Name</p>
                  <p className="text-sm font-medium text-gray-900">{bill.customerName}</p>
                </div>
                {bill.customerPhone && (
                  <div>
                    <p className="text-sm text-gray-600">Phone</p>
                    <p className="text-sm text-gray-900">{bill.customerPhone}</p>
                  </div>
                )}
                <Link
                  to={`/admin/customers/${bill.customerId}`}
                  className="text-sm text-primary-600 hover:text-primary-700"
                >
                  View Customer Profile →
                </Link>
              </div>
            ) : (
              <p className="text-gray-500">Walk-in Customer</p>
            )}
          </div>

          {/* Summary */}
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center gap-3 mb-4">
              <Package className="h-6 w-6 text-primary-600" />
              <h2 className="text-lg font-semibold text-gray-900">Summary</h2>
            </div>
            <div className="space-y-3">
              <div className="flex items-center justify-between">
                <span className="text-sm text-gray-600">Total Items</span>
                <span className="text-sm font-medium text-gray-900">{bill.totalItems}</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-sm text-gray-600">Total Quantity</span>
                <span className="text-sm font-medium text-gray-900">{bill.totalQuantity}</span>
              </div>
              <div className="flex items-center justify-between pt-3 border-t">
                <span className="text-sm text-gray-600">Subtotal</span>
                <span className="text-sm font-medium text-gray-900">
                  ₹{bill.subtotal.toLocaleString()}
                </span>
              </div>
              {bill.discountAmount > 0 && (
                <div className="flex items-center justify-between">
                  <span className="text-sm text-green-600">Discount</span>
                  <span className="text-sm font-medium text-green-600">
                    -₹{bill.discountAmount.toLocaleString()}
                  </span>
                </div>
              )}
              <div className="flex items-center justify-between pt-3 border-t">
                <span className="text-base font-semibold text-gray-900">Total Amount</span>
                <span className="text-xl font-bold text-primary-600">
                  ₹{bill.totalAmount.toLocaleString()}
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Bill Items */}
        <div className="bg-white rounded-lg shadow">
          <div className="px-6 py-4 border-b border-gray-200">
            <h2 className="text-lg font-semibold text-gray-900">Items</h2>
          </div>
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                    Product
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                    SKU
                  </th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">
                    Qty
                  </th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">
                    Unit Price
                  </th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">
                    Discount
                  </th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">
                    Total
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {bill.items.map((item: any) => (
                  <tr key={item.id}>
                    <td className="px-6 py-4">
                      <div className="text-sm font-medium text-gray-900">{item.productName}</div>
                      {item.variantName && (
                        <div className="text-sm text-gray-500">{item.variantName}</div>
                      )}
                    </td>
                    <td className="px-6 py-4">
                      <div className="text-sm font-mono text-gray-900">{item.sku}</div>
                    </td>
                    <td className="px-6 py-4 text-right">
                      <div className="text-sm text-gray-900">{item.quantity}</div>
                    </td>
                    <td className="px-6 py-4 text-right">
                      <div className="text-sm text-gray-900">
                        ₹{item.unitPrice.toLocaleString()}
                      </div>
                    </td>
                    <td className="px-6 py-4 text-right">
                      {item.discountAmount > 0 ? (
                        <div className="text-sm text-green-600">
                          -₹{item.discountAmount.toLocaleString()}
                        </div>
                      ) : (
                        <div className="text-sm text-gray-400">-</div>
                      )}
                    </td>
                    <td className="px-6 py-4 text-right">
                      <div className="text-sm font-medium text-gray-900">
                        ₹{item.totalAmount.toLocaleString()}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* Payments */}
        {bill.payments && bill.payments.length > 0 && (
          <div className="bg-white rounded-lg shadow">
            <div className="px-6 py-4 border-b border-gray-200">
              <div className="flex items-center gap-2">
                <CreditCard className="h-5 w-5 text-primary-600" />
                <h2 className="text-lg font-semibold text-gray-900">Payments</h2>
              </div>
            </div>
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                      Date & Time
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                      Payment Method
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                      Reference
                    </th>
                    <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">
                      Amount
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {bill.payments.map((payment: any) => (
                    <tr key={payment.id}>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm text-gray-900">
                          {new Date(payment.paymentDate).toLocaleString()}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm text-gray-900">{payment.paymentMethod}</div>
                      </td>
                      <td className="px-6 py-4">
                        <div className="text-sm text-gray-500">
                          {payment.referenceNumber || '-'}
                        </div>
                      </td>
                      <td className="px-6 py-4 text-right whitespace-nowrap">
                        <div className="text-sm font-medium text-green-600">
                          ₹{payment.amount.toLocaleString()}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* Payment Summary */}
        <div className="bg-white rounded-lg shadow p-6">
          <div className="max-w-md ml-auto space-y-3">
            <div className="flex items-center justify-between text-sm">
              <span className="text-gray-600">Total Amount</span>
              <span className="font-medium text-gray-900">
                ₹{bill.totalAmount.toLocaleString()}
              </span>
            </div>
            <div className="flex items-center justify-between text-sm">
              <span className="text-gray-600">Paid Amount</span>
              <span className="font-medium text-green-600">
                ₹{bill.paidAmount.toLocaleString()}
              </span>
            </div>
            <div className="flex items-center justify-between pt-3 border-t">
              <span className="font-semibold text-gray-900">Balance Due</span>
              <span className="text-xl font-bold text-red-600">
                ₹{bill.balanceAmount.toLocaleString()}
              </span>
            </div>
          </div>
        </div>

        {bill.notes && (
          <div className="bg-white rounded-lg shadow p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-2">Notes</h2>
            <p className="text-sm text-gray-600">{bill.notes}</p>
          </div>
        )}
      </div>
    </MainLayout>
  );
};

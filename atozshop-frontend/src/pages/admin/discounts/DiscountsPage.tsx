import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { MainLayout } from '../../../components/layout/MainLayout';
import { discountService } from '../../../services/discountService';
import { Plus, Edit, Trash2, Tag, ToggleLeft, ToggleRight } from 'lucide-react';
import toast from 'react-hot-toast';
import { DiscountFormModal } from './DiscountFormModal';

export const DiscountsPage = () => {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingDiscount, setEditingDiscount] = useState<any>(null);
  const queryClient = useQueryClient();

  const { data: discounts, isLoading } = useQuery({
    queryKey: ['discounts'],
    queryFn: () => discountService.getAllDiscounts(),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => discountService.deleteDiscount(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['discounts'] });
      toast.success('Discount deleted successfully');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to delete discount');
    },
  });

  const toggleMutation = useMutation({
    mutationFn: (id: number) => discountService.toggleDiscountStatus(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['discounts'] });
      toast.success('Discount status updated');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to update discount');
    },
  });

  const handleDelete = (id: number, name: string) => {
    if (window.confirm(`Are you sure you want to delete discount "${name}"?`)) {
      deleteMutation.mutate(id);
    }
  };

  const handleEdit = (discount: any) => {
    setEditingDiscount(discount);
    setIsModalOpen(true);
  };

  const handleToggle = (id: number) => {
    toggleMutation.mutate(id);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setEditingDiscount(null);
  };

  const activeDiscounts = discounts?.filter((d: any) => d.isActive).length || 0;

  return (
    <MainLayout>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Discounts & Offers</h1>
            <p className="text-gray-600 mt-1">Manage promotional discounts and special offers</p>
          </div>
          <button
            onClick={() => setIsModalOpen(true)}
            className="flex items-center gap-2 px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700"
          >
            <Plus className="h-5 w-5" />
            Add Discount
          </button>
        </div>

        {/* Stats */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="bg-white rounded-lg shadow p-6">
            <p className="text-sm text-gray-600">Total Discounts</p>
            <p className="text-3xl font-bold text-gray-900">{discounts?.length || 0}</p>
          </div>
          <div className="bg-white rounded-lg shadow p-6">
            <p className="text-sm text-gray-600">Active Discounts</p>
            <p className="text-3xl font-bold text-green-600">{activeDiscounts}</p>
          </div>
          <div className="bg-white rounded-lg shadow p-6">
            <p className="text-sm text-gray-600">Inactive Discounts</p>
            <p className="text-3xl font-bold text-red-600">
              {(discounts?.length || 0) - activeDiscounts}
            </p>
          </div>
        </div>

        {/* Discounts Table */}
        <div className="bg-white rounded-lg shadow overflow-hidden">
          {isLoading ? (
            <div className="p-12 text-center text-gray-500">Loading discounts...</div>
          ) : discounts && discounts.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Discount
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Code
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Type & Value
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Applicable On
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Valid Period
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Min Purchase
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Status
                    </th>
                    <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {discounts.map((discount: any) => (
                    <tr key={discount.id} className="hover:bg-gray-50">
                      <td className="px-6 py-4">
                        <div className="text-sm font-medium text-gray-900">{discount.name}</div>
                        {discount.description && (
                          <div className="text-sm text-gray-500 line-clamp-1">
                            {discount.description}
                          </div>
                        )}
                      </td>
                      <td className="px-6 py-4">
                        <div className="text-sm font-mono text-gray-900">
                          {discount.discountCode}
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <div className="text-sm text-gray-900">
                          {discount.discountType === 'PERCENTAGE' ? (
                            <span className="font-medium text-primary-600">
                              {discount.discountValue}% OFF
                            </span>
                          ) : (
                            <span className="font-medium text-green-600">
                              ₹{discount.discountValue} OFF
                            </span>
                          )}
                        </div>
                        {discount.maxDiscountAmount && (
                          <div className="text-xs text-gray-500">
                            Max: ₹{discount.maxDiscountAmount}
                          </div>
                        )}
                      </td>
                      <td className="px-6 py-4">
                        <span className="px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full bg-blue-100 text-blue-800">
                          {discount.applicableOn}
                        </span>
                      </td>
                      <td className="px-6 py-4">
                        {discount.validFrom && discount.validTo ? (
                          <div className="text-sm text-gray-900">
                            <div>{new Date(discount.validFrom).toLocaleDateString()}</div>
                            <div className="text-xs text-gray-500">
                              to {new Date(discount.validTo).toLocaleDateString()}
                            </div>
                          </div>
                        ) : (
                          <div className="text-sm text-gray-500">No expiry</div>
                        )}
                      </td>
                      <td className="px-6 py-4">
                        {discount.minPurchaseAmount ? (
                          <div className="text-sm text-gray-900">
                            ₹{discount.minPurchaseAmount}
                          </div>
                        ) : (
                          <div className="text-sm text-gray-500">None</div>
                        )}
                      </td>
                      <td className="px-6 py-4">
                        <button
                          onClick={() => handleToggle(discount.id)}
                          className="flex items-center gap-2"
                        >
                          {discount.isActive ? (
                            <>
                              <ToggleRight className="h-6 w-6 text-green-600" />
                              <span className="text-xs text-green-600 font-medium">Active</span>
                            </>
                          ) : (
                            <>
                              <ToggleLeft className="h-6 w-6 text-gray-400" />
                              <span className="text-xs text-gray-500 font-medium">Inactive</span>
                            </>
                          )}
                        </button>
                      </td>
                      <td className="px-6 py-4 text-right text-sm font-medium">
                        <div className="flex items-center justify-end gap-2">
                          <button
                            onClick={() => handleEdit(discount)}
                            className="text-blue-600 hover:text-blue-900"
                            title="Edit"
                          >
                            <Edit className="h-5 w-5" />
                          </button>
                          <button
                            onClick={() => handleDelete(discount.id, discount.name)}
                            className="text-red-600 hover:text-red-900"
                            title="Delete"
                          >
                            <Trash2 className="h-5 w-5" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="p-12 text-center">
              <Tag className="h-16 w-16 mx-auto text-gray-400 mb-4" />
              <p className="text-gray-500">No discounts found</p>
            </div>
          )}
        </div>

        {/* Modal */}
        {isModalOpen && (
          <DiscountFormModal discount={editingDiscount} onClose={handleCloseModal} />
        )}
      </div>
    </MainLayout>
  );
};

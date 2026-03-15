import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { discountService } from '../../../services/discountService';
import { X } from 'lucide-react';
import toast from 'react-hot-toast';

interface DiscountFormModalProps {
  discount?: any;
  onClose: () => void;
}

export const DiscountFormModal = ({ discount, onClose }: DiscountFormModalProps) => {
  const queryClient = useQueryClient();
  const isEditing = !!discount;

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
    watch,
  } = useForm({
    defaultValues: {
      name: discount?.name || '',
      discountCode: discount?.discountCode || '',
      description: discount?.description || '',
      discountType: discount?.discountType || 'PERCENTAGE',
      discountValue: discount?.discountValue || 0,
      minPurchaseAmount: discount?.minPurchaseAmount || '',
      maxDiscountAmount: discount?.maxDiscountAmount || '',
      applicableOn: discount?.applicableOn || 'BILL',
      validFrom: discount?.validFrom ? discount.validFrom.split('T')[0] : '',
      validTo: discount?.validTo ? discount.validTo.split('T')[0] : '',
      isActive: discount?.isActive ?? true,
    },
  });

  const discountType = watch('discountType');

  useEffect(() => {
    if (discount) {
      reset({
        name: discount.name,
        discountCode: discount.discountCode,
        description: discount.description || '',
        discountType: discount.discountType,
        discountValue: discount.discountValue,
        minPurchaseAmount: discount.minPurchaseAmount || '',
        maxDiscountAmount: discount.maxDiscountAmount || '',
        applicableOn: discount.applicableOn,
        validFrom: discount.validFrom ? discount.validFrom.split('T')[0] : '',
        validTo: discount.validTo ? discount.validTo.split('T')[0] : '',
        isActive: discount.isActive,
      });
    }
  }, [discount, reset]);

  const createMutation = useMutation({
    mutationFn: (data: any) => discountService.createDiscount(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['discounts'] });
      toast.success('Discount created successfully');
      onClose();
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to create discount');
    },
  });

  const updateMutation = useMutation({
    mutationFn: (data: any) => discountService.updateDiscount(discount.id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['discounts'] });
      toast.success('Discount updated successfully');
      onClose();
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to update discount');
    },
  });

  const onSubmit = (data: any) => {
    // Convert empty strings to null for optional fields
    const formData = {
      ...data,
      minPurchaseAmount: data.minPurchaseAmount ? Number(data.minPurchaseAmount) : null,
      maxDiscountAmount: data.maxDiscountAmount ? Number(data.maxDiscountAmount) : null,
      validFrom: data.validFrom || null,
      validTo: data.validTo || null,
    };

    if (isEditing) {
      updateMutation.mutate(formData);
    } else {
      createMutation.mutate(formData);
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg shadow-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-200 sticky top-0 bg-white">
          <h2 className="text-xl font-bold text-gray-900">
            {isEditing ? 'Edit Discount' : 'Add New Discount'}
          </h2>
          <button
            onClick={onClose}
            className="p-1 hover:bg-gray-100 rounded-lg transition-colors"
          >
            <X className="h-5 w-5 text-gray-600" />
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit(onSubmit)} className="p-6 space-y-4">
          {/* Basic Info */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="md:col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Discount Name <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                {...register('name', { required: 'Discount name is required' })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
                placeholder="e.g., Summer Sale, New Customer Offer"
              />
              {errors.name && (
                <p className="text-red-500 text-sm mt-1">{errors.name.message}</p>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Discount Code <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                {...register('discountCode', { required: 'Discount code is required' })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
                placeholder="e.g., SUMMER25"
              />
              {errors.discountCode && (
                <p className="text-red-500 text-sm mt-1">{errors.discountCode.message}</p>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Applicable On <span className="text-red-500">*</span>
              </label>
              <select
                {...register('applicableOn', { required: true })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
              >
                <option value="BILL">Bill Total</option>
                <option value="ITEM">Individual Item</option>
                <option value="CATEGORY">Category</option>
              </select>
            </div>

            <div className="md:col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Description
              </label>
              <textarea
                {...register('description')}
                rows={2}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
                placeholder="Discount description..."
              />
            </div>
          </div>

          {/* Discount Value */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Discount Type <span className="text-red-500">*</span>
              </label>
              <select
                {...register('discountType', { required: true })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
              >
                <option value="PERCENTAGE">Percentage (%)</option>
                <option value="FIXED_AMOUNT">Fixed Amount (₹)</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Discount Value <span className="text-red-500">*</span>
              </label>
              <div className="relative">
                <input
                  type="number"
                  step="0.01"
                  {...register('discountValue', {
                    required: 'Discount value is required',
                    min: 0.01,
                  })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
                  placeholder="0.00"
                />
                <span className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-500">
                  {discountType === 'PERCENTAGE' ? '%' : '₹'}
                </span>
              </div>
              {errors.discountValue && (
                <p className="text-red-500 text-sm mt-1">{errors.discountValue.message}</p>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Min Purchase Amount
              </label>
              <input
                type="number"
                step="0.01"
                {...register('minPurchaseAmount')}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
                placeholder="Optional"
              />
              <p className="text-xs text-gray-500 mt-1">
                Minimum bill amount required to use this discount
              </p>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Max Discount Amount
              </label>
              <input
                type="number"
                step="0.01"
                {...register('maxDiscountAmount')}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
                placeholder="Optional"
              />
              <p className="text-xs text-gray-500 mt-1">
                Maximum discount amount (for percentage discounts)
              </p>
            </div>
          </div>

          {/* Validity Period */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Valid From
              </label>
              <input
                type="date"
                {...register('validFrom')}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Valid To
              </label>
              <input
                type="date"
                {...register('validTo')}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
              />
            </div>
          </div>

          {/* Status */}
          <div className="flex items-center gap-3">
            <input
              type="checkbox"
              id="isActive"
              {...register('isActive')}
              className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
            />
            <label htmlFor="isActive" className="text-sm font-medium text-gray-700">
              Active Discount
            </label>
          </div>

          {/* Actions */}
          <div className="flex items-center justify-end gap-3 pt-4 border-t">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={createMutation.isPending || updateMutation.isPending}
              className="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 disabled:opacity-50"
            >
              {createMutation.isPending || updateMutation.isPending
                ? 'Saving...'
                : isEditing
                ? 'Save Changes'
                : 'Create Discount'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

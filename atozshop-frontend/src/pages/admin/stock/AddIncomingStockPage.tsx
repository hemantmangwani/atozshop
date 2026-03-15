import { useState } from 'react';
import { useForm, useFieldArray } from 'react-hook-form';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useNavigate, Link } from 'react-router-dom';
import { MainLayout } from '../../../components/layout/MainLayout';
import { stockService, IncomingStockRequest } from '../../../services/stockService';
import { productService } from '../../../services/productService';
import { ArrowLeft, Plus, Trash2, Package, Search } from 'lucide-react';
import toast from 'react-hot-toast';

interface StockItem {
  variantId: number;
  sku: string;
  productName: string;
  variantName: string;
  quantity: number;
  costPrice: number;
  sellingPrice: number;
}

export const AddIncomingStockPage = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<any[]>([]);

  const { register, handleSubmit, control, formState: { errors } } = useForm<IncomingStockRequest>({
    defaultValues: {
      items: [],
      notes: '',
    },
  });

  const { fields, append, remove } = useFieldArray({
    control,
    name: 'items',
  });

  const { data: products } = useQuery({
    queryKey: ['products-with-variants'],
    queryFn: () => productService.getAllProductsWithVariants(),
  });

  const addStockMutation = useMutation({
    mutationFn: (data: IncomingStockRequest) => stockService.addIncomingStock(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['current-stock'] });
      queryClient.invalidateQueries({ queryKey: ['stock-ledger'] });
      toast.success('Incoming stock added successfully');
      navigate('/admin/stock');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to add incoming stock');
    },
  });

  const handleSearch = (query: string) => {
    setSearchQuery(query);
    if (!query.trim() || !products) {
      setSearchResults([]);
      return;
    }

    const results: any[] = [];
    products.forEach((product: any) => {
      product.variants?.forEach((variant: any) => {
        const matchesSku = variant.sku.toLowerCase().includes(query.toLowerCase());
        const matchesProduct = product.name.toLowerCase().includes(query.toLowerCase());
        const matchesVariant = variant.name?.toLowerCase().includes(query.toLowerCase());

        if (matchesSku || matchesProduct || matchesVariant) {
          results.push({
            variantId: variant.id,
            sku: variant.sku,
            productName: product.name,
            variantName: variant.name,
            currentPrice: variant.currentPrice || {},
          });
        }
      });
    });

    setSearchResults(results.slice(0, 10));
  };

  const addVariantToList = (variant: any) => {
    // Check if already added
    const exists = fields.some((f: any) => f.variantId === variant.variantId);
    if (exists) {
      toast.error('This variant is already in the list');
      return;
    }

    append({
      variantId: variant.variantId,
      sku: variant.sku,
      productName: variant.productName,
      variantName: variant.variantName,
      quantity: 1,
      costPrice: variant.currentPrice?.costPrice || 0,
      sellingPrice: variant.currentPrice?.sellingPrice || 0,
    } as any);

    setSearchQuery('');
    setSearchResults([]);
  };

  const onSubmit = (data: IncomingStockRequest) => {
    if (!data.items || data.items.length === 0) {
      toast.error('Please add at least one item');
      return;
    }

    // Validate all items
    const invalid = data.items.some(
      (item: any) => !item.quantity || item.quantity <= 0 || !item.costPrice || item.costPrice <= 0
    );

    if (invalid) {
      toast.error('All items must have valid quantity and cost price');
      return;
    }

    addStockMutation.mutate(data);
  };

  const totalItems = fields.length;
  const totalQuantity = fields.reduce((sum, field: any) => sum + (Number(field.quantity) || 0), 0);
  const totalValue = fields.reduce(
    (sum, field: any) => sum + (Number(field.quantity) || 0) * (Number(field.costPrice) || 0),
    0
  );

  return (
    <MainLayout>
      <div className="max-w-6xl mx-auto space-y-6">
        {/* Header */}
        <div className="flex items-center gap-4">
          <Link
            to="/admin/stock"
            className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
          >
            <ArrowLeft className="h-5 w-5 text-gray-600" />
          </Link>
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Add Incoming Stock</h1>
            <p className="text-gray-600 mt-1">Receive new stock from suppliers</p>
          </div>
        </div>

        {/* Summary Cards */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="bg-white rounded-lg shadow p-4">
            <p className="text-sm text-gray-600">Total Items</p>
            <p className="text-2xl font-bold text-gray-900">{totalItems}</p>
          </div>
          <div className="bg-white rounded-lg shadow p-4">
            <p className="text-sm text-gray-600">Total Quantity</p>
            <p className="text-2xl font-bold text-primary-600">{totalQuantity}</p>
          </div>
          <div className="bg-white rounded-lg shadow p-4">
            <p className="text-sm text-gray-600">Total Value</p>
            <p className="text-2xl font-bold text-green-600">₹{totalValue.toLocaleString()}</p>
          </div>
        </div>

        {/* Search & Add Items */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Search Products</h2>
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => handleSearch(e.target.value)}
              placeholder="Search by product name, variant, or SKU..."
              className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
            />
          </div>

          {/* Search Results */}
          {searchResults.length > 0 && (
            <div className="mt-2 border border-gray-200 rounded-lg max-h-60 overflow-y-auto">
              {searchResults.map((result) => (
                <button
                  key={result.variantId}
                  type="button"
                  onClick={() => addVariantToList(result)}
                  className="w-full px-4 py-3 hover:bg-gray-50 text-left border-b border-gray-100 last:border-b-0"
                >
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-sm font-medium text-gray-900">
                        {result.productName}
                        {result.variantName && ` - ${result.variantName}`}
                      </p>
                      <p className="text-xs text-gray-500">SKU: {result.sku}</p>
                    </div>
                    <Plus className="h-5 w-5 text-primary-600" />
                  </div>
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Items List */}
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
          <div className="bg-white rounded-lg shadow">
            <div className="px-6 py-4 border-b border-gray-200">
              <h2 className="text-lg font-semibold text-gray-900">Items to Receive</h2>
            </div>

            {fields.length === 0 ? (
              <div className="p-12 text-center">
                <Package className="h-16 w-16 mx-auto text-gray-400 mb-4" />
                <p className="text-gray-500">No items added yet</p>
                <p className="text-sm text-gray-400 mt-2">Search and add products above</p>
              </div>
            ) : (
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
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                        Quantity
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                        Cost Price
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                        Selling Price
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                        Total
                      </th>
                      <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">
                        Actions
                      </th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {fields.map((field: any, index) => (
                      <tr key={field.id}>
                        <td className="px-6 py-4">
                          <div className="text-sm font-medium text-gray-900">
                            {field.productName}
                          </div>
                          {field.variantName && (
                            <div className="text-sm text-gray-500">{field.variantName}</div>
                          )}
                        </td>
                        <td className="px-6 py-4">
                          <div className="text-sm font-mono text-gray-900">{field.sku}</div>
                        </td>
                        <td className="px-6 py-4">
                          <input
                            type="number"
                            {...register(`items.${index}.quantity` as const, {
                              required: true,
                              min: 1,
                            })}
                            className="w-24 px-2 py-1 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-primary-500"
                          />
                        </td>
                        <td className="px-6 py-4">
                          <input
                            type="number"
                            step="0.01"
                            {...register(`items.${index}.costPrice` as const, {
                              required: true,
                              min: 0,
                            })}
                            className="w-32 px-2 py-1 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-primary-500"
                          />
                        </td>
                        <td className="px-6 py-4">
                          <input
                            type="number"
                            step="0.01"
                            {...register(`items.${index}.sellingPrice` as const, {
                              required: true,
                              min: 0,
                            })}
                            className="w-32 px-2 py-1 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-primary-500"
                          />
                        </td>
                        <td className="px-6 py-4">
                          <div className="text-sm font-medium text-gray-900">
                            ₹
                            {(
                              (Number(field.quantity) || 0) * (Number(field.costPrice) || 0)
                            ).toLocaleString()}
                          </div>
                        </td>
                        <td className="px-6 py-4 text-right">
                          <button
                            type="button"
                            onClick={() => remove(index)}
                            className="text-red-600 hover:text-red-900"
                          >
                            <Trash2 className="h-5 w-5" />
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>

          {/* Notes */}
          <div className="bg-white rounded-lg shadow p-6">
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Notes (Optional)
            </label>
            <textarea
              {...register('notes')}
              rows={3}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
              placeholder="Add any notes about this stock receipt..."
            />
          </div>

          {/* Actions */}
          <div className="flex items-center justify-end gap-3">
            <Link
              to="/admin/stock"
              className="px-6 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50"
            >
              Cancel
            </Link>
            <button
              type="submit"
              disabled={addStockMutation.isPending || fields.length === 0}
              className="px-6 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 disabled:opacity-50"
            >
              {addStockMutation.isPending ? 'Adding Stock...' : 'Confirm Receipt'}
            </button>
          </div>
        </form>
      </div>
    </MainLayout>
  );
};

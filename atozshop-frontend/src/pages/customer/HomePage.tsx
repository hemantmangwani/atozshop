import { useQuery } from '@tanstack/react-query';
import { MainLayout } from '../../components/layout/MainLayout';
import { ProductCard } from '../../components/customer/ProductCard';
import { productService } from '../../services/productService';
import { Loader2 } from 'lucide-react';

export const HomePage = () => {
  const { data: products, isLoading, error } = useQuery({
    queryKey: ['products'],
    queryFn: () => productService.getAllProducts(),
  });

  return (
    <MainLayout>
      <div className="space-y-6">
        {/* Hero Section */}
        <div className="bg-gradient-to-r from-primary-600 to-primary-700 rounded-lg p-8 text-white">
          <h1 className="text-4xl font-bold mb-2">Welcome to AtoZShop</h1>
          <p className="text-lg opacity-90">
            Browse our wide selection of products with real-time stock availability
          </p>
        </div>

        {/* Products Section */}
        <div>
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-2xl font-bold text-gray-900">All Products</h2>
            <p className="text-sm text-gray-600">
              {products?.length || 0} products available
            </p>
          </div>

          {/* Loading State */}
          {isLoading && (
            <div className="flex items-center justify-center py-12">
              <Loader2 className="h-8 w-8 animate-spin text-primary-600" />
              <span className="ml-2 text-gray-600">Loading products...</span>
            </div>
          )}

          {/* Error State */}
          {error && (
            <div className="bg-red-50 border border-red-200 rounded-lg p-4 text-red-700">
              <p className="font-semibold">Error loading products</p>
              <p className="text-sm mt-1">{(error as any).message || 'Please try again later'}</p>
            </div>
          )}

          {/* Products Grid */}
          {products && products.length > 0 && (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
              {products.map((product) => (
                <ProductCard key={product.id} product={product} />
              ))}
            </div>
          )}

          {/* Empty State */}
          {products && products.length === 0 && (
            <div className="text-center py-12">
              <p className="text-gray-600">No products found</p>
            </div>
          )}
        </div>
      </div>
    </MainLayout>
  );
};

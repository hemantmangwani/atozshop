import { useParams, Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { MainLayout } from '../../components/layout/MainLayout';
import { productService } from '../../services/productService';
import { useCart } from '../../context/CartContext';
import { Loader2, ArrowLeft, ShoppingCart, Package, Check } from 'lucide-react';
import { useState } from 'react';

export const ProductDetailPage = () => {
  const { id } = useParams<{ id: string }>();
  const { addToCart } = useCart();
  const [selectedVariantId, setSelectedVariantId] = useState<number | null>(null);
  const [quantity, setQuantity] = useState(1);
  const [isAdding, setIsAdding] = useState(false);

  const { data: product, isLoading, error } = useQuery({
    queryKey: ['product', id],
    queryFn: () => productService.getProductById(Number(id)),
    enabled: !!id,
  });

  const selectedVariant = product?.variants.find(
    (v) => v.id === (selectedVariantId || product.defaultVariantId)
  );

  const handleAddToCart = async () => {
    if (!product || !selectedVariant) return;

    setIsAdding(true);
    try {
      await addToCart({
        variantId: selectedVariant.id,
        productName: product.name,
        variantName: selectedVariant.name,
        sku: selectedVariant.sku,
        quantity,
        unitPrice: selectedVariant.sellingPrice,
        mrp: selectedVariant.mrp,
        availableStock: selectedVariant.availableStock,
      });
      alert('Added to cart!');
      setQuantity(1);
    } catch (error: any) {
      alert(error.message || 'Failed to add to cart');
    } finally {
      setIsAdding(false);
    }
  };

  if (isLoading) {
    return (
      <MainLayout>
        <div className="flex items-center justify-center py-12">
          <Loader2 className="h-8 w-8 animate-spin text-primary-600" />
          <span className="ml-2 text-gray-600">Loading product...</span>
        </div>
      </MainLayout>
    );
  }

  if (error || !product) {
    return (
      <MainLayout>
        <div className="max-w-4xl mx-auto">
          <div className="bg-red-50 border border-red-200 rounded-lg p-4 text-red-700">
            <p className="font-semibold">Product not found</p>
            <Link to="/" className="text-primary-600 hover:underline mt-2 inline-block">
              ← Back to home
            </Link>
          </div>
        </div>
      </MainLayout>
    );
  }

  const currentVariant = selectedVariant || product.variants[0];
  const isOutOfStock = !currentVariant || currentVariant.availableStock <= 0;

  return (
    <MainLayout>
      <div className="max-w-6xl mx-auto">
        {/* Breadcrumb */}
        <div className="mb-6">
          <Link
            to="/"
            className="text-primary-600 hover:text-primary-700 flex items-center space-x-1"
          >
            <ArrowLeft className="h-4 w-4" />
            <span>Back to products</span>
          </Link>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          {/* Product Image */}
          <div className="bg-white rounded-lg shadow-sm p-8">
            <div className="aspect-square bg-gray-100 rounded-lg flex items-center justify-center">
              <Package className="h-32 w-32 text-gray-400" />
            </div>
          </div>

          {/* Product Info */}
          <div className="space-y-6">
            {/* Brand */}
            {product.brandName && (
              <p className="text-sm text-gray-500 uppercase tracking-wide">
                {product.brandName}
              </p>
            )}

            {/* Product Name */}
            <h1 className="text-3xl font-bold text-gray-900">{product.name}</h1>

            {/* Category */}
            {product.categoryName && (
              <p className="text-sm text-gray-600">Category: {product.categoryName}</p>
            )}

            {/* Price */}
            <div className="flex items-baseline space-x-3">
              <span className="text-3xl font-bold text-gray-900">
                ₹{currentVariant.sellingPrice.toFixed(2)}
              </span>
              {currentVariant.mrp > currentVariant.sellingPrice && (
                <>
                  <span className="text-xl text-gray-500 line-through">
                    ₹{currentVariant.mrp.toFixed(2)}
                  </span>
                  <span className="text-lg font-semibold text-green-600">
                    {(
                      ((currentVariant.mrp - currentVariant.sellingPrice) /
                        currentVariant.mrp) *
                      100
                    ).toFixed(0)}
                    % off
                  </span>
                </>
              )}
            </div>

            {/* Stock Status */}
            <div>
              {isOutOfStock ? (
                <span className="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium bg-red-100 text-red-800">
                  Out of Stock
                </span>
              ) : currentVariant.availableStock <= 5 ? (
                <span className="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium bg-yellow-100 text-yellow-800">
                  Only {currentVariant.availableStock} left
                </span>
              ) : (
                <span className="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium bg-green-100 text-green-800">
                  <Check className="h-4 w-4 mr-1" />
                  In Stock
                </span>
              )}
            </div>

            {/* Description */}
            {product.description && (
              <div>
                <h2 className="text-lg font-semibold text-gray-900 mb-2">Description</h2>
                <p className="text-gray-600 leading-relaxed">{product.description}</p>
              </div>
            )}

            {/* Variant Selection */}
            {product.variants.length > 1 && (
              <div>
                <h2 className="text-lg font-semibold text-gray-900 mb-3">Select Variant</h2>
                <div className="grid grid-cols-2 sm:grid-cols-3 gap-2">
                  {product.variants.map((variant) => {
                    const isSelected =
                      variant.id === (selectedVariantId || product.defaultVariantId);
                    const isAvailable = variant.availableStock > 0;

                    return (
                      <button
                        key={variant.id}
                        onClick={() => setSelectedVariantId(variant.id)}
                        disabled={!isAvailable}
                        className={`
                          p-3 rounded-lg border-2 text-left transition-all
                          ${
                            isSelected
                              ? 'border-primary-600 bg-primary-50'
                              : 'border-gray-200 hover:border-gray-300'
                          }
                          ${!isAvailable ? 'opacity-50 cursor-not-allowed' : ''}
                        `}
                      >
                        <p className="font-medium text-sm">{variant.name}</p>
                        <p className="text-xs text-gray-600 mt-1">
                          ₹{variant.sellingPrice.toFixed(2)}
                        </p>
                        {!isAvailable && (
                          <p className="text-xs text-red-600 mt-1">Out of stock</p>
                        )}
                      </button>
                    );
                  })}
                </div>
              </div>
            )}

            {/* Quantity */}
            {!isOutOfStock && (
              <div>
                <h2 className="text-lg font-semibold text-gray-900 mb-3">Quantity</h2>
                <div className="flex items-center space-x-4">
                  <button
                    onClick={() => setQuantity(Math.max(1, quantity - 1))}
                    className="p-2 rounded border border-gray-300 hover:bg-gray-50"
                  >
                    -
                  </button>
                  <span className="w-16 text-center font-semibold text-lg">{quantity}</span>
                  <button
                    onClick={() =>
                      setQuantity(Math.min(currentVariant.availableStock, quantity + 1))
                    }
                    className="p-2 rounded border border-gray-300 hover:bg-gray-50"
                  >
                    +
                  </button>
                  <span className="text-sm text-gray-600">
                    Max: {currentVariant.availableStock}
                  </span>
                </div>
              </div>
            )}

            {/* Add to Cart Button */}
            <div className="flex space-x-4">
              <button
                onClick={handleAddToCart}
                disabled={isOutOfStock || isAdding}
                className="flex-1 bg-primary-600 text-white py-3 px-6 rounded-lg hover:bg-primary-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors flex items-center justify-center space-x-2 font-semibold"
              >
                <ShoppingCart className="h-5 w-5" />
                <span>{isAdding ? 'Adding...' : 'Add to Cart'}</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </MainLayout>
  );
};

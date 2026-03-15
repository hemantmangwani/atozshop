import { Link } from 'react-router-dom';
import { ShoppingCart, Package } from 'lucide-react';
import type { PublicProductResponse } from '../../types/product';
import { useCart } from '../../context/CartContext';
import { useState } from 'react';

interface ProductCardProps {
  product: PublicProductResponse;
}

export const ProductCard = ({ product }: ProductCardProps) => {
  const { addToCart } = useCart();
  const [isAdding, setIsAdding] = useState(false);

  const handleAddToCart = async (e: React.MouseEvent) => {
    e.preventDefault(); // Prevent navigation
    setIsAdding(true);

    try {
      await addToCart({
        variantId: product.defaultVariantId,
        productName: product.name,
        variantName: product.defaultVariantName,
        sku: product.sku,
        quantity: 1,
        unitPrice: product.sellingPrice,
        mrp: product.mrp,
        availableStock: product.availableStock,
      });
      alert('Added to cart!');
    } catch (error: any) {
      alert(error.message || 'Failed to add to cart');
    } finally {
      setIsAdding(false);
    }
  };

  const getStockBadge = () => {
    if (!product.isAvailable) {
      return (
        <span className="inline-flex items-center px-2 py-1 rounded text-xs font-medium bg-red-100 text-red-800">
          Out of Stock
        </span>
      );
    }

    if (product.stockStatus === 'Low Stock') {
      return (
        <span className="inline-flex items-center px-2 py-1 rounded text-xs font-medium bg-yellow-100 text-yellow-800">
          Only {product.availableStock} left
        </span>
      );
    }

    return (
      <span className="inline-flex items-center px-2 py-1 rounded text-xs font-medium bg-green-100 text-green-800">
        In Stock
      </span>
    );
  };

  return (
    <Link to={`/products/${product.id}`} className="group">
      <div className="bg-white rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200 overflow-hidden">
        {/* Product Image Placeholder */}
        <div className="aspect-square bg-gray-100 flex items-center justify-center">
          <Package className="h-16 w-16 text-gray-400" />
        </div>

        {/* Product Info */}
        <div className="p-4">
          {/* Brand */}
          {product.brandName && (
            <p className="text-xs text-gray-500 mb-1">{product.brandName}</p>
          )}

          {/* Product Name */}
          <h3 className="font-semibold text-gray-900 mb-2 line-clamp-2 group-hover:text-primary-600 transition-colors">
            {product.name}
          </h3>

          {/* Variant */}
          <p className="text-sm text-gray-600 mb-2">{product.defaultVariantName}</p>

          {/* Stock Badge */}
          <div className="mb-3">{getStockBadge()}</div>

          {/* Price */}
          <div className="flex items-baseline space-x-2 mb-3">
            <span className="text-xl font-bold text-gray-900">
              ₹{product.sellingPrice.toFixed(2)}
            </span>
            {product.mrp > product.sellingPrice && (
              <>
                <span className="text-sm text-gray-500 line-through">
                  ₹{product.mrp.toFixed(2)}
                </span>
                <span className="text-sm font-semibold text-green-600">
                  {product.discountPercent.toFixed(0)}% off
                </span>
              </>
            )}
          </div>

          {/* Add to Cart Button */}
          <button
            onClick={handleAddToCart}
            disabled={!product.isAvailable || isAdding}
            className="w-full bg-primary-600 text-white py-2 px-4 rounded-lg hover:bg-primary-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors flex items-center justify-center space-x-2"
          >
            <ShoppingCart className="h-4 w-4" />
            <span>{isAdding ? 'Adding...' : 'Add to Cart'}</span>
          </button>
        </div>
      </div>
    </Link>
  );
};

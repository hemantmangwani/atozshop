export interface VariantInfo {
  id: number;
  name: string;
  sku: string;
  sellingPrice: number;
  mrp: number;
  availableStock: number;
  stockStatus: string;
}

export interface PublicProductResponse {
  id: number;
  sku: string;
  name: string;
  description?: string;
  categoryName?: string;
  brandName?: string;
  defaultVariantId: number;
  defaultVariantName: string;
  sellingPrice: number;
  mrp: number;
  discountPercent: number;
  availableStock: number;
  stockStatus: string;
  isAvailable: boolean;
  variants: VariantInfo[];
}

export interface StockAvailabilityResponse {
  variantId: number;
  sku: string;
  currentStock: number;
  reservedStock: number;
  availableStock: number;
  stockStatus: string;
}

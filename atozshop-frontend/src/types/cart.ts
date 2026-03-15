export interface CartItem {
  variantId: number;
  productName: string;
  variantName: string;
  sku: string;
  quantity: number;
  unitPrice: number;
  mrp: number;
  totalPrice: number;
  availableStock: number;
  image?: string;
}

export interface Cart {
  items: CartItem[];
  totalItems: number;
  totalQuantity: number;
  subtotal: number;
}

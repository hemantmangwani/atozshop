export type OrderStatus =
  | 'NEW'
  | 'ACCEPTED'
  | 'PACKED'
  | 'OUT_FOR_DELIVERY'
  | 'DELIVERED'
  | 'CANCELLED'
  | 'RETURNED';

export type PaymentStatus = 'UNPAID' | 'PARTIAL' | 'PAID' | 'REFUNDED';

export type PaymentMethod = 'COD' | 'ONLINE' | 'WALLET' | 'UPI';

export interface OrderItemRequest {
  variantId: number;
  quantity: number;
}

export interface CreateOrderRequest {
  tenantId: number;
  storeId: number;
  customerId: number;
  deliveryAddressId: number;
  deliverySlot?: string;
  paymentMethod: PaymentMethod;
  items: OrderItemRequest[];
  customerNotes?: string;
}

export interface OrderItemResponse {
  id: number;
  orderId: number;
  variantId: number;
  sku: string;
  productName: string;
  variantName: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  fulfilledQuantity?: number;
  substitutedVariantId?: number;
  substitutedVariantName?: string;
}

export interface OrderResponse {
  id: number;
  tenantId: number;
  storeId: number;
  customerId: number;
  customerName?: string;
  customerPhone?: string;
  orderNumber: string;
  orderDate: string;
  status: OrderStatus;
  paymentStatus: PaymentStatus;
  paymentMethod: PaymentMethod;

  // Items
  items: OrderItemResponse[];
  totalItems: number;
  totalQuantity: number;

  // Amounts
  subtotal: number;
  deliveryFee: number;
  totalAmount: number;

  // Delivery
  deliveryAddress: string;
  deliverySlot?: string;
  customerNotes?: string;

  // Tracking
  placedAt: string;
  acceptedAt?: string;
  packedAt?: string;
  dispatchedAt?: string;
  deliveredAt?: string;
  cancelledAt?: string;

  // Users
  acceptedBy?: number;
  packedBy?: number;
  dispatchedBy?: number;
  deliveredBy?: number;
  cancelledBy?: number;

  // Cancellation
  cancelReason?: string;
}

export interface CancelOrderRequest {
  tenantId: number;
  cancelReason: string;
  cancelledBy: number;
}

export interface OrderSummaryResponse {
  id: number;
  orderNumber: string;
  orderDate: string;
  status: OrderStatus;
  totalAmount: number;
  totalItems: number;
  customerName?: string;
}

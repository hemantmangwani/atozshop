export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

export const API_ENDPOINTS = {
  // Auth
  LOGIN: '/auth/login',
  REGISTER: '/auth/register',

  // Public Products
  PRODUCTS: '/public/products',
  PRODUCT_DETAIL: (id: number) => `/public/products/${id}`,
  PRODUCT_SEARCH: '/public/products/search',
  PRODUCTS_BY_CATEGORY: (categoryId: number) => `/public/products/category/${categoryId}`,
  VARIANT_AVAILABILITY: (variantId: number) => `/public/products/variant/${variantId}/availability`,

  // Orders
  ORDERS: '/orders',
  ORDER_DETAIL: (id: number) => `/orders/${id}`,
  CUSTOMER_ORDERS: (customerId: number) => `/orders/customer/${customerId}`,
  CANCEL_ORDER: (id: number) => `/orders/${id}/cancel`,

  // Admin Orders
  ADMIN_ORDERS: '/admin/orders',
  ADMIN_ORDER_DETAIL: (id: number) => `/admin/orders/${id}`,
  ACCEPT_ORDER: (id: number) => `/admin/orders/${id}/accept`,
  PACK_ORDER: (id: number) => `/admin/orders/${id}/pack`,
  DISPATCH_ORDER: (id: number) => `/admin/orders/${id}/dispatch`,
  DELIVER_ORDER: (id: number) => `/admin/orders/${id}/deliver`,

  // Addresses
  ADDRESSES: '/customers/addresses',
  ADDRESS_DETAIL: (id: number) => `/customers/addresses/${id}`,
  CUSTOMER_ADDRESSES: (customerId: number) => `/customers/addresses/customer/${customerId}`,
  SET_DEFAULT_ADDRESS: (id: number) => `/customers/addresses/${id}/default`,
};

export const STORAGE_KEYS = {
  TOKEN: 'atozshop_token',
  USER: 'atozshop_user',
  CART: 'atozshop_cart',
  TENANT_ID: 'atozshop_tenant_id',
  STORE_ID: 'atozshop_store_id',
};

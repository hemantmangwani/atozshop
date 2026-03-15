import { apiService } from './api';
import { API_ENDPOINTS } from '../constants/api';
import type {
  CreateOrderRequest,
  OrderResponse,
  OrderSummaryResponse,
  CancelOrderRequest,
} from '../types/order';
import { authService } from './authService';

export const orderService = {
  placeOrder: async (request: CreateOrderRequest): Promise<OrderResponse> => {
    const response = await apiService.post<OrderResponse>(
      API_ENDPOINTS.ORDERS,
      request
    );

    return response.data;
  },

  // Get orders for the currently logged-in user
  getMyOrders: async (): Promise<OrderSummaryResponse[]> => {
    const response = await apiService.get<OrderSummaryResponse[]>(
      API_ENDPOINTS.ORDERS
    );

    return response.data;
  },

  // Get orders for a specific customer (admin use)
  getCustomerOrders: async (customerId: number): Promise<OrderSummaryResponse[]> => {
    const params = {
      tenantId: authService.getTenantId(),
    };

    const response = await apiService.get<OrderSummaryResponse[]>(
      API_ENDPOINTS.CUSTOMER_ORDERS(customerId),
      params
    );

    return response.data;
  },

  getOrderById: async (orderId: number): Promise<OrderResponse> => {
    const params = {
      tenantId: authService.getTenantId(),
    };

    const response = await apiService.get<OrderResponse>(
      API_ENDPOINTS.ORDER_DETAIL(orderId),
      params
    );

    return response.data;
  },

  cancelOrder: async (orderId: number, request: CancelOrderRequest): Promise<OrderResponse> => {
    const response = await apiService.post<OrderResponse>(
      API_ENDPOINTS.CANCEL_ORDER(orderId),
      request
    );

    return response.data;
  },

  // Admin methods
  getAllOrders: async (status?: string, startDate?: string, endDate?: string): Promise<OrderSummaryResponse[]> => {
    const params = {
      tenantId: authService.getTenantId(),
      storeId: authService.getStoreId(),
      ...(status && { status }),
      ...(startDate && { startDate }),
      ...(endDate && { endDate }),
    };

    const response = await apiService.get<OrderSummaryResponse[]>(
      API_ENDPOINTS.ADMIN_ORDERS,
      params
    );

    return response.data;
  },

  acceptOrder: async (orderId: number, acceptedBy: number): Promise<OrderResponse> => {
    const params = {
      tenantId: authService.getTenantId(),
      acceptedBy,
    };

    const response = await apiService.post<OrderResponse>(
      API_ENDPOINTS.ACCEPT_ORDER(orderId),
      null,
      { params }
    );

    return response.data;
  },

  packOrder: async (orderId: number, packedBy: number): Promise<OrderResponse> => {
    const params = {
      tenantId: authService.getTenantId(),
      packedBy,
    };

    const response = await apiService.post<OrderResponse>(
      API_ENDPOINTS.PACK_ORDER(orderId),
      null,
      { params }
    );

    return response.data;
  },

  dispatchOrder: async (orderId: number, dispatchedBy: number): Promise<OrderResponse> => {
    const params = {
      tenantId: authService.getTenantId(),
      dispatchedBy,
    };

    const response = await apiService.post<OrderResponse>(
      API_ENDPOINTS.DISPATCH_ORDER(orderId),
      null,
      { params }
    );

    return response.data;
  },

  deliverOrder: async (orderId: number, deliveredBy: number): Promise<OrderResponse> => {
    const params = {
      tenantId: authService.getTenantId(),
      deliveredBy,
    };

    const response = await apiService.post<OrderResponse>(
      API_ENDPOINTS.DELIVER_ORDER(orderId),
      null,
      { params }
    );

    return response.data;
  },
};

import { apiService } from './api';
import { API_ENDPOINTS } from '../constants/api';
import type { PublicProductResponse, StockAvailabilityResponse } from '../types/product';
import { authService } from './authService';

export const productService = {
  getAllProducts: async (categoryId?: number): Promise<PublicProductResponse[]> => {
    const params = {
      tenantId: authService.getTenantId(),
      storeId: authService.getStoreId(),
      ...(categoryId && { categoryId }),
    };

    const response = await apiService.get<PublicProductResponse[]>(
      API_ENDPOINTS.PRODUCTS,
      params
    );

    return response.data;
  },

  getProductById: async (id: number): Promise<PublicProductResponse> => {
    const params = {
      tenantId: authService.getTenantId(),
      storeId: authService.getStoreId(),
    };

    const response = await apiService.get<PublicProductResponse>(
      API_ENDPOINTS.PRODUCT_DETAIL(id),
      params
    );

    return response.data;
  },

  searchProducts: async (keyword: string): Promise<PublicProductResponse[]> => {
    const params = {
      tenantId: authService.getTenantId(),
      storeId: authService.getStoreId(),
      keyword,
    };

    const response = await apiService.get<PublicProductResponse[]>(
      API_ENDPOINTS.PRODUCT_SEARCH,
      params
    );

    return response.data;
  },

  getProductsByCategory: async (categoryId: number): Promise<PublicProductResponse[]> => {
    const params = {
      tenantId: authService.getTenantId(),
      storeId: authService.getStoreId(),
    };

    const response = await apiService.get<PublicProductResponse[]>(
      API_ENDPOINTS.PRODUCTS_BY_CATEGORY(categoryId),
      params
    );

    return response.data;
  },

  checkStockAvailability: async (variantId: number): Promise<StockAvailabilityResponse> => {
    const params = {
      tenantId: authService.getTenantId(),
      storeId: authService.getStoreId(),
    };

    const response = await apiService.get<StockAvailabilityResponse>(
      API_ENDPOINTS.VARIANT_AVAILABILITY(variantId),
      params
    );

    return response.data;
  },
};

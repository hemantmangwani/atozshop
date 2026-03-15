import { apiService } from './api';
import { authService } from './authService';

export interface Discount {
  id: number;
  tenantId: number;
  discountCode: string;
  name: string;
  description?: string;
  discountType: 'PERCENTAGE' | 'FIXED_AMOUNT';
  discountValue: number;
  minPurchaseAmount?: number;
  maxDiscountAmount?: number;
  applicableOn: 'ITEM' | 'BILL' | 'CATEGORY';
  validFrom?: string;
  validTo?: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateDiscountRequest {
  tenantId: number;
  discountCode: string;
  name: string;
  description?: string;
  discountType: 'PERCENTAGE' | 'FIXED_AMOUNT';
  discountValue: number;
  minPurchaseAmount?: number;
  maxDiscountAmount?: number;
  applicableOn: 'ITEM' | 'BILL' | 'CATEGORY';
  validFrom?: string;
  validTo?: string;
  isActive: boolean;
}

export interface UpdateDiscountRequest {
  name?: string;
  description?: string;
  discountValue?: number;
  minPurchaseAmount?: number;
  maxDiscountAmount?: number;
  validFrom?: string;
  validTo?: string;
  isActive?: boolean;
}

export const discountService = {
  // Get all discounts
  getAllDiscounts: async (): Promise<Discount[]> => {
    const params = {
      tenantId: authService.getTenantId(),
    };

    const response = await apiService.get<Discount[]>(
      '/discounts',
      params
    );

    return response.data;
  },

  // Get active discounts
  getActiveDiscounts: async (): Promise<Discount[]> => {
    const params = {
      tenantId: authService.getTenantId(),
    };

    const response = await apiService.get<Discount[]>(
      '/discounts/active',
      params
    );

    return response.data;
  },

  // Get discount by ID
  getDiscountById: async (id: number): Promise<Discount> => {
    const params = {
      tenantId: authService.getTenantId(),
    };

    const response = await apiService.get<Discount>(
      `/api/v1/discounts/${id}`,
      params
    );

    return response.data;
  },

  // Get discount by code
  getDiscountByCode: async (code: string): Promise<Discount> => {
    const params = {
      tenantId: authService.getTenantId(),
    };

    const response = await apiService.get<Discount>(
      `/api/v1/discounts/code/${code}`,
      params
    );

    return response.data;
  },

  // Create discount
  createDiscount: async (data: CreateDiscountRequest): Promise<Discount> => {
    const request = {
      ...data,
      tenantId: authService.getTenantId(),
    };

    const response = await apiService.post<Discount>(
      '/discounts',
      request
    );

    return response.data;
  },

  // Update discount
  updateDiscount: async (id: number, data: UpdateDiscountRequest): Promise<Discount> => {
    const params = {
      tenantId: authService.getTenantId(),
    };

    const response = await apiService.put<Discount>(
      `/api/v1/discounts/${id}`,
      data,
      params
    );

    return response.data;
  },

  // Delete discount
  deleteDiscount: async (id: number): Promise<void> => {
    const params = {
      tenantId: authService.getTenantId(),
    };

    await apiService.delete(
      `/api/v1/discounts/${id}`,
      params
    );
  },

  // Toggle discount active status
  toggleDiscountStatus: async (id: number, isActive: boolean): Promise<Discount> => {
    return this.updateDiscount(id, { isActive });
  },
};

import { apiService } from './api';
import { authService } from './authService';

export interface Supplier {
  id: number;
  tenantId: number;
  code: string;
  name: string;
  contactPerson?: string;
  phone?: string;
  email?: string;
  address?: string;
  city?: string;
  state?: string;
  postalCode?: string;
  country?: string;
  gstNumber?: string;
  panNumber?: string;
  bankName?: string;
  bankAccountNumber?: string;
  bankIfscCode?: string;
  supplierType: 'LOCAL' | 'NATIONAL' | 'INTERNATIONAL';
  isActive: boolean;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateSupplierRequest {
  name: string;
  contactPerson?: string;
  phone?: string;
  email?: string;
  address?: string;
  city?: string;
  state?: string;
  postalCode?: string;
  country?: string;
  gstNumber?: string;
  panNumber?: string;
  bankName?: string;
  bankAccountNumber?: string;
  bankIfscCode?: string;
  supplierType?: 'LOCAL' | 'NATIONAL' | 'INTERNATIONAL';
  isActive?: boolean;
  notes?: string;
}

export interface UpdateSupplierRequest {
  name?: string;
  contactPerson?: string;
  phone?: string;
  email?: string;
  address?: string;
  city?: string;
  state?: string;
  postalCode?: string;
  country?: string;
  gstNumber?: string;
  panNumber?: string;
  bankName?: string;
  bankAccountNumber?: string;
  bankIfscCode?: string;
  supplierType?: 'LOCAL' | 'NATIONAL' | 'INTERNATIONAL';
  isActive?: boolean;
  notes?: string;
}

export const supplierService = {
  // Get all suppliers for a tenant
  getAllSuppliers: async (): Promise<Supplier[]> => {
    const params = {
      tenantId: authService.getTenantId(),
    };

    const response = await apiService.get<Supplier[]>('/suppliers', params);
    return response.data;
  },

  // Get supplier by ID
  getSupplierById: async (id: number): Promise<Supplier> => {
    const params = {
      tenantId: authService.getTenantId(),
    };

    const response = await apiService.get<Supplier>(`/suppliers/${id}`, params);
    return response.data;
  },

  // Get supplier by code
  getSupplierByCode: async (code: string): Promise<Supplier> => {
    const params = {
      tenantId: authService.getTenantId(),
    };

    const response = await apiService.get<Supplier>(`/suppliers/code/${code}`, params);
    return response.data;
  },

  // Search suppliers by name
  searchSuppliers: async (keyword: string): Promise<Supplier[]> => {
    const params = {
      tenantId: authService.getTenantId(),
      keyword,
    };

    const response = await apiService.get<Supplier[]>('/suppliers/search', params);
    return response.data;
  },

  // Create new supplier
  createSupplier: async (data: CreateSupplierRequest): Promise<Supplier> => {
    const requestData = {
      ...data,
      tenantId: authService.getTenantId(),
    };

    const response = await apiService.post<Supplier>('/suppliers', requestData);
    return response.data;
  },

  // Update supplier
  updateSupplier: async (id: number, data: UpdateSupplierRequest): Promise<Supplier> => {
    const params = {
      tenantId: authService.getTenantId(),
    };

    const response = await apiService.put<Supplier>(`/suppliers/${id}`, data, params);
    return response.data;
  },

  // Delete supplier
  deleteSupplier: async (id: number): Promise<void> => {
    const params = {
      tenantId: authService.getTenantId(),
    };

    await apiService.delete(`/suppliers/${id}`, params);
  },

  // Get active suppliers only
  getActiveSuppliers: async (): Promise<Supplier[]> => {
    const allSuppliers = await supplierService.getAllSuppliers();
    return allSuppliers.filter((s) => s.isActive);
  },
};

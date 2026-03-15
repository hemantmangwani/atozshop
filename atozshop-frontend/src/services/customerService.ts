import { apiService } from './api';
import { authService } from './authService';

export interface Customer {
  id: number;
  tenantId: number;
  customerCode: string;
  name: string;
  phone: string;
  email?: string;
  address?: string;
  city?: string;
  state?: string;
  postalCode?: string;
  gstin?: string;
  loyaltyPoints: number;
  totalPurchases: number;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateCustomerRequest {
  tenantId: number;
  name: string;
  phone: string;
  email?: string;
  address?: string;
  city?: string;
  state?: string;
  postalCode?: string;
  gstin?: string;
}

export interface UpdateCustomerRequest {
  name?: string;
  phone?: string;
  email?: string;
  address?: string;
  city?: string;
  state?: string;
  postalCode?: string;
  gstin?: string;
  isActive?: boolean;
}

export interface CustomerPurchaseHistory {
  customerId: number;
  customerName: string;
  totalBills: number;
  totalPurchases: number;
  lastPurchaseDate?: string;
  recentBills: any[];
}

export const customerService = {
  // Get all customers
  getAllCustomers: async (): Promise<Customer[]> => {
    const params = {
      tenantId: authService.getTenantId(),
    };

    const response = await apiService.get<Customer[]>(
      '/customers',
      params
    );

    return response.data;
  },

  // Search customers by keyword
  searchCustomers: async (keyword: string): Promise<Customer[]> => {
    const params = {
      tenantId: authService.getTenantId(),
      keyword,
    };

    const response = await apiService.get<Customer[]>(
      '/customers/search',
      params
    );

    return response.data;
  },

  // Get customer by ID
  getCustomerById: async (id: number): Promise<Customer> => {
    const params = {
      tenantId: authService.getTenantId(),
    };

    const response = await apiService.get<Customer>(
      `/api/v1/customers/${id}`,
      params
    );

    return response.data;
  },

  // Get customer by phone
  getCustomerByPhone: async (phone: string): Promise<Customer> => {
    const params = {
      tenantId: authService.getTenantId(),
    };

    const response = await apiService.get<Customer>(
      `/api/v1/customers/phone/${phone}`,
      params
    );

    return response.data;
  },

  // Create new customer
  createCustomer: async (data: CreateCustomerRequest): Promise<Customer> => {
    const request = {
      ...data,
      tenantId: authService.getTenantId(),
    };

    const response = await apiService.post<Customer>(
      '/customers',
      request
    );

    return response.data;
  },

  // Update customer
  updateCustomer: async (id: number, data: UpdateCustomerRequest): Promise<Customer> => {
    const params = {
      tenantId: authService.getTenantId(),
    };

    const response = await apiService.put<Customer>(
      `/api/v1/customers/${id}`,
      data,
      params
    );

    return response.data;
  },

  // Delete customer
  deleteCustomer: async (id: number): Promise<void> => {
    const params = {
      tenantId: authService.getTenantId(),
    };

    await apiService.delete(
      `/api/v1/customers/${id}`,
      params
    );
  },

  // Get customer purchase history
  getPurchaseHistory: async (id: number): Promise<CustomerPurchaseHistory> => {
    const params = {
      tenantId: authService.getTenantId(),
    };

    const response = await apiService.get<CustomerPurchaseHistory>(
      `/api/v1/customers/${id}/purchase-history`,
      params
    );

    return response.data;
  },

  /**
   * Get customer ID for the current logged-in user
   * This will get or create a customer record for the logged-in user
   */
  getCurrentUserCustomerId: async (): Promise<number> => {
    const user = authService.getCurrentUser();
    if (!user || !user.email) {
      throw new Error('No user logged in');
    }

    // Call the backend to get or create customer record for the current user
    const response = await apiService.get<Customer>('/customers/me');
    return response.data.id;
  },
};

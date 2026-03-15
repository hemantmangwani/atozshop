import { apiService } from './api';
import { API_ENDPOINTS, STORAGE_KEYS } from '../constants/api';
import type { LoginRequest, LoginResponse, User } from '../types/auth';

export const authService = {
  login: async (credentials: LoginRequest): Promise<LoginResponse> => {
    const response = await apiService.post<LoginResponse>(
      API_ENDPOINTS.LOGIN,
      credentials
    );

    const loginData = response.data;

    // Store token
    localStorage.setItem(STORAGE_KEYS.TOKEN, loginData.token);

    // Store user info (excluding token)
    const { token, ...userInfo } = loginData;
    localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(userInfo));

    // Store tenant and store IDs for convenience
    localStorage.setItem(STORAGE_KEYS.TENANT_ID, loginData.tenantId.toString());
    // Default store ID to 1 (can be changed based on user preference)
    localStorage.setItem(STORAGE_KEYS.STORE_ID, '1');

    return loginData;
  },

  logout: () => {
    localStorage.removeItem(STORAGE_KEYS.TOKEN);
    localStorage.removeItem(STORAGE_KEYS.USER);
    localStorage.removeItem(STORAGE_KEYS.CART);
  },

  getCurrentUser: (): User | null => {
    const userStr = localStorage.getItem(STORAGE_KEYS.USER);
    if (!userStr) return null;

    try {
      return JSON.parse(userStr);
    } catch {
      return null;
    }
  },

  isAuthenticated: (): boolean => {
    return !!localStorage.getItem(STORAGE_KEYS.TOKEN);
  },

  getTenantId: (): number => {
    return parseInt(localStorage.getItem(STORAGE_KEYS.TENANT_ID) || '1', 10);
  },

  getStoreId: (): number => {
    return parseInt(localStorage.getItem(STORAGE_KEYS.STORE_ID) || '1', 10);
  },
};

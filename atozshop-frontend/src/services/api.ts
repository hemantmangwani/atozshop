import axios from 'axios';
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios';
import { API_BASE_URL, STORAGE_KEYS } from '../constants/api';

class ApiService {
  private api: AxiosInstance;

  constructor() {
    this.api = axios.create({
      baseURL: API_BASE_URL,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    this.setupInterceptors();
  }

  private setupInterceptors() {
    // Request interceptor - Add auth token
    this.api.interceptors.request.use(
      (config: InternalAxiosRequestConfig) => {
        const token = localStorage.getItem(STORAGE_KEYS.TOKEN);
        if (token && config.headers) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
      },
      (error) => {
        return Promise.reject(error);
      }
    );

    // Response interceptor - Handle errors
    this.api.interceptors.response.use(
      (response: AxiosResponse) => response,
      (error) => {
        if (error.response) {
          // Server responded with error status
          const { status } = error.response;

          if (status === 401) {
            // Unauthorized - clear token and redirect to login
            localStorage.removeItem(STORAGE_KEYS.TOKEN);
            localStorage.removeItem(STORAGE_KEYS.USER);
            window.location.href = '/login';
          }

          // Return formatted error
          return Promise.reject({
            status,
            message: error.response.data?.message || 'An error occurred',
            data: error.response.data,
          });
        } else if (error.request) {
          // Request made but no response
          return Promise.reject({
            message: 'Network error. Please check your connection.',
          });
        } else {
          // Something else happened
          return Promise.reject({
            message: error.message || 'An unexpected error occurred',
          });
        }
      }
    );
  }

  public getInstance(): AxiosInstance {
    return this.api;
  }

  // Utility methods
  public get<T>(url: string, params?: any): Promise<AxiosResponse<T>> {
    return this.api.get<T>(url, { params });
  }

  public post<T>(url: string, data?: any, config?: any): Promise<AxiosResponse<T>> {
    return this.api.post<T>(url, data, config);
  }

  public put<T>(url: string, data?: any): Promise<AxiosResponse<T>> {
    return this.api.put<T>(url, data);
  }

  public delete<T>(url: string, params?: any): Promise<AxiosResponse<T>> {
    return this.api.delete<T>(url, { params });
  }
}

export const apiService = new ApiService();
export default apiService.getInstance();

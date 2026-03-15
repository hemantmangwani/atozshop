import { apiService } from './api';
import { API_ENDPOINTS } from '../constants/api';
import type {
  AddAddressRequest,
  UpdateAddressRequest,
  AddressResponse,
} from '../types/address';

export const addressService = {
  addAddress: async (request: AddAddressRequest): Promise<AddressResponse> => {
    const response = await apiService.post<AddressResponse>(
      API_ENDPOINTS.ADDRESSES,
      request
    );

    return response.data;
  },

  getCustomerAddresses: async (customerId: number): Promise<AddressResponse[]> => {
    const response = await apiService.get<AddressResponse[]>(
      API_ENDPOINTS.CUSTOMER_ADDRESSES(customerId)
    );

    return response.data;
  },

  getAddressById: async (id: number, customerId: number): Promise<AddressResponse> => {
    const response = await apiService.get<AddressResponse>(
      API_ENDPOINTS.ADDRESS_DETAIL(id),
      { customerId }
    );

    return response.data;
  },

  updateAddress: async (
    id: number,
    customerId: number,
    request: UpdateAddressRequest
  ): Promise<AddressResponse> => {
    const response = await apiService.put<AddressResponse>(
      API_ENDPOINTS.ADDRESS_DETAIL(id) + `?customerId=${customerId}`,
      request
    );

    return response.data;
  },

  deleteAddress: async (id: number, customerId: number): Promise<void> => {
    await apiService.delete(
      API_ENDPOINTS.ADDRESS_DETAIL(id),
      { customerId }
    );
  },

  setDefaultAddress: async (id: number, customerId: number): Promise<AddressResponse> => {
    const response = await apiService.put<AddressResponse>(
      API_ENDPOINTS.SET_DEFAULT_ADDRESS(id) + `?customerId=${customerId}`
    );

    return response.data;
  },
};

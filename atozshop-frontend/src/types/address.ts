export type AddressType = 'HOME' | 'WORK' | 'OTHER';
export type PaymentMethod = 'COD' | 'ONLINE' | 'WALLET' | 'UPI';

export interface AddAddressRequest {
  customerId: number;
  addressType?: AddressType;
  addressLine1: string;
  addressLine2?: string;
  landmark?: string;
  city: string;
  state: string;
  postalCode: string;
  country?: string;
  phone: string;
  isDefault?: boolean;
}

export interface UpdateAddressRequest {
  addressType?: AddressType;
  addressLine1?: string;
  addressLine2?: string;
  landmark?: string;
  city?: string;
  state?: string;
  postalCode?: string;
  country?: string;
  phone?: string;
  isDefault?: boolean;
}

export interface AddressResponse {
  id: number;
  customerId: number;
  addressType?: AddressType;
  addressLine1: string;
  addressLine2?: string;
  landmark?: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
  phone: string;
  isDefault: boolean;
  createdAt: string;
  updatedAt: string;
}

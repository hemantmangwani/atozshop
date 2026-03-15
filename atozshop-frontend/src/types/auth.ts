export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  type: string;
  id: number;
  email: string;
  username: string;
  fullName: string;
  tenantId: number;
  roles: string[];
  customerId?: number; // Customer ID from customers table (for online orders)
}

export interface User {
  id: number;
  username: string;
  email: string;
  fullName: string;
  role: string; // Primary role (first from roles array)
  roles: string[];
  tenantId: number;
  customerId?: number; // Customer ID from customers table (for online orders)
}

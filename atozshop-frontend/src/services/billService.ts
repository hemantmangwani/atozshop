import { apiService } from './api';
import { authService } from './authService';

export interface Bill {
  id: number;
  tenantId: number;
  storeId: number;
  customerId?: number;
  cashierId: number;
  billNumber: string;
  billDate: string;
  billType: 'SALES' | 'SALES_RETURN';
  totalItems: number;
  totalQuantity: number;
  subtotal: number;
  discountAmount: number;
  taxAmount: number;
  totalAmount: number;
  paidAmount: number;
  balanceAmount: number;
  status: 'DRAFT' | 'CONFIRMED' | 'CANCELLED';
  paymentStatus: 'UNPAID' | 'PARTIAL' | 'PAID' | 'REFUNDED';
  notes?: string;
  items: BillItem[];
  payments: Payment[];
  discounts: BillDiscount[];
  customer?: {
    id: number;
    name: string;
    phone: string;
    customerCode: string;
  };
}

export interface BillItem {
  id: number;
  billId: number;
  variantId: number;
  sku: string;
  productName: string;
  variantName: string;
  quantity: number;
  unitPrice: number;
  mrp: number;
  discountPercent: number;
  discountAmount: number;
  subtotal: number;
  taxPercent: number;
  taxAmount: number;
  totalAmount: number;
}

export interface Payment {
  id: number;
  billId: number;
  paymentMethod: 'CASH' | 'CARD' | 'UPI' | 'WALLET' | 'CHEQUE';
  paymentDate: string;
  amount: number;
  referenceNumber?: string;
  cardLast4?: string;
  upiId?: string;
  bankName?: string;
  notes?: string;
}

export interface BillDiscount {
  id: number;
  billId: number;
  discountId?: number;
  discountName: string;
  discountCode?: string;
  discountType: 'PERCENTAGE' | 'FIXED_AMOUNT';
  discountValue: number;
  discountAmount: number;
}

export interface CreateBillRequest {
  tenantId: number;
  storeId: number;
  customerId?: number;
  cashierId: number;
  billType: 'SALES' | 'SALES_RETURN';
  items: AddBillItemRequest[];
  notes?: string;
}

export interface AddBillItemRequest {
  variantId: number;
  quantity: number;
  discountPercent?: number;
}

export interface CreatePaymentRequest {
  billId: number;
  tenantId: number;
  paymentMethod: 'CASH' | 'CARD' | 'UPI' | 'WALLET' | 'CHEQUE';
  amount: number;
  paymentDate: string;
  referenceNumber?: string;
  cardLast4?: string;
  upiId?: string;
  bankName?: string;
  notes?: string;
}

export interface ApplyDiscountRequest {
  discountId?: number;
  discountCode?: string;
  discountType: 'PERCENTAGE' | 'FIXED_AMOUNT';
  discountValue: number;
  tenantId: number;
}

export const billService = {
  // Create new bill
  createBill: async (data: CreateBillRequest): Promise<Bill> => {
    const request = {
      ...data,
      tenantId: authService.getTenantId(),
      storeId: authService.getStoreId(),
      cashierId: authService.getUserId(),
    };

    const response = await apiService.post<Bill>(
      '/bills',
      request
    );

    return response.data;
  },

  // Get all bills
  getAllBills: async (status?: string): Promise<Bill[]> => {
    const params: any = {
      tenantId: authService.getTenantId(),
      storeId: authService.getStoreId(),
    };

    if (status) {
      params.status = status;
    }

    const response = await apiService.get<Bill[]>(
      '/bills',
      params
    );

    return response.data;
  },

  // Get bill by ID
  getBillById: async (id: number): Promise<Bill> => {
    const params = {
      tenantId: authService.getTenantId(),
    };

    const response = await apiService.get<Bill>(
      `/api/v1/bills/${id}`,
      params
    );

    return response.data;
  },

  // Get bill by number
  getBillByNumber: async (billNumber: string): Promise<Bill> => {
    const params = {
      tenantId: authService.getTenantId(),
    };

    const response = await apiService.get<Bill>(
      `/api/v1/bills/number/${billNumber}`,
      params
    );

    return response.data;
  },

  // Add item to bill
  addItemToBill: async (billId: number, item: AddBillItemRequest): Promise<Bill> => {
    const response = await apiService.post<Bill>(
      `/api/v1/bills/${billId}/items`,
      item
    );

    return response.data;
  },

  // Update bill item
  updateBillItem: async (billId: number, itemId: number, quantity: number): Promise<Bill> => {
    const response = await apiService.put<Bill>(
      `/api/v1/bills/${billId}/items/${itemId}`,
      { quantity }
    );

    return response.data;
  },

  // Remove bill item
  removeBillItem: async (billId: number, itemId: number): Promise<Bill> => {
    await apiService.delete(
      `/api/v1/bills/${billId}/items/${itemId}`
    );

    return this.getBillById(billId);
  },

  // Apply discount to bill
  applyDiscount: async (billId: number, discount: ApplyDiscountRequest): Promise<Bill> => {
    const request = {
      ...discount,
      tenantId: authService.getTenantId(),
    };

    const response = await apiService.post<Bill>(
      `/api/v1/bills/${billId}/discounts`,
      request
    );

    return response.data;
  },

  // Process payment
  processPayment: async (payment: CreatePaymentRequest): Promise<Payment> => {
    const request = {
      ...payment,
      tenantId: authService.getTenantId(),
    };

    const response = await apiService.post<Payment>(
      '/payments',
      request
    );

    return response.data;
  },

  // Confirm bill (deduct stock)
  confirmBill: async (billId: number): Promise<Bill> => {
    const params = {
      tenantId: authService.getTenantId(),
    };

    const response = await apiService.post<Bill>(
      `/api/v1/bills/${billId}/confirm`,
      {},
      params
    );

    return response.data;
  },

  // Cancel bill
  cancelBill: async (billId: number): Promise<Bill> => {
    const params = {
      tenantId: authService.getTenantId(),
    };

    const response = await apiService.post<Bill>(
      `/api/v1/bills/${billId}/cancel`,
      {},
      params
    );

    return response.data;
  },

  // Get receipt
  getReceipt: async (billId: number): Promise<any> => {
    const params = {
      tenantId: authService.getTenantId(),
    };

    const response = await apiService.get(
      `/api/v1/bills/${billId}/receipt`,
      params
    );

    return response.data;
  },
};

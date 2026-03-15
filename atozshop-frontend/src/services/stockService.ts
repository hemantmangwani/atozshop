import { apiService } from './api';
import { authService } from './authService';

export interface StockLedgerEntry {
  id: number;
  variantId: number;
  storeId: number;
  transactionType: 'PURCHASE' | 'SALE' | 'ADJUSTMENT' | 'RETURN';
  referenceId?: number;
  quantityChange: number;
  costPrice: number;
  sellingPrice: number;
  remarks?: string;
  transactionDate: string;
  performedBy: number;
  createdAt: string;
}

export interface IncomingStockItem {
  variantId: number;
  quantity: number;
  costPrice: number;
  sellingPrice: number;
}

export interface IncomingStockRequest {
  tenantId: number;
  storeId: number;
  supplierId?: number;
  transactionDate: string;
  referenceNumber?: string;
  items: IncomingStockItem[];
  receivedBy: number;
}

export interface StockAdjustmentRequest {
  tenantId: number;
  storeId: number;
  variantId: number;
  quantityChange: number;
  reason: string;
  adjustedBy: number;
}

export interface CurrentStockResponse {
  variantId: number;
  sku: string;
  productName: string;
  variantName: string;
  currentStock: number;
  reservedStock: number;
  availableStock: number;
  costPrice: number;
  sellingPrice: number;
  reorderLevel?: number;
}

export interface LowStockItem {
  variantId: number;
  sku: string;
  productName: string;
  variantName: string;
  currentStock: number;
  reorderLevel: number;
  stockStatus: 'LOW' | 'OUT_OF_STOCK';
}

export const stockService = {
  // Get stock ledger (all entries)
  getStockLedger: async (variantId?: number): Promise<StockLedgerEntry[]> => {
    const params: any = {
      tenantId: authService.getTenantId(),
      storeId: authService.getStoreId(),
    };

    if (variantId) {
      params.variantId = variantId;
    }

    const response = await apiService.get<StockLedgerEntry[]>(
      '/stock/ledger',
      params
    );

    return response.data;
  },

  // Get current stock for all variants
  getCurrentStock: async (): Promise<CurrentStockResponse[]> => {
    const params = {
      tenantId: authService.getTenantId(),
      storeId: authService.getStoreId(),
    };

    const response = await apiService.get<CurrentStockResponse[]>(
      '/stock/current',
      params
    );

    return response.data;
  },

  // Get current stock for specific variant
  getVariantStock: async (variantId: number): Promise<CurrentStockResponse> => {
    const params = {
      tenantId: authService.getTenantId(),
      storeId: authService.getStoreId(),
    };

    const response = await apiService.get<CurrentStockResponse>(
      `/api/v1/stock/variant/${variantId}`,
      params
    );

    return response.data;
  },

  // Add incoming stock (purchase/receive)
  addIncomingStock: async (data: IncomingStockRequest): Promise<any> => {
    const request = {
      ...data,
      tenantId: authService.getTenantId(),
      storeId: authService.getStoreId(),
      receivedBy: authService.getUserId(),
    };

    const response = await apiService.post(
      '/stock/incoming',
      request
    );

    return response.data;
  },

  // Adjust stock (manual adjustment)
  adjustStock: async (data: StockAdjustmentRequest): Promise<any> => {
    const request = {
      ...data,
      tenantId: authService.getTenantId(),
      storeId: authService.getStoreId(),
      adjustedBy: authService.getUserId(),
    };

    const response = await apiService.post(
      '/stock/adjustment',
      request
    );

    return response.data;
  },

  // Get low stock alerts
  getLowStockAlerts: async (): Promise<LowStockItem[]> => {
    const params = {
      tenantId: authService.getTenantId(),
      storeId: authService.getStoreId(),
    };

    const response = await apiService.get<LowStockItem[]>(
      '/stock/low-stock',
      params
    );

    return response.data;
  },

  // Get stock movements by date range
  getStockMovements: async (fromDate: string, toDate: string): Promise<StockLedgerEntry[]> => {
    const params = {
      tenantId: authService.getTenantId(),
      storeId: authService.getStoreId(),
      fromDate,
      toDate,
    };

    const response = await apiService.get<StockLedgerEntry[]>(
      '/stock/movements',
      params
    );

    return response.data;
  },
};

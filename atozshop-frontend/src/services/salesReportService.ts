import { apiService } from './api';
import { authService } from './authService';

export interface DailySalesReport {
  date: string;
  totalSales: number;
  transactionCount: number;
  totalItemsSold: number;
  averageOrderValue: number;
  paymentMethodBreakdown: PaymentMethodSummary[];
}

export interface PaymentMethodSummary {
  paymentMethod: string;
  amount: number;
  count: number;
}

export interface SalesSummary {
  totalRevenue: number;
  totalTransactions: number;
  averageTransactionValue: number;
  topSellingProducts: TopSellingProduct[];
}

export interface TopSellingProduct {
  variantId: number;
  sku: string;
  productName: string;
  variantName: string;
  quantitySold: number;
  revenue: number;
}

export const salesReportService = {
  // Get daily sales report
  getDailySalesReport: async (date: string): Promise<DailySalesReport> => {
    const params = {
      tenantId: authService.getTenantId(),
      storeId: authService.getStoreId(),
      date,
    };

    const response = await apiService.get<DailySalesReport>(
      '/reports/sales/daily',
      params
    );

    return response.data;
  },

  // Get sales report for date range
  getSalesReportRange: async (fromDate: string, toDate: string): Promise<DailySalesReport[]> => {
    const params = {
      tenantId: authService.getTenantId(),
      storeId: authService.getStoreId(),
      fromDate,
      toDate,
    };

    const response = await apiService.get<DailySalesReport[]>(
      '/reports/sales/range',
      params
    );

    return response.data;
  },

  // Get sales summary
  getSalesSummary: async (fromDate?: string, toDate?: string): Promise<SalesSummary> => {
    const params: any = {
      tenantId: authService.getTenantId(),
      storeId: authService.getStoreId(),
    };

    if (fromDate) params.fromDate = fromDate;
    if (toDate) params.toDate = toDate;

    const response = await apiService.get<SalesSummary>(
      '/reports/sales/summary',
      params
    );

    return response.data;
  },

  // Get payment method breakdown
  getPaymentBreakdown: async (fromDate: string, toDate: string): Promise<PaymentMethodSummary[]> => {
    const params = {
      tenantId: authService.getTenantId(),
      storeId: authService.getStoreId(),
      fromDate,
      toDate,
    };

    const response = await apiService.get<PaymentMethodSummary[]>(
      '/reports/payments/breakdown',
      params
    );

    return response.data;
  },

  // Get top customers
  getTopCustomers: async (limit: number = 10): Promise<any[]> => {
    const params = {
      tenantId: authService.getTenantId(),
      limit,
    };

    const response = await apiService.get<any[]>(
      '/reports/top-customers',
      params
    );

    return response.data;
  },
};

import { apiService } from './api';
import { authService } from './authService';

export interface Category {
  id: number;
  tenantId: number;
  parentId?: number;
  name: string;
  slug: string;
  description?: string;
  displayOrder: number;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
  children?: Category[];
}

export interface CreateCategoryRequest {
  tenantId: number;
  parentId?: number;
  name: string;
  description?: string;
  displayOrder?: number;
}

export interface UpdateCategoryRequest {
  name?: string;
  description?: string;
  parentId?: number;
  displayOrder?: number;
  isActive?: boolean;
}

export const categoryService = {
  // Get all categories
  getAllCategories: async (): Promise<Category[]> => {
    const params = {
      tenantId: authService.getTenantId(),
    };

    const response = await apiService.get<Category[]>(
      '/categories',
      params
    );

    return response.data;
  },

  // Get category by ID
  getCategoryById: async (id: number): Promise<Category> => {
    const params = {
      tenantId: authService.getTenantId(),
    };

    const response = await apiService.get<Category>(
      `/api/v1/categories/${id}`,
      params
    );

    return response.data;
  },

  // Get category hierarchy (tree structure)
  getCategoryHierarchy: async (): Promise<Category[]> => {
    const params = {
      tenantId: authService.getTenantId(),
    };

    const response = await apiService.get<Category[]>(
      '/categories/hierarchy',
      params
    );

    return response.data;
  },

  // Create new category
  createCategory: async (data: CreateCategoryRequest): Promise<Category> => {
    const request = {
      ...data,
      tenantId: authService.getTenantId(),
    };

    const response = await apiService.post<Category>(
      '/categories',
      request
    );

    return response.data;
  },

  // Update category
  updateCategory: async (id: number, data: UpdateCategoryRequest): Promise<Category> => {
    const params = {
      tenantId: authService.getTenantId(),
    };

    const response = await apiService.put<Category>(
      `/api/v1/categories/${id}`,
      data,
      params
    );

    return response.data;
  },

  // Delete category
  deleteCategory: async (id: number): Promise<void> => {
    const params = {
      tenantId: authService.getTenantId(),
    };

    await apiService.delete(
      `/api/v1/categories/${id}`,
      params
    );
  },

  // Get subcategories
  getSubcategories: async (parentId: number): Promise<Category[]> => {
    const params = {
      tenantId: authService.getTenantId(),
    };

    const response = await apiService.get<Category[]>(
      `/api/v1/categories/${parentId}/subcategories`,
      params
    );

    return response.data;
  },
};

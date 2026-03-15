import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { MainLayout } from '../../../components/layout/MainLayout';
import { categoryService } from '../../../services/categoryService';
import { Plus, Edit, Trash2, Folder, FolderOpen } from 'lucide-react';
import toast from 'react-hot-toast';
import { CategoryFormModal } from './CategoryFormModal';

export const CategoriesPage = () => {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingCategory, setEditingCategory] = useState<any>(null);
  const queryClient = useQueryClient();

  const { data: categories, isLoading } = useQuery({
    queryKey: ['categories'],
    queryFn: () => categoryService.getAllCategories(),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => categoryService.deleteCategory(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['categories'] });
      toast.success('Category deleted successfully');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to delete category');
    },
  });

  const handleDelete = (id: number, name: string) => {
    if (window.confirm(`Are you sure you want to delete category "${name}"?`)) {
      deleteMutation.mutate(id);
    }
  };

  const handleEdit = (category: any) => {
    setEditingCategory(category);
    setIsModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setEditingCategory(null);
  };

  const rootCategories = categories?.filter((cat: any) => !cat.parentId) || [];
  const getSubcategories = (parentId: number) => {
    return categories?.filter((cat: any) => cat.parentId === parentId) || [];
  };

  return (
    <MainLayout>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Categories</h1>
            <p className="text-gray-600 mt-1">Manage product categories and hierarchy</p>
          </div>
          <button
            onClick={() => setIsModalOpen(true)}
            className="flex items-center gap-2 px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700"
          >
            <Plus className="h-5 w-5" />
            Add Category
          </button>
        </div>

        {/* Stats */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="bg-white rounded-lg shadow p-6">
            <p className="text-sm text-gray-600">Total Categories</p>
            <p className="text-3xl font-bold text-gray-900">{categories?.length || 0}</p>
          </div>
          <div className="bg-white rounded-lg shadow p-6">
            <p className="text-sm text-gray-600">Root Categories</p>
            <p className="text-3xl font-bold text-primary-600">{rootCategories.length}</p>
          </div>
          <div className="bg-white rounded-lg shadow p-6">
            <p className="text-sm text-gray-600">Active Categories</p>
            <p className="text-3xl font-bold text-green-600">
              {categories?.filter((c: any) => c.isActive).length || 0}
            </p>
          </div>
        </div>

        {/* Categories List */}
        <div className="bg-white rounded-lg shadow">
          <div className="px-6 py-4 border-b border-gray-200">
            <h2 className="text-lg font-semibold text-gray-900">Category Hierarchy</h2>
          </div>

          {isLoading ? (
            <div className="p-12 text-center text-gray-500">Loading categories...</div>
          ) : categories && categories.length > 0 ? (
            <div className="divide-y divide-gray-200">
              {rootCategories.map((category: any) => (
                <CategoryRow
                  key={category.id}
                  category={category}
                  subcategories={getSubcategories(category.id)}
                  onEdit={handleEdit}
                  onDelete={handleDelete}
                  level={0}
                />
              ))}
            </div>
          ) : (
            <div className="p-12 text-center">
              <Folder className="h-16 w-16 mx-auto text-gray-400 mb-4" />
              <p className="text-gray-500">No categories found</p>
            </div>
          )}
        </div>

        {/* Modal */}
        {isModalOpen && (
          <CategoryFormModal
            category={editingCategory}
            categories={categories || []}
            onClose={handleCloseModal}
          />
        )}
      </div>
    </MainLayout>
  );
};

interface CategoryRowProps {
  category: any;
  subcategories: any[];
  onEdit: (category: any) => void;
  onDelete: (id: number, name: string) => void;
  level: number;
}

const CategoryRow = ({ category, subcategories, onEdit, onDelete, level }: CategoryRowProps) => {
  const [isExpanded, setIsExpanded] = useState(false);
  const hasChildren = subcategories.length > 0;

  return (
    <>
      <div
        className="px-6 py-4 hover:bg-gray-50 flex items-center justify-between"
        style={{ paddingLeft: `${24 + level * 32}px` }}
      >
        <div className="flex items-center gap-3 flex-1">
          {hasChildren ? (
            <button
              onClick={() => setIsExpanded(!isExpanded)}
              className="p-1 hover:bg-gray-200 rounded"
            >
              {isExpanded ? (
                <FolderOpen className="h-5 w-5 text-primary-600" />
              ) : (
                <Folder className="h-5 w-5 text-gray-600" />
              )}
            </button>
          ) : (
            <Folder className="h-5 w-5 text-gray-400 ml-1" />
          )}
          <div className="flex-1">
            <h3 className="text-sm font-medium text-gray-900">{category.name}</h3>
            {category.description && (
              <p className="text-sm text-gray-500 line-clamp-1">{category.description}</p>
            )}
          </div>
          <span
            className={`px-2 py-1 text-xs font-semibold rounded-full ${
              category.isActive ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
            }`}
          >
            {category.isActive ? 'Active' : 'Inactive'}
          </span>
        </div>
        <div className="flex items-center gap-2 ml-4">
          <button
            onClick={() => onEdit(category)}
            className="text-blue-600 hover:text-blue-900"
            title="Edit"
          >
            <Edit className="h-5 w-5" />
          </button>
          <button
            onClick={() => onDelete(category.id, category.name)}
            className="text-red-600 hover:text-red-900"
            title="Delete"
          >
            <Trash2 className="h-5 w-5" />
          </button>
        </div>
      </div>
      {isExpanded &&
        subcategories.map((sub: any) => (
          <CategoryRow
            key={sub.id}
            category={sub}
            subcategories={[]}
            onEdit={onEdit}
            onDelete={onDelete}
            level={level + 1}
          />
        ))}
    </>
  );
};

import { Link, useNavigate } from 'react-router-dom';
import { ShoppingCart, Search, User, LogOut, LayoutDashboard } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { useCart } from '../../context/CartContext';

export const Header = () => {
  const { user, isAdmin, logout } = useAuth();
  const { getCartItemCount } = useCart();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const cartCount = getCartItemCount();

  return (
    <header className="bg-white shadow-sm sticky top-0 z-50">
      <div className="container mx-auto px-4">
        <div className="flex items-center justify-between h-16">
          {/* Logo */}
          <Link to="/" className="flex items-center space-x-2">
            <div className="w-8 h-8 bg-primary-600 rounded-lg flex items-center justify-center">
              <span className="text-white font-bold text-lg">A</span>
            </div>
            <span className="text-xl font-bold text-gray-900">AtoZShop</span>
          </Link>

          {/* Search Bar */}
          <div className="hidden md:flex flex-1 max-w-2xl mx-8">
            <div className="relative w-full">
              <input
                type="text"
                placeholder="Search products..."
                className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
              />
              <Search className="absolute left-3 top-2.5 h-5 w-5 text-gray-400" />
            </div>
          </div>

          {/* Actions */}
          <div className="flex items-center space-x-4">
            {/* Cart */}
            <Link
              to="/cart"
              className="relative p-2 text-gray-700 hover:text-primary-600 transition-colors"
            >
              <ShoppingCart className="h-6 w-6" />
              {cartCount > 0 && (
                <span className="absolute -top-1 -right-1 bg-primary-600 text-white text-xs font-bold rounded-full h-5 w-5 flex items-center justify-center">
                  {cartCount}
                </span>
              )}
            </Link>

            {/* User Menu */}
            <div className="flex items-center space-x-2">
              {isAdmin && (
                <Link
                  to="/admin"
                  className="p-2 text-gray-700 hover:text-primary-600 transition-colors"
                  title="Admin Dashboard"
                >
                  <LayoutDashboard className="h-6 w-6" />
                </Link>
              )}

              <Link
                to="/orders"
                className="p-2 text-gray-700 hover:text-primary-600 transition-colors"
                title="My Orders"
              >
                <User className="h-6 w-6" />
              </Link>

              <button
                onClick={handleLogout}
                className="p-2 text-gray-700 hover:text-red-600 transition-colors"
                title="Logout"
              >
                <LogOut className="h-6 w-6" />
              </button>
            </div>

            {/* User Info */}
            <div className="hidden lg:flex flex-col items-end">
              <span className="text-sm font-medium text-gray-900">
                {user?.username || 'Guest'}
              </span>
              <span className="text-xs text-gray-500">
                {isAdmin ? 'Admin' : 'Customer'}
              </span>
            </div>
          </div>
        </div>

        {/* Mobile Search */}
        <div className="md:hidden pb-3">
          <div className="relative">
            <input
              type="text"
              placeholder="Search products..."
              className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
            />
            <Search className="absolute left-3 top-2.5 h-5 w-5 text-gray-400" />
          </div>
        </div>
      </div>
    </header>
  );
};

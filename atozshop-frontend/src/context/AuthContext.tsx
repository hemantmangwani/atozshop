import React, { createContext, useContext, useState, useEffect } from 'react';
import type { ReactNode } from 'react';
import type { User, LoginRequest } from '../types/auth';
import { authService } from '../services/authService';

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (credentials: LoginRequest) => Promise<User>;
  logout: () => void;
  isAdmin: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Check if user is already logged in
    const storedUser = authService.getCurrentUser();
    if (storedUser) {
      // Ensure role is set from roles array if not present
      if (!storedUser.role && storedUser.roles && storedUser.roles.length > 0) {
        storedUser.role = storedUser.roles[0];
      }
    }
    setUser(storedUser);
    setIsLoading(false);
  }, []);

  const login = async (credentials: LoginRequest) => {
    const response = await authService.login(credentials);
    console.log('AuthContext - Raw response from authService:', response);
    const { id, username, email, fullName, roles, tenantId, customerId } = response;
    console.log('AuthContext - Extracted roles:', roles);
    console.log('AuthContext - Customer ID:', customerId);
    // Use the first role as the primary role
    const role = roles && roles.length > 0 ? roles[0] : 'CUSTOMER';
    console.log('AuthContext - Computed primary role:', role);
    const user = { id, username, email, fullName, role, roles, tenantId, customerId };
    console.log('AuthContext - User object to return:', user);
    setUser(user);
    return user;
  };

  const logout = () => {
    authService.logout();
    setUser(null);
  };

  const isAdmin = user?.role === 'ADMIN' || user?.role === 'SUPER_ADMIN';

  const value: AuthContextType = {
    user,
    isAuthenticated: !!user,
    isLoading,
    login,
    logout,
    isAdmin,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

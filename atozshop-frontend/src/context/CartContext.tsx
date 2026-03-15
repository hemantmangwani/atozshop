import React, { createContext, useContext, useState, useEffect } from 'react';
import type { ReactNode } from 'react';
import type { Cart, CartItem } from '../types/cart';
import { STORAGE_KEYS } from '../constants/api';
import { productService } from '../services/productService';

interface CartContextType {
  cart: Cart;
  addToCart: (item: Omit<CartItem, 'totalPrice'>) => Promise<void>;
  removeFromCart: (variantId: number) => void;
  updateQuantity: (variantId: number, quantity: number) => Promise<void>;
  clearCart: () => void;
  getCartItemCount: () => number;
}

const CartContext = createContext<CartContextType | undefined>(undefined);

export const useCart = () => {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error('useCart must be used within a CartProvider');
  }
  return context;
};

interface CartProviderProps {
  children: ReactNode;
}

const calculateCart = (items: CartItem[]): Cart => {
  const totalItems = items.length;
  const totalQuantity = items.reduce((sum, item) => sum + item.quantity, 0);
  const subtotal = items.reduce((sum, item) => sum + item.totalPrice, 0);

  return {
    items,
    totalItems,
    totalQuantity,
    subtotal,
  };
};

export const CartProvider: React.FC<CartProviderProps> = ({ children }) => {
  const [cart, setCart] = useState<Cart>({
    items: [],
    totalItems: 0,
    totalQuantity: 0,
    subtotal: 0,
  });

  // Load cart from localStorage on mount
  useEffect(() => {
    const savedCart = localStorage.getItem(STORAGE_KEYS.CART);
    if (savedCart) {
      try {
        const items = JSON.parse(savedCart) as CartItem[];
        setCart(calculateCart(items));
      } catch (error) {
        console.error('Error loading cart:', error);
      }
    }
  }, []);

  // Save cart to localStorage whenever it changes
  useEffect(() => {
    localStorage.setItem(STORAGE_KEYS.CART, JSON.stringify(cart.items));
  }, [cart]);

  const addToCart = async (item: Omit<CartItem, 'totalPrice'>) => {
    // Check stock availability
    const stock = await productService.checkStockAvailability(item.variantId);

    if (stock.availableStock < item.quantity) {
      throw new Error(`Only ${stock.availableStock} items available in stock`);
    }

    setCart((prevCart) => {
      const existingItemIndex = prevCart.items.findIndex(
        (i) => i.variantId === item.variantId
      );

      let newItems: CartItem[];

      if (existingItemIndex >= 0) {
        // Update existing item
        const newQuantity = prevCart.items[existingItemIndex].quantity + item.quantity;

        if (newQuantity > stock.availableStock) {
          throw new Error(`Only ${stock.availableStock} items available in stock`);
        }

        newItems = [...prevCart.items];
        newItems[existingItemIndex] = {
          ...newItems[existingItemIndex],
          quantity: newQuantity,
          totalPrice: newQuantity * item.unitPrice,
        };
      } else {
        // Add new item
        newItems = [
          ...prevCart.items,
          {
            ...item,
            totalPrice: item.quantity * item.unitPrice,
          },
        ];
      }

      return calculateCart(newItems);
    });
  };

  const removeFromCart = (variantId: number) => {
    setCart((prevCart) => {
      const newItems = prevCart.items.filter((item) => item.variantId !== variantId);
      return calculateCart(newItems);
    });
  };

  const updateQuantity = async (variantId: number, quantity: number) => {
    if (quantity <= 0) {
      removeFromCart(variantId);
      return;
    }

    // Check stock availability
    const stock = await productService.checkStockAvailability(variantId);

    if (stock.availableStock < quantity) {
      throw new Error(`Only ${stock.availableStock} items available in stock`);
    }

    setCart((prevCart) => {
      const newItems = prevCart.items.map((item) => {
        if (item.variantId === variantId) {
          return {
            ...item,
            quantity,
            totalPrice: quantity * item.unitPrice,
          };
        }
        return item;
      });

      return calculateCart(newItems);
    });
  };

  const clearCart = () => {
    setCart({
      items: [],
      totalItems: 0,
      totalQuantity: 0,
      subtotal: 0,
    });
  };

  const getCartItemCount = () => {
    return cart.totalQuantity;
  };

  const value: CartContextType = {
    cart,
    addToCart,
    removeFromCart,
    updateQuantity,
    clearCart,
    getCartItemCount,
  };

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
};

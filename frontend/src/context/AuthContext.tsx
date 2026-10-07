import React, { createContext, useContext, useState, useEffect } from 'react';
import { api } from '../services/api';
import { User, AuthResponse } from '../types';

interface AuthContextType {
  user: User | null;
  token: string | null;
  loading: boolean;
  login: (usernameOrEmail: string, password: string) => Promise<AuthResponse>;
  register: (data: any) => Promise<AuthResponse>;
  logout: () => void;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    const savedToken = localStorage.getItem('token');
    const savedUser = localStorage.getItem('user');
    if (savedToken && savedUser) {
      try {
        setToken(savedToken);
        setUser(JSON.parse(savedUser));
      } catch (e) {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
      }
    }
    setLoading(false);
  }, []);

  const login = async (usernameOrEmail: string, password: string): Promise<AuthResponse> => {
    const res = await api.post('/auth/login', { usernameOrEmail, password });
    const authData: AuthResponse = res.data.data;
    localStorage.setItem('token', authData.accessToken);
    const userData: User = {
      id: authData.id,
      username: authData.username,
      email: authData.email,
      role: authData.role,
      fullName: authData.fullName,
    };
    localStorage.setItem('user', JSON.stringify(userData));
    setToken(authData.accessToken);
    setUser(userData);
    return authData;
  };

  const register = async (data: any): Promise<AuthResponse> => {
    const res = await api.post('/auth/register', data);
    const authData: AuthResponse = res.data.data;
    localStorage.setItem('token', authData.accessToken);
    const userData: User = {
      id: authData.id,
      username: authData.username,
      email: authData.email,
      role: authData.role,
      fullName: authData.fullName,
    };
    localStorage.setItem('user', JSON.stringify(userData));
    setToken(authData.accessToken);
    setUser(userData);
    return authData;
  };

  const logout = () => {
    try {
      api.post('/auth/logout').catch(() => {});
    } finally {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      setToken(null);
      setUser(null);
      window.location.href = '/login';
    }
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        loading,
        login,
        register,
        logout,
        isAuthenticated: !!token && !!user,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

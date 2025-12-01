import React, { createContext, useState, useEffect } from 'react';
import { authApi } from '../api';

export const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  const checkAuth = async () => {
    try {
      const data = await authApi.me();
      setUser(data);
    } catch (_) {
      setUser(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { checkAuth(); }, []);

  const login = async (payload) => {
    const data = await authApi.login(payload);
    setUser(data);
    return data;
  };

  const register = async (payload) => {
    const data = await authApi.register(payload);
    setUser(data);
    return data;
  };

  const logout = async () => {
    try {
      await authApi.logout();
    } catch (e) {
      console.warn('Logout failed:', e);
    } finally {
      // aseguramos que el user se limpie aunque falle la petición
      setUser(null);
    }
  };

  return (
    <AuthContext.Provider value={{ user, setUser, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}
import React, { useState, useContext, useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, AuthContext } from './context/AuthContext';
import { onTokenExpired } from './api';
import TokenRefreshModal from './components/TokenRefreshModal';
import Login from './components/Login';
import Register from './components/Register';
import Dashboard from './components/dashboard/Dashboard';
import DashboardLayout from './components/dashboard/DashboardLayout';
import Expenses from './components/Expenses';
import CalendarPage from './components/TasksCalendar';
import ShoppingList from './components/ShoppingItems';
import Settings from './components/Settings';

function LoginWrapper() {
  const [view, setView] = useState('login');
  return view === 'login' ? <Login switchToRegister={() => setView('register')} /> : <Register switchToLogin={() => setView('login')} />;
}

function PrivateRoute({ children }) {
  const { user, loading } = useContext(AuthContext);
  if (loading) return (
    <div className="min-h-screen bg-gray-100 flex items-center justify-center">
      <div className="text-xl text-gray-600">Loading...</div>
    </div>
  );
  return user ? children : <Navigate to="/login" replace />;
}

function AppContent() {
  const { logout, refreshToken } = useContext(AuthContext);
  const [showRefreshModal, setShowRefreshModal] = useState(false);

  useEffect(() => {
    const unsubscribe = onTokenExpired(() => {
      setShowRefreshModal(true);
    });

    return unsubscribe;
  }, []);

  const handleRefresh = async () => {
    try {
      await refreshToken();
      setShowRefreshModal(false);
    } catch (error) {
      console.error('Failed to refresh token:', error);
      await logout();
      setShowRefreshModal(false);
    }
  };

  const handleLogout = async () => {
    setShowRefreshModal(false);
    await logout();
  };

  return (
    <>
      <TokenRefreshModal 
        isOpen={showRefreshModal}
        onRefresh={handleRefresh}
        onLogout={handleLogout}
      />
      
      <Routes>
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="/login" element={<LoginWrapper />} />
        <Route path="/register" element={<Register switchToLogin={() => {}} />} />
        <Route path="/dashboard" element={<PrivateRoute><DashboardLayout /></PrivateRoute>}>
          <Route index element={<Dashboard />} />
          <Route path="expenses" element={<Expenses />} />
          <Route path="calendar" element={<CalendarPage />} />
          <Route path="shopping" element={<ShoppingList />} />
          <Route path="settings" element={<Settings />} />
        </Route>
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <AppContent />
      </BrowserRouter>
    </AuthProvider>
  );
}

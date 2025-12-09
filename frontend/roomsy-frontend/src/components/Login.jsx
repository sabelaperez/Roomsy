import React, { useState, useContext } from 'react';
import { AuthContext } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';

export default function Login({ switchToRegister }) {
  const { login } = useContext(AuthContext);
  const navigate = useNavigate();
  const [loginData, setLoginData] = useState({ email: '', password: '' });
  const [loginError, setLoginError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleLogin = async () => {
    setLoginError('');
    setLoading(true);
    try {
      await login(loginData);
      navigate('/dashboard', { replace: true }); 
    } catch (e) {
      setLoginError(e.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="bg-white rounded-lg shadow-md p-8 max-w-md mx-auto mt-10">
      <h2 className="text-2xl font-bold mb-6 text-center text-gray-800">Login to Roomsy</h2>

      <div className="space-y-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Email
          </label>
          <input
            type="email"
            value={loginData.email}
            onChange={(e) => setLoginData({ ...loginData, email: e.target.value })}
            onKeyDown={(e) => e.key === 'Enter' && handleLogin()}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="your@email.com"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Password
          </label>
          <input
            type="password"
            value={loginData.password}
            onChange={(e) => setLoginData({ ...loginData, password: e.target.value })}
            onKeyDown={(e) => e.key === 'Enter' && handleLogin()}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="••••••••"
          />
        </div>

        {loginError && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
            {loginError}
          </div>
        )}

        <button
          onClick={handleLogin}
          className="w-full bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 transition-colors font-medium"
          disabled={loading}
        >
          {loading ? 'Logging in...' : 'Login'}
        </button>
      </div>

      <div className="mt-4 text-center">
        <button
          onClick={() => switchToRegister()}
          className="text-blue-600 hover:text-blue-700 text-sm"
        >
          Don't have an account? Register
        </button>
      </div>
    </div>
  );
}
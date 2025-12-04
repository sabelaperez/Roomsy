import React, { useContext } from 'react';
import { NavLink, Outlet } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';

export default function DashboardLayout() {
  const { logout } = useContext(AuthContext);

  const linkClass = ({ isActive }) =>
    `px-3 py-2 rounded-md text-sm font-medium ${isActive ? 'bg-blue-600 text-white' : 'text-gray-700 hover:bg-gray-100'}`;

  return (
    <div className="min-h-screen bg-gray-100 py-12 px-4">
      <div className="max-w-5xl mx-auto">
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-2xl font-bold text-gray-800">Roomsy</h1>
          <div className="flex items-center gap-4">
            <button onClick={logout} className="text-sm text-red-600 hover:text-red-700">Logout</button>
          </div>
        </div>

        <nav className="bg-white rounded-lg shadow-sm p-3 mb-6">
          <div className="flex items-center w-full">
            <div className="flex gap-2">
              <NavLink to="/dashboard" className={linkClass} end>Dashboard</NavLink>
              <NavLink to="/dashboard/expenses" className={linkClass}>Expenses</NavLink>
              <NavLink to="/dashboard/calendar" className={linkClass}>Calendar</NavLink>
              <NavLink to="/dashboard/shopping" className={linkClass}>Shopping</NavLink>
            </div>
            <div className="ml-auto">
              <NavLink to="/dashboard/settings" className={linkClass}>Settings</NavLink>
            </div>
          </div>
        </nav>

        <Outlet />
      </div>
    </div>
  );
}
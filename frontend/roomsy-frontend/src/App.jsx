import React, { useState, useEffect } from 'react';

const API_BASE_URL = 'http://localhost:8080/api/v1';

function App() {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [view, setView] = useState('login'); // 'login', 'register', 'dashboard'
  
  // Login form
  const [loginData, setLoginData] = useState({ email: '', password: '' });
  const [loginError, setLoginError] = useState('');
  
  // Register form
  const [registerData, setRegisterData] = useState({
    email: '',
    username: '',
    fullName: '',
    password: ''
  });
  const [registerError, setRegisterError] = useState('');
  
  // Group creation
  const [groupName, setGroupName] = useState('');
  const [groupError, setGroupError] = useState('');
  const [groupSuccess, setGroupSuccess] = useState('');

  // Join group
  const [inviteCode, setInviteCode] = useState('');
  const [joinError, setJoinError] = useState('');
  const [joinSuccess, setJoinSuccess] = useState('');

  // Group info
  const [groupInfo, setGroupInfo] = useState(null);
  const [loadingGroupInfo, setLoadingGroupInfo] = useState(false);
  const [regenerateSuccess, setRegenerateSuccess] = useState('');
  const [regenerateError, setRegenerateError] = useState('');

  // Check if user is authenticated on mount
  useEffect(() => {
    checkAuth();
  }, []);

  // Load group info when user has a group
  useEffect(() => {
    if (user?.groupId) {
      loadGroupInfo();
    }
  }, [user?.groupId]);

  const checkAuth = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/me`, {
        credentials: 'include'
      });
      
      if (response.ok) {
        const data = await response.json();
        setUser(data);
        setView('dashboard');
      }
    } catch (error) {
      console.error('Auth check failed:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadGroupInfo = async () => {
    if (!user?.groupId) return;
    
    setLoadingGroupInfo(true);
    try {
      const response = await fetch(`${API_BASE_URL}/groups/${user.groupId}`, {
        credentials: 'include'
      });
      
      if (response.ok) {
        const data = await response.json();
        setGroupInfo(data);
      }
    } catch (error) {
      console.error('Failed to load group info:', error);
    } finally {
      setLoadingGroupInfo(false);
    }
  };

  const handleLogin = async () => {
    setLoginError('');
    
    try {
      const response = await fetch(`${API_BASE_URL}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(loginData)
      });
      
      if (response.ok) {
        const data = await response.json();
        setUser(data);
        setView('dashboard');
        setLoginData({ email: '', password: '' });
      } else {
        const error = await response.json();
        setLoginError(error.message || 'Login failed');
      }
    } catch (error) {
      setLoginError('Network error. Please try again.');
    }
  };

  const handleRegister = async () => {
    setRegisterError('');
    
    try {
      const response = await fetch(`${API_BASE_URL}/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(registerData)
      });
      
      if (response.ok) {
        const data = await response.json();
        setUser(data);
        setView('dashboard');
        setRegisterData({ email: '', username: '', fullName: '', password: '' });
      } else {
        const error = await response.json();
        setRegisterError(error.message || 'Registration failed');
      }
    } catch (error) {
      setRegisterError('Network error. Please try again.');
    }
  };

  const handleCreateGroup = async () => {
    setGroupError('');
    setGroupSuccess('');
    
    if (!user?.userId) {
      setGroupError('User not authenticated');
      return;
    }
    
    try {
      const response = await fetch(`${API_BASE_URL}/groups`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({
          name: groupName,
          creatorId: user.userId
        })
      });
      
      if (response.ok) {
        const data = await response.json();
        setGroupSuccess(`Group "${data.name}" created successfully! Invite code: ${data.inviteCode}`);
        setGroupName('');
        setUser({ ...user, groupId: data.id });
        setGroupInfo(data);
      } else {
        const error = await response.json();
        setGroupError(error.message || 'Failed to create group');
      }
    } catch (error) {
      setGroupError('Network error. Please try again.');
    }
  };

  const handleJoinGroup = async () => {
    setJoinError('');
    setJoinSuccess('');
    
    if (!user?.userId) {
      setJoinError('User not authenticated');
      return;
    }

    if (!inviteCode.trim()) {
      setJoinError('Please enter an invite code');
      return;
    }
    
    try {
      const response = await fetch(`${API_BASE_URL}/users/${user.userId}/join?inviteCode=${inviteCode}`, {
        method: 'POST',
        credentials: 'include'
      });
      
      if (response.ok) {
        const data = await response.json();
        setJoinSuccess(`Successfully joined group "${data.name}"!`);
        setInviteCode('');
        setUser({ ...user, groupId: data.id });
        setGroupInfo(data);
      } else {
        const error = await response.json();
        setJoinError(error.message || 'Failed to join group');
      }
    } catch (error) {
      setJoinError('Network error. Please try again.');
    }
  };

  const handleRegenerateInviteCode = async () => {
    setRegenerateError('');
    setRegenerateSuccess('');
    
    if (!user?.groupId) {
      setRegenerateError('No group found');
      return;
    }
    
    try {
      const response = await fetch(`${API_BASE_URL}/groups/${user.groupId}/invite-code/regenerate`, {
        method: 'POST',
        credentials: 'include'
      });
      
      if (response.ok) {
        const data = await response.json();
        setRegenerateSuccess(`New invite code generated: ${data.inviteCode}`);
        setGroupInfo({ ...groupInfo, inviteCode: data.inviteCode });
      } else {
        const error = await response.json();
        setRegenerateError(error.message || 'Failed to regenerate invite code');
      }
    } catch (error) {
      setRegenerateError('Network error. Please try again.');
    }
  };

  const copyInviteCode = () => {
    if (groupInfo?.inviteCode) {
      navigator.clipboard.writeText(groupInfo.inviteCode);
      setRegenerateSuccess('Invite code copied to clipboard!');
      setTimeout(() => setRegenerateSuccess(''), 2000);
    }
  };

  const handleLogout = async () => {
    try {
      await fetch(`${API_BASE_URL}/auth/logout`, {
        method: 'POST',
        credentials: 'include'
      });
      setUser(null);
      setGroupInfo(null);
      setView('login');
    } catch (error) {
      console.error('Logout failed:', error);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-100 flex items-center justify-center">
        <div className="text-xl text-gray-600">Loading...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-100 py-12 px-4">
      <div className="max-w-md mx-auto">
        
        {view === 'login' && (
          <div className="bg-white rounded-lg shadow-md p-8">
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
                  onKeyPress={(e) => e.key === 'Enter' && handleLogin()}
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
                  onKeyPress={(e) => e.key === 'Enter' && handleLogin()}
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
              >
                Login
              </button>
            </div>
            
            <div className="mt-4 text-center">
              <button
                onClick={() => setView('register')}
                className="text-blue-600 hover:text-blue-700 text-sm"
              >
                Don't have an account? Register
              </button>
            </div>
          </div>
        )}

        {view === 'register' && (
          <div className="bg-white rounded-lg shadow-md p-8">
            <h2 className="text-2xl font-bold mb-6 text-center text-gray-800">Create Account</h2>
            
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Email
                </label>
                <input
                  type="email"
                  value={registerData.email}
                  onChange={(e) => setRegisterData({ ...registerData, email: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="your@email.com"
                />
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Username
                </label>
                <input
                  type="text"
                  value={registerData.username}
                  onChange={(e) => setRegisterData({ ...registerData, username: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="username"
                />
                <p className="text-xs text-gray-500 mt-1">4-20 characters, letters, numbers and underscores</p>
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Full Name
                </label>
                <input
                  type="text"
                  value={registerData.fullName}
                  onChange={(e) => setRegisterData({ ...registerData, fullName: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="John Doe"
                />
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Password
                </label>
                <input
                  type="password"
                  value={registerData.password}
                  onChange={(e) => setRegisterData({ ...registerData, password: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="••••••••"
                />
                <p className="text-xs text-gray-500 mt-1">Minimum 8 characters</p>
              </div>
              
              {registerError && (
                <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
                  {registerError}
                </div>
              )}
              
              <button
                onClick={handleRegister}
                className="w-full bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 transition-colors font-medium"
              >
                Register
              </button>
            </div>
            
            <div className="mt-4 text-center">
              <button
                onClick={() => setView('login')}
                className="text-blue-600 hover:text-blue-700 text-sm"
              >
                Already have an account? Login
              </button>
            </div>
          </div>
        )}

        {view === 'dashboard' && user && (
          <div className="bg-white rounded-lg shadow-md p-8">
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-2xl font-bold text-gray-800">Dashboard</h2>
              <button
                onClick={handleLogout}
                className="text-sm text-red-600 hover:text-red-700 font-medium"
              >
                Logout
              </button>
            </div>
            
            <div className="bg-blue-50 border border-blue-200 p-4 rounded-md mb-6">
              <p className="text-sm text-gray-600">Welcome back!</p>
              <p className="font-semibold text-gray-800">{user.fullName}</p>
              <p className="text-sm text-gray-600">{user.email}</p>
            </div>

            {user.groupId ? (
              <div className="space-y-4">
                <div className="bg-green-50 border border-green-200 p-4 rounded-md">
                  <p className="text-green-800 font-medium mb-2">
                    You're in a group!
                  </p>
                  {loadingGroupInfo ? (
                    <p className="text-sm text-green-700">Loading group info...</p>
                  ) : groupInfo ? (
                    <div>
                      <p className="text-sm text-green-700">Group: <span className="font-semibold">{groupInfo.name}</span></p>
                      <p className="text-sm text-green-700">Members: {groupInfo.memberCount}</p>
                      <div className="mt-3 flex items-center space-x-2">
                        <div className="bg-white border border-green-300 px-3 py-2 rounded text-sm font-mono flex-1">
                          {groupInfo.inviteCode}
                        </div>
                        <button
                          onClick={copyInviteCode}
                          className="bg-green-600 text-white px-3 py-2 rounded text-sm hover:bg-green-700 transition-colors"
                        >
                          Copy
                        </button>
                      </div>
                    </div>
                  ) : (
                    <p className="text-sm text-green-700">Group ID: {user.groupId}</p>
                  )}
                </div>

                {groupInfo && (
                  <div className="border-t pt-4">
                    <h3 className="text-lg font-semibold mb-3 text-gray-800">Manage Invite Code</h3>
                    
                    {regenerateSuccess && (
                      <div className="bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded mb-3">
                        {regenerateSuccess}
                      </div>
                    )}
                    
                    {regenerateError && (
                      <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-3">
                        {regenerateError}
                      </div>
                    )}
                    
                    <button
                      onClick={handleRegenerateInviteCode}
                      className="w-full bg-orange-600 text-white py-2 px-4 rounded-md hover:bg-orange-700 transition-colors font-medium"
                    >
                      Regenerate Invite Code
                    </button>
                    <p className="text-xs text-gray-500 mt-2 text-center">
                      This will invalidate the current invite code
                    </p>
                  </div>
                )}
              </div>
            ) : (
              <div className="space-y-6">
                <div>
                  <h3 className="text-lg font-semibold mb-4 text-gray-800">Create a Group</h3>
                  
                  <div className="space-y-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Group Name
                      </label>
                      <input
                        type="text"
                        value={groupName}
                        onChange={(e) => setGroupName(e.target.value)}
                        onKeyPress={(e) => e.key === 'Enter' && handleCreateGroup()}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                        placeholder="My Awesome Group"
                      />
                      <p className="text-xs text-gray-500 mt-1">
                        Letters, numbers, and spaces only (3-50 characters)
                      </p>
                    </div>
                    
                    {groupError && (
                      <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
                        {groupError}
                      </div>
                    )}
                    
                    {groupSuccess && (
                      <div className="bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded">
                        {groupSuccess}
                      </div>
                    )}
                    
                    <button
                      onClick={handleCreateGroup}
                      className="w-full bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 transition-colors font-medium"
                    >
                      Create Group
                    </button>
                  </div>
                </div>

                <div className="border-t pt-6">
                  <h3 className="text-lg font-semibold mb-4 text-gray-800">Join a Group</h3>
                  
                  <div className="space-y-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Invite Code
                      </label>
                      <input
                        type="text"
                        value={inviteCode}
                        onChange={(e) => setInviteCode(e.target.value)}
                        onKeyPress={(e) => e.key === 'Enter' && handleJoinGroup()}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-green-500"
                        placeholder="Enter invite code"
                      />
                      <p className="text-xs text-gray-500 mt-1">
                        Ask your roommate for the group invite code
                      </p>
                    </div>
                    
                    {joinError && (
                      <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
                        {joinError}
                      </div>
                    )}
                    
                    {joinSuccess && (
                      <div className="bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded">
                        {joinSuccess}
                      </div>
                    )}
                    
                    <button
                      onClick={handleJoinGroup}
                      className="w-full bg-green-600 text-white py-2 px-4 rounded-md hover:bg-green-700 transition-colors font-medium"
                    >
                      Join Group
                    </button>
                  </div>
                </div>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}

export default App;

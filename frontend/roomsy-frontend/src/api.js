const API_BASE_URL = 'http://localhost:8080/api/v1';

async function request(path, options = {}) {
  const res = await fetch(`${API_BASE_URL}${path}`, {
    credentials: 'include',
    headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
    ...options,
  });
  const text = await res.text();
  const data = text ? JSON.parse(text) : null;
  if (!res.ok) {
    const err = (data && data.message) || res.statusText || 'Error';
    throw new Error(err);
  }
  return data;
}

export const authApi = {
  me: () => request('/auth/me'),
  login: (payload) => request('/auth/login', { method: 'POST', body: JSON.stringify(payload) }),
  register: (payload) => request('/auth/register', { method: 'POST', body: JSON.stringify(payload) }),
  logout: () => request('/auth/logout', { method: 'POST' }),
};

export const groupApi = {
  create: (payload) => request('/groups', { method: 'POST', body: JSON.stringify(payload) }),
  get: (id) => request(`/users/${id}/group`),
  regenerateInvite: (groupId) => request(`/groups/${groupId}/invite-code/regenerate`, { method: 'POST' }),
  joinByInvite: (userId, inviteCode) => request(`/users/${userId}/join?inviteCode=${encodeURIComponent(inviteCode)}`, { method: 'POST' }),
};
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
  getGroupNews: (groupId, { page = 0, size = 5, sortBy = 'createdAt', sortDirection = 'desc' } = {}) =>
    request(`/groups/${groupId}/news?page=${page}&size=${size}&sortBy=${encodeURIComponent(sortBy)}&sortDirection=${encodeURIComponent(sortDirection)}`),
  getNews: (groupId, newsId) => request(`/groups/${groupId}/news/${newsId}`),
  getMembers: (groupId) => request(`/groups/${groupId}/members`),
  removeMember: (groupId, memberId) => request(`/groups/${groupId}/members/${memberId}`, { method: 'DELETE' }),
  delete: (groupId) => request(`/groups/${groupId}`, { method: 'DELETE' }),
  updateName: (groupId, groupName) => request(`/groups/${groupId}/name`, { method: 'PATCH', body: JSON.stringify({ name: groupName }) }),
};

export const userApi = {
  deleteMe: () => request('/users/me', { method: 'DELETE' }),
  joinByInvite: (userId, inviteCode) => request(`/users/${userId}/join?inviteCode=${encodeURIComponent(inviteCode)}`, { method: 'POST' }),
}

export const expenseApi = {
  create: (groupId, payload) => request(`/groups/${groupId}/expenses`, { method: 'POST', body: JSON.stringify(payload) }),
  getExpenseItems: (groupId, { page = 0, size = 50, sortBy = 'createdAt', sortDirection = 'desc' } = {}) =>
    request(`/groups/${groupId}/expenses?page=${page}&size=${size}&sortBy=${encodeURIComponent(sortBy)}&sortDirection=${encodeURIComponent(sortDirection)}`),
  getSharedExpenses: (groupId, { page = 0, size = 50, sortBy = 'createdAt', sortDirection = 'desc' } = {}) =>
    request(`/groups/${groupId}/expenses/shared-expenses?page=${page}&size=${size}&sortBy=${encodeURIComponent(sortBy)}&sortDirection=${encodeURIComponent(sortDirection)}`),
  delete: (groupId, expenseItemId) => request(`/groups/${groupId}/expenses/items/${expenseItemId}`, { method: 'DELETE' }),
  pay: (groupId, sharedExpenseId) => request(`/groups/${groupId}/expenses/shared/${sharedExpenseId}`, { method: 'DELETE' }),
}

export const tasksApi = {
  createTask: (groupId, payload) => request(`/groups/${groupId}/cleaning-tasks`, { method: 'POST', body: JSON.stringify(payload) }),
  getTask: (groupId, taskId) => request(`/groups/${groupId}/cleaning-tasks/${taskId}`),
  deleteTask: (groupId, taskId) => request(`/groups/${groupId}/cleaning-tasks/${taskId}`, { method: 'DELETE' }),
  reassignTask: (groupId, taskId, assignedToIds) =>
    request(`/groups/${groupId}/cleaning-tasks/${taskId}/assign-to`, { method: 'PATCH', body: JSON.stringify({ assignedToIds }),}),
  setCompleted: (groupId, taskId, completed) =>
    request(`/groups/${groupId}/cleaning-tasks/${taskId}/completed`, { method: 'PATCH', body: JSON.stringify({ completed }),}),
  changeDate: (groupId, taskId, newDate) =>
    request(`/groups/${groupId}/cleaning-tasks/${taskId}/date`, { method: 'PATCH', body: JSON.stringify({ newDate }),}),
  changeTitle: (groupId, taskId, title) =>
    request(`/groups/${groupId}/cleaning-tasks/${taskId}/title`, { method: 'PATCH', body: JSON.stringify({ title }),}),
  getGroupTasks: (groupId, { page = 0, size = 10, sortBy = 'createdAt', sortDirection = 'desc' } = {}) =>
    request(`/groups/${groupId}/cleaning-tasks?page=${page}&size=${size}&sortBy=${encodeURIComponent(sortBy)}&sortDirection=${encodeURIComponent(sortDirection)}`),
};

export const shoppingApi = {
  getGroupShoppingItems: (groupId, { page = 0, size = 10, sortBy = 'name', sortDirection = 'asc' } = {}) =>
    request(`/groups/${groupId}/shopping-items?page=${page}&size=${size}&sortBy=${encodeURIComponent(sortBy)}&sortDirection=${encodeURIComponent(sortDirection)}`),
  create: (groupId, payload) =>
    request(`/groups/${groupId}/shopping-items`, { method: 'POST', body: JSON.stringify(payload) }),
  updateQuantity: (groupId, itemId, payload) =>
    request(`/groups/${groupId}/shopping-items/${itemId}/quantity`, { method: 'PATCH', body: JSON.stringify(payload) }),
  updateName: (groupId, itemId, payload) =>
    request(`/groups/${groupId}/shopping-items/${itemId}/name`, { method: 'PATCH', body: JSON.stringify(payload) }),
  updateCategory: (groupId, itemId, payload) =>
    request(`/groups/${groupId}/shopping-items/${itemId}/category`, { method: 'PATCH', body: JSON.stringify(payload) }),
  delete: (groupId, itemId) =>
    request(`/groups/${groupId}/shopping-items/${itemId}`, { method: 'DELETE' }),
};

export const categoryApi = {
  getGroupCategories: (groupId, { page = 0, size = 10, sortBy = 'name', sortDirection = 'asc' } = {}) =>
    request(`/groups/${groupId}/categories?page=${page}&size=${size}&sortBy=${encodeURIComponent(sortBy)}&sortDirection=${encodeURIComponent(sortDirection)}`),
  create: (groupId, payload) =>
    request(`/groups/${groupId}/categories`, { method: 'POST', body: JSON.stringify(payload) }),
  getCategoryById: (groupId, categoryId) =>
    request(`/groups/${groupId}/categories/${categoryId}`),
  updateName: (groupId, categoryId, payload) =>
    request(`/groups/${groupId}/categories/${categoryId}/name`, { method: 'PATCH', body: JSON.stringify(payload) }),
  updateColor: (groupId, categoryId, payload) =>
    request(`/groups/${groupId}/categories/${categoryId}/color`, { method: 'PATCH', body: JSON.stringify(payload) }),
  delete: (groupId, categoryId) =>
    request(`/groups/${groupId}/categories/${categoryId}`, { method: 'DELETE' }),
};

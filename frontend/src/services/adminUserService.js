import { apiClient } from './apiClient.js';

const qs = (params = {}) => {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') search.set(key, value);
  });
  return search.toString();
};

export const getAdminUsers = (params = {}) => apiClient(`/users${qs(params) ? `?${qs(params)}` : ''}`);
export const getAdminUserSummary = () => apiClient('/users/summary');
export const getAdminUserDetails = (id) => apiClient(`/users/${id}`);
export const updateAdminUserRole = (id, role) => apiClient(`/users/${id}/role`, { method: 'PATCH', body: JSON.stringify({ role }) });
export const updateAdminUserStatus = (id, status) => apiClient(`/users/${id}/status`, { method: 'PATCH', body: JSON.stringify({ status }) });
export const lockAdminUser = (id) => apiClient(`/users/${id}/lock`, { method: 'PATCH' });
export const unlockAdminUser = (id) => apiClient(`/users/${id}/unlock`, { method: 'PATCH' });
export const deleteAdminUser = (id) => apiClient(`/users/${id}`, { method: 'DELETE' });
export const getAdminDeletedUsers = () => apiClient('/users/deleted');
export const restoreAdminUser = (id) => apiClient(`/users/${id}/restore`, { method: 'PATCH' });

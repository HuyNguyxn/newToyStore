import { apiClient } from './apiClient.js';

const qs = (params = {}) => {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') search.set(key, value);
  });
  return search.toString();
};

export const getBlacklistedWords = (params = {}) => apiClient(`/admin/moderation/blacklists${qs(params) ? `?${qs(params)}` : ''}`);
export const createBlacklistedWord = (payload) => apiClient('/admin/moderation/blacklists', { method: 'POST', body: JSON.stringify(payload) });
export const updateBlacklistedWord = (id, payload) => apiClient(`/admin/moderation/blacklists/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
export const deleteBlacklistedWord = (id) => apiClient(`/admin/moderation/blacklists/${id}`, { method: 'DELETE' });
export const restoreBlacklistedWord = (id) => apiClient(`/admin/moderation/blacklists/${id}/restore`, { method: 'PUT' });
export const hardDeleteBlacklistedWord = (id) => apiClient(`/admin/moderation/blacklists/${id}/hard`, { method: 'DELETE' });

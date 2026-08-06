import { apiClient } from './apiClient.js';

const qs = (params = {}) => {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') search.set(key, value);
  });
  return search.toString();
};

export const getImports = (params = {}) => apiClient(`/imports${qs(params) ? `?${qs(params)}` : ''}`);
export const getImportDetails = (id) => apiClient(`/imports/${id}`);
export const createImportNote = (payload) => apiClient('/imports', { method: 'POST', body: JSON.stringify(payload) });
export const completeImportNote = (id) => apiClient(`/imports/${id}/complete`, { method: 'PATCH' });
export const cancelImportNote = (id) => apiClient(`/imports/${id}/cancel`, { method: 'PATCH' });

import { apiClient } from './apiClient.js';

const qs = (params = {}) => {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') search.set(key, value);
  });
  return search.toString();
};

export const getSuppliers = (params = {}) => apiClient(`/suppliers${qs(params) ? `?${qs(params)}` : ''}`);
export const createSupplier = (payload) => apiClient('/suppliers', { method: 'POST', body: JSON.stringify(payload) });
export const updateSupplier = (id, payload) => apiClient(`/suppliers/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
export const changeSupplierStatus = (id, status) => apiClient(`/suppliers/${id}/status`, { method: 'PATCH', body: JSON.stringify({ status }) });
export const restoreSupplier = (id) => apiClient(`/suppliers/${id}/restore`, { method: 'PATCH' });
export const deleteSupplier = (id) => apiClient(`/suppliers/${id}`, { method: 'DELETE' });

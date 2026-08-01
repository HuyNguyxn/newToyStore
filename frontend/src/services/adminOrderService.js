import { apiClient } from './apiClient.js';

const qs = (params = {}) => {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') search.set(key, value);
  });
  return search.toString();
};

export const getAdminOrders = (params = {}) => apiClient(`/orders/admin/filter${qs(params) ? `?${qs(params)}` : ''}`);
export const getAdminOrderDetails = (id) => apiClient(`/orders/${id}`);
export const confirmAdminOrder = (id, note = '') => apiClient(`/orders/${id}/confirm${note ? `?note=${encodeURIComponent(note)}` : ''}`, { method: 'PATCH' });
export const shipAdminOrder = (id, note = '') => apiClient(`/orders/${id}/ship${note ? `?note=${encodeURIComponent(note)}` : ''}`, { method: 'PATCH' });
export const completeAdminOrder = (id, note = '') => apiClient(`/orders/${id}/complete${note ? `?note=${encodeURIComponent(note)}` : ''}`, { method: 'PATCH' });
export const cancelAdminOrder = (id, note = '') => apiClient(`/orders/${id}/cancel${note ? `?note=${encodeURIComponent(note)}` : ''}`, { method: 'PATCH' });
export const deleteAdminOrder = (id) => apiClient(`/orders/${id}`, { method: 'DELETE' });
export const updateAdminOrderShipping = (id, payload) => apiClient(`/orders/${id}/shipping-address`, { method: 'PATCH', body: JSON.stringify(payload) });

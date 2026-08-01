import { apiClient } from './apiClient.js';

const qs = (params = {}) => {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') search.set(key, value);
  });
  return search.toString();
};

export const getShipments = (params = {}) => apiClient(`/shipments/admin/filter${qs(params) ? `?${qs(params)}` : ''}`);
export const createShipmentForOrder = (orderId) => apiClient(`/shipments/orders/${orderId}`, { method: 'POST' });
export const getShipmentTrackingLogs = (id, params = {}) => apiClient(`/shipments/${id}/tracking-logs${qs(params) ? `?${qs(params)}` : ''}`);
export const executeShipmentAction = (id, payload) => apiClient(`/shipments/${id}/actions`, { method: 'POST', body: JSON.stringify(payload) });
export const deleteShipment = (id) => apiClient(`/shipments/${id}`, { method: 'DELETE' });

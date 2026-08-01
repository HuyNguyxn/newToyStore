import { apiClient } from './apiClient.js';

const qs = (params = {}) => {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') search.set(key, value);
  });
  return search.toString();
};

export const getMyShipments = (params = {}) => apiClient(`/shipments/my-shipments${qs(params) ? `?${qs(params)}` : ''}`);
export const getShipmentDetails = (id) => apiClient(`/shipments/${id}`);
export const getShipmentTrackingLogs = (id, params = {}) => apiClient(`/shipments/${id}/tracking-logs${qs(params) ? `?${qs(params)}` : ''}`);

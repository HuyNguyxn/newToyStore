import { apiClient } from './apiClient.js';

const qs = (params = {}) => {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') search.set(key, value);
  });
  return search.toString();
};

export const getAdminPayments = (params = {}) => apiClient(`/payments/admin/filter${qs(params) ? `?${qs(params)}` : ''}`);
export const getAdminPaymentDetails = (id) => apiClient(`/payments/${id}`);
export const markPaymentSucceeded = (id, providerTransactionId = '') => apiClient(`/payments/${id}/succeed`, { method: 'PATCH', body: JSON.stringify({ providerTransactionId: providerTransactionId || null }) });
export const markPaymentFailed = (id, reason) => apiClient(`/payments/${id}/fail`, { method: 'PATCH', body: JSON.stringify({ reason }) });
export const cancelAdminPayment = (id, reason) => apiClient(`/payments/${id}/cancel`, { method: 'PATCH', body: JSON.stringify({ reason }) });
export const deleteAdminPayment = (id) => apiClient(`/payments/${id}`, { method: 'DELETE' });
export const getAdminPaymentRefunds = (id, params = {}) => apiClient(`/payments/${id}/refunds${qs(params) ? `?${qs(params)}` : ''}`);

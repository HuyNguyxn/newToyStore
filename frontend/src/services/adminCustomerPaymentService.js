import { apiClient } from './apiClient.js';

const qs = (params = {}) => {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') search.set(key, value);
  });
  return search.toString();
};

export const getAdminCustomerPayments = (params = {}) => apiClient(`/payments/admin/filter${qs(params) ? `?${qs(params)}` : ''}`);
export const getAdminCustomerPaymentDetails = (id) => apiClient(`/payments/${id}`);
export const markCustomerPaymentSucceeded = (id, providerTransactionId = '') => apiClient(`/payments/${id}/succeed`, { method: 'PATCH', body: JSON.stringify({ providerTransactionId: providerTransactionId || null }) });
export const markCustomerPaymentFailed = (id, reason) => apiClient(`/payments/${id}/fail`, { method: 'PATCH', body: JSON.stringify({ reason }) });
export const cancelAdminCustomerPayment = (id, reason) => apiClient(`/payments/${id}/cancel`, { method: 'PATCH', body: JSON.stringify({ reason }) });
export const deleteAdminCustomerPayment = (id) => apiClient(`/payments/${id}`, { method: 'DELETE' });
export const getAdminCustomerPaymentRefunds = (id, params = {}) => apiClient(`/payments/${id}/refunds${qs(params) ? `?${qs(params)}` : ''}`);

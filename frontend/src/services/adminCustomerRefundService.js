import { apiClient } from './apiClient.js';

export function requestCustomerPaymentRefund(paymentId, payload) {
  return apiClient(`/payments/${paymentId}/refunds`, {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function getCustomerPaymentRefunds(paymentId, params = {}) {
  const query = new URLSearchParams(params).toString();
  return apiClient(`/payments/${paymentId}/refunds${query ? `?${query}` : ''}`);
}

export function getAllCustomerPaymentRefunds(params = {}) {
  const query = new URLSearchParams(params).toString();
  return apiClient(`/payments/refunds${query ? `?${query}` : ''}`);
}

export function processCustomerPaymentRefund(refundId) {
  return apiClient(`/payments/refunds/${refundId}/process`, {
    method: 'PATCH',
  });
}

export function rejectCustomerPaymentRefund(refundId, reason) {
  return apiClient(`/payments/refunds/${refundId}/reject`, {
    method: 'PATCH',
    body: JSON.stringify({ reason }),
  });
}

export function deleteCustomerPaymentRefund(refundId) {
  return apiClient(`/payments/refunds/${refundId}`, {
    method: 'DELETE',
  });
}

import { apiClient } from './apiClient.js';

export function requestPaymentRefund(paymentId, payload) {
  return apiClient(`/payments/${paymentId}/refunds`, {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function getPaymentRefunds(paymentId, params = {}) {
  const query = new URLSearchParams(params).toString();
  return apiClient(`/payments/${paymentId}/refunds${query ? `?${query}` : ''}`);
}

export function processPaymentRefund(refundId) {
  return apiClient(`/payments/refunds/${refundId}/process`, {
    method: 'PATCH',
  });
}

export function rejectPaymentRefund(refundId, reason) {
  return apiClient(`/payments/refunds/${refundId}/reject`, {
    method: 'PATCH',
    body: JSON.stringify({ reason }),
  });
}

export function deletePaymentRefund(refundId) {
  return apiClient(`/payments/refunds/${refundId}`, {
    method: 'DELETE',
  });
}

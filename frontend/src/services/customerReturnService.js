import { apiClient } from './apiClient.js';

export function createCustomerReturn(payload) {
  return apiClient('/api/returns', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function getCustomerReturns(params = {}) {
  const query = new URLSearchParams(params).toString();
  return apiClient(`/api/returns${query ? `?${query}` : ''}`);
}

export function cancelCustomerReturn(id) {
  return apiClient(`/api/returns/${id}/cancel`, { method: 'PATCH' });
}

export function updateCustomerReturnInfo(id, newReasonNote) {
  return apiClient(`/api/returns/${id}/update-info?newReasonNote=${encodeURIComponent(newReasonNote)}`, { method: 'PATCH' });
}

export function disputeCustomerReturn(id, disputeReason) {
  return apiClient(`/api/returns/${id}/dispute?disputeReason=${encodeURIComponent(disputeReason)}`, { method: 'PATCH' });
}

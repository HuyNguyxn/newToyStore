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

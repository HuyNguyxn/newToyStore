import { apiClient } from './apiClient.js';

const qs = (params = {}) => {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') search.set(key, value);
  });
  return search.toString();
};

export const getSupplierPayments = (params = {}) => (
  apiClient(`/api/supplier-payments${qs(params) ? `?${qs(params)}` : ''}`)
);

export const getSupplierPaymentDetails = (id) => apiClient(`/api/supplier-payments/${id}`);

export const createSupplierPaymentFromImport = (importNoteId) => (
  apiClient(`/api/supplier-payments/imports/${importNoteId}`, { method: 'POST' })
);

export const recordSupplierPayment = (id, payload) => (
  apiClient(`/api/supplier-payments/${id}/payments`, {
    method: 'PATCH',
    body: JSON.stringify(payload),
  })
);

export const cancelSupplierPayment = (id, reason) => (
  apiClient(`/api/supplier-payments/${id}/cancel`, {
    method: 'PATCH',
    body: JSON.stringify({ reason }),
  })
);

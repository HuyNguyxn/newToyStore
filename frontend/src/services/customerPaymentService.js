import { apiClient } from './apiClient.js';

export function checkoutCustomerPayment(payload) {
  return apiClient('/payments/checkout', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function getMyCustomerPayments(params = {}) {
  const query = new URLSearchParams(params).toString();
  return apiClient(`/payments/my-payments${query ? `?${query}` : ''}`);
}

export function getCustomerPaymentDetails(paymentId) {
  return apiClient(`/payments/${paymentId}`);
}

export function cancelCustomerPayment(paymentId, reason) {
  return apiClient(`/payments/${paymentId}/cancel`, {
    method: 'PATCH',
    body: JSON.stringify({ reason }),
  });
}

export function handleCustomerVnpayReturn(search) {
  return apiClient(`/payments/vnpay-return${search || ''}`);
}

export function createCustomerPaymentIdempotencyKey(orderId, method) {
  const randomPart = crypto.randomUUID ? crypto.randomUUID() : `${Date.now()}-${Math.random()}`;
  return `web-${orderId}-${method}-${randomPart}`.slice(0, 80);
}

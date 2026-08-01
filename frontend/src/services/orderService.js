import { apiClient } from './apiClient.js';

export function getMyOrders(params = {}) {
  const query = new URLSearchParams(params).toString();
  return apiClient(`/orders/my-orders${query ? `?${query}` : ''}`);
}

export function getAdminOrders(params = {}) {
  const query = new URLSearchParams(params).toString();
  return apiClient(`/orders/admin/filter${query ? `?${query}` : ''}`);
}

export function getOrderDetails(orderId) {
  return apiClient(`/orders/${orderId}`);
}

export function cancelOrder(orderId, note = '') {
  const query = note ? `?note=${encodeURIComponent(note)}` : '';
  return apiClient(`/orders/${orderId}/cancel${query}`, {
    method: 'PATCH',
  });
}

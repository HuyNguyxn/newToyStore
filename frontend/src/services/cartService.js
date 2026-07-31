import { apiClient } from './apiClient.js';

export function addCartItem(userId, payload) {
  return apiClient(`/carts/${userId}/items`, {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

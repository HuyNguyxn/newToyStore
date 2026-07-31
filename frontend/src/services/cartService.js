import { apiClient } from './apiClient.js';

export function getCart(userId, promoCode = '') {
  const query = promoCode ? `?promoCode=${encodeURIComponent(promoCode)}` : '';
  return apiClient(`/carts/${userId}${query}`);
}

export function addCartItem(userId, payload) {
  return apiClient(`/carts/${userId}/items`, {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function updateCartItemQuantity(userId, itemId, quantity) {
  return apiClient(`/carts/${userId}/items/${itemId}`, {
    method: 'PUT',
    body: JSON.stringify({ quantity }),
  });
}

export function toggleCartItemSelection(userId, itemId, selected) {
  return apiClient(`/carts/${userId}/items/${itemId}/toggle`, {
    method: 'PATCH',
    body: JSON.stringify({ selected }),
  });
}

export function removeCartItem(userId, itemId) {
  return apiClient(`/carts/${userId}/items/${itemId}`, {
    method: 'DELETE',
  });
}

export function clearCart(userId) {
  return apiClient(`/carts/${userId}`, {
    method: 'DELETE',
  });
}

export function checkoutCart(userId, payload) {
  return apiClient(`/carts/${userId}/checkout`, {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

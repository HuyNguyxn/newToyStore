import { apiClient } from './apiClient.js';

export function getCart(userId, promoCode = '') {
  const query = promoCode ? `?promoCode=${encodeURIComponent(promoCode)}` : '';
  return apiClient(`/carts/${userId}${query}`);
}

export async function addCartItem(userId, payload) {
  const result = await apiClient(`/carts/${userId}/items`, {
    method: 'POST',
    body: JSON.stringify(payload),
  });
  window.dispatchEvent(new Event('cart_updated'));
  return result;
}

export async function updateCartItemQuantity(userId, itemId, quantity) {
  const result = await apiClient(`/carts/${userId}/items/${itemId}`, {
    method: 'PUT',
    body: JSON.stringify({ quantity }),
  });
  window.dispatchEvent(new Event('cart_updated'));
  return result;
}

export async function toggleCartItemSelection(userId, itemId, selected) {
  const result = await apiClient(`/carts/${userId}/items/${itemId}/toggle`, {
    method: 'PATCH',
    body: JSON.stringify({ selected }),
  });
  window.dispatchEvent(new Event('cart_updated'));
  return result;
}

export async function removeCartItem(userId, itemId) {
  const result = await apiClient(`/carts/${userId}/items/${itemId}`, {
    method: 'DELETE',
  });
  window.dispatchEvent(new Event('cart_updated'));
  return result;
}

export async function clearCart(userId) {
  const result = await apiClient(`/carts/${userId}`, {
    method: 'DELETE',
  });
  window.dispatchEvent(new Event('cart_updated'));
  return result;
}

export async function checkoutCart(userId, payload) {
  const result = await apiClient(`/carts/${userId}/checkout`, {
    method: 'POST',
    body: JSON.stringify(payload),
  });
  window.dispatchEvent(new Event('cart_updated'));
  return result;
}

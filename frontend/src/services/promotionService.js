import { apiClient } from './apiClient.js';

export function getAvailableOrderPromotions(cartTotal) {
  const query = new URLSearchParams({ cartTotal: String(Number(cartTotal) || 0) });
  return apiClient(`/api/v1/promotions/available-order?${query.toString()}`);
}

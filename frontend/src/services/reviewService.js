import { apiClient } from './apiClient.js';

export function getProductReviews(productId, params = {}) {
  const query = new URLSearchParams(params).toString();
  return apiClient(`/reviews/products/${productId}${query ? `?${query}` : ''}`);
}

export function createReview(payload) {
  return apiClient('/reviews', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

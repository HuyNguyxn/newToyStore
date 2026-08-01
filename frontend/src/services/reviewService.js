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

export function getMyReviews(params = {}) {
  const query = new URLSearchParams(params).toString();
  return apiClient(`/reviews/me${query ? `?${query}` : ''}`);
}

export function updateReview(reviewId, payload) {
  return apiClient(`/reviews/${reviewId}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  });
}

export function deleteReview(reviewId) {
  return apiClient(`/reviews/${reviewId}`, {
    method: 'DELETE',
  });
}

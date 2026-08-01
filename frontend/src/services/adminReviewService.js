import { apiClient } from './apiClient.js';

function toQueryString(params = {}) {
  const searchParams = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      searchParams.set(key, value);
    }
  });
  return searchParams.toString();
}

export function getAdminReviews(params = {}) {
  const query = toQueryString(params);
  return apiClient(`/reviews/admin/all${query ? `?${query}` : ''}`);
}

export function changeReviewStatus(reviewId, status) {
  return apiClient(`/reviews/admin/${reviewId}/status?status=${encodeURIComponent(status)}`, {
    method: 'PATCH',
  });
}

export function replyToReview(reviewId, reply) {
  return apiClient(`/reviews/admin/${reviewId}/reply`, {
    method: 'PATCH',
    body: JSON.stringify({ reply }),
  });
}

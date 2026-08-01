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

export function getAdminPromotions(params = {}) {
  const query = toQueryString(params);
  return apiClient(`/api/v1/promotions${query ? `?${query}` : ''}`);
}

export function createPromotion(payload) {
  return apiClient('/api/v1/promotions', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function updatePromotion(promotionId, payload) {
  return apiClient(`/api/v1/promotions/${promotionId}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  });
}

export function activatePromotion(promotionId) {
  return apiClient(`/api/v1/promotions/${promotionId}/activate`, {
    method: 'PATCH',
  });
}

export function deactivatePromotion(promotionId) {
  return apiClient(`/api/v1/promotions/${promotionId}/deactivate`, {
    method: 'PATCH',
  });
}

export function deletePromotion(promotionId) {
  return apiClient(`/api/v1/promotions/${promotionId}`, {
    method: 'DELETE',
  });
}

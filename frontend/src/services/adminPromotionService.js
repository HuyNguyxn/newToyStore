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

export async function getAllAdminPromotions(params = {}) {
  const pageSize = 100;
  const firstPage = await getAdminPromotions({ ...params, page: 0, size: pageSize });
  if (Array.isArray(firstPage)) return firstPage;
  const items = [...(firstPage?.content || [])];
  const totalPages = Number(firstPage?.totalPages || 1);
  if (totalPages <= 1) return items;
  const remainingPages = await Promise.all(
    Array.from({ length: totalPages - 1 }, (_, index) => (
      getAdminPromotions({ ...params, page: index + 1, size: pageSize })
    )),
  );
  return items.concat(remainingPages.flatMap((page) => page?.content || []));
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

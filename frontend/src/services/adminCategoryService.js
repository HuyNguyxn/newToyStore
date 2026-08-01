import { apiClient } from './apiClient.js';

const qs = (params = {}) => {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') search.set(key, value);
  });
  return search.toString();
};

export const getAdminCategories = (params = {}) => apiClient(`/api/categories${qs(params) ? `?${qs(params)}` : ''}`);
export const getAdminCategoryTree = () => apiClient('/api/categories/admin/tree');
export const createCategory = (payload) => apiClient('/api/categories', { method: 'POST', body: JSON.stringify(payload) });
export const updateCategoryInfo = (id, payload) => apiClient(`/api/categories/${id}/info`, { method: 'PUT', body: JSON.stringify(payload) });
export const moveCategory = (id, payload) => apiClient(`/api/categories/${id}/move`, { method: 'PUT', body: JSON.stringify(payload) });
export const showCategory = (id) => apiClient(`/api/categories/${id}/show`, { method: 'PATCH' });
export const hideCategory = (id) => apiClient(`/api/categories/${id}/hide`, { method: 'PATCH' });
export const deleteCategory = (id) => apiClient(`/api/categories/${id}`, { method: 'DELETE' });

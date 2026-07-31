import { apiClient } from './apiClient.js';

export function getCategoryTree() {
  return apiClient('/api/categories/tree');
}

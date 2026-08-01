import { apiClient } from './apiClient.js';

export function getStatisticsOverview(params = {}) {
  const query = new URLSearchParams(params).toString();
  return apiClient(`/statistics/overview${query ? `?${query}` : ''}`);
}

export function getTopSellingProducts(params = {}) {
  const query = new URLSearchParams(params).toString();
  return apiClient(`/statistics/products/top-selling${query ? `?${query}` : ''}`);
}

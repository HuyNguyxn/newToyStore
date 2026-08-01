import { apiClient } from './apiClient.js';

export function getStatisticsOverview(params = {}) {
  const query = new URLSearchParams(params).toString();
  return apiClient(`/statistics/overview${query ? `?${query}` : ''}`);
}

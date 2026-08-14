import { apiClient } from './apiClient.js';

export function getAdminMenuBadges() {
  return apiClient('/api/admin/menu-badges');
}

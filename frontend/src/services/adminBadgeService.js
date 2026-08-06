import { apiClient } from './apiClient.js';

export function getAdminMenuBadges() {
  return apiClient('/api/admin/menu-badges').catch(() => ({
    pendingOrders: 0,
    pendingCustomerReturns: 0,
    pendingSupplierReturns: 0,
    lowStockVariants: 0,
  }));
}

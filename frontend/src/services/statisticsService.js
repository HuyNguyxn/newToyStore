import { apiClient } from './apiClient.js';

function buildQuery(params = {}) {
  const query = new URLSearchParams(params).toString();
  return query ? `?${query}` : '';
}

export function getStatisticsOverview(params = {}) {
  return apiClient(`/statistics/overview${buildQuery(params)}`);
}

export function getTopSellingProducts(params = {}) {
  return apiClient(`/statistics/products/top-selling${buildQuery(params)}`);
}

export function getSlowSellingProducts(params = {}) {
  return apiClient(`/statistics/products/slow-selling${buildQuery(params)}`);
}

export function getRevenueTrend(params = {}) {
  return apiClient(`/statistics/revenue/trend${buildQuery(params)}`);
}

export function getRevenueByPaymentMethod(params = {}) {
  return apiClient(`/statistics/revenue/by-payment-method${buildQuery(params)}`);
}

export function getRevenueByCategory(params = {}) {
  return apiClient(`/statistics/revenue/by-category${buildQuery(params)}`);
}

export function getTopSpendingCustomers(params = {}) {
  return apiClient(`/statistics/customers/top-spending${buildQuery(params)}`);
}

export function getPaymentFailureReasons(params = {}) {
  return apiClient(`/statistics/payments/failure-reasons${buildQuery(params)}`);
}

export function getRefundReasons(params = {}) {
  return apiClient(`/statistics/refunds/by-reason${buildQuery(params)}`);
}

export function getRefundByProduct(params = {}) {
  return apiClient(`/statistics/refunds/by-product${buildQuery(params)}`);
}

export function getShipmentsByProvider(params = {}) {
  return apiClient(`/statistics/shipments/by-provider${buildQuery(params)}`);
}

export function getShipmentFailureReasons(params = {}) {
  return apiClient(`/statistics/shipments/failure-reasons${buildQuery(params)}`);
}

export function getShipmentsByRegion(params = {}) {
  return apiClient(`/statistics/shipments/by-region${buildQuery(params)}`);
}

export function getCustomerSummary(params = {}) {
  return apiClient(`/statistics/customers/summary${buildQuery(params)}`);
}

export function getCustomerTrend(params = {}) {
  return apiClient(`/statistics/customers/trend${buildQuery(params)}`);
}

export function getInventorySnapshot(params = {}) {
  return apiClient(`/statistics/inventory/snapshot${buildQuery(params)}`);
}

export function getInventoryMovements(params = {}) {
  return apiClient(`/statistics/inventory/movements${buildQuery(params)}`);
}

export function getInventoryCostSummary(variantId) {
  const params = variantId && variantId !== 'ALL' ? { variantId } : {};
  return apiClient(`/statistics/inventory/cost-summary${buildQuery(params)}`);
}

export function getProfitMargin(params = {}) {
  return apiClient(`/statistics/profit-margin${buildQuery(params)}`);
}

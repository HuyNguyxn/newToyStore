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

export function getRevenueTrend(params = {}) {
  return apiClient(`/statistics/revenue/trend${buildQuery(params)}`);
}

export function getRevenueByPaymentMethod(params = {}) {
  return apiClient(`/statistics/revenue/by-payment-method${buildQuery(params)}`);
}

export function getRevenueByCategory(params = {}) {
  return apiClient(`/statistics/revenue/by-category${buildQuery(params)}`);
}

export function getRevenueByProduct(params = {}) {
  return apiClient(`/statistics/revenue/by-product${buildQuery(params)}`);
}

export function getRevenueByPromotion(params = {}) {
  return apiClient(`/statistics/revenue/by-promotion${buildQuery(params)}`);
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

export function getShipmentsByProvider(params = {}) {
  return apiClient(`/statistics/shipments/by-provider${buildQuery(params)}`);
}

export function getShipmentFailureReasons(params = {}) {
  return apiClient(`/statistics/shipments/failure-reasons${buildQuery(params)}`);
}

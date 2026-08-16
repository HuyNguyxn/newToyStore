import { apiClient } from './apiClient.js';

function queryString(params = {}) {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') search.set(key, value);
  });
  return search.toString();
}

export function getWarehouseBatches(params = {}) {
  const query = queryString(params);
  return apiClient(`/warehouse/batches${query ? `?${query}` : ''}`);
}

export function getWarehouseBatchDetails(batchId) {
  return apiClient(`/warehouse/batches/${batchId}`);
}

export function completeWarehouseBatch(batchId) {
  return apiClient(`/warehouse/batches/${batchId}/complete`, {
    method: 'PATCH',
  });
}

export function cancelWarehouseBatch(batchId) {
  return apiClient(`/warehouse/batches/${batchId}/cancel`, {
    method: 'PATCH',
  });
}

export function reconcileWarehouseBatch(batchId) {
  return apiClient(`/warehouse/batches/${batchId}/reconcile`, {
    method: 'PATCH',
  });
}

export function publishWarehouseProduct(batchId, productId) {
  return apiClient(`/warehouse/batches/${batchId}/products/${productId}/publish`, {
    method: 'PATCH',
  });
}

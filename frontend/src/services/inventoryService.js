import { apiClient } from './apiClient.js';

export function getVariantInventoryBatches(variantId) {
  return apiClient(`/inventory/variants/${variantId}/batches`);
}

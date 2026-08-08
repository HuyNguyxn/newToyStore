import { apiClient } from './apiClient.js';

/** @deprecated Use warehouseService.getWarehouseBatchDetails instead. */
export function getVariantInventoryBatches(variantId) {
  return apiClient(`/inventory/variants/${variantId}/batches`);
}

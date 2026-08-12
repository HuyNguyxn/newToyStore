import { apiClient } from './apiClient.js';

function toQueryString(params = {}) {
  const searchParams = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      searchParams.set(key, value);
    }
  });
  return searchParams.toString();
}

export function getAdminProducts(params = {}) {
  const query = toQueryString(params);
  return apiClient(`/products/filter${query ? `?${query}` : ''}`);
}

export function createAdminProduct(payload) {
  return apiClient('/products', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function updateAdminProduct(productId, payload) {
  return apiClient(`/products/${productId}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  });
}

export async function getAllAdminProducts(params = {}) {
  const pageSize = 200;
  const firstPage = await getAdminProducts({ ...params, page: 0, size: pageSize });
  if (Array.isArray(firstPage)) return firstPage;

  const items = [...(firstPage?.content || [])];
  const totalPages = Number(firstPage?.totalPages || 1);
  if (totalPages <= 1) return items;

  const remainingPages = await Promise.all(
    Array.from({ length: totalPages - 1 }, (_, index) => (
      getAdminProducts({ ...params, page: index + 1, size: pageSize })
    )),
  );
  return items.concat(remainingPages.flatMap((page) => page?.content || []));
}

export function updateProductStatus(productId, status) {
  return apiClient(`/products/${productId}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ status }),
  });
}

export function deleteAdminProduct(productId) {
  return apiClient(`/products/${productId}`, {
    method: 'DELETE',
  });
}

export function addProductImage(productId, payload) {
  return apiClient(`/products/${productId}/images`, {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function removeProductImage(productId, imageId) {
  return apiClient(`/products/${productId}/images/${imageId}`, {
    method: 'DELETE',
  });
}

export function setProductThumbnail(productId, imageId) {
  return apiClient(`/products/${productId}/images/${imageId}/thumbnail`, {
    method: 'PATCH',
  });
}

export function updateVariantPrice(productId, variantId, price) {
  return apiClient(`/products/${productId}/variants/${variantId}/price`, {
    method: 'PATCH',
    body: JSON.stringify({ price: Number(price) }),
  });
}

export function addVariantStock(productId, variantId, amount) {
  return apiClient(`/products/${productId}/variants/${variantId}/stock`, {
    method: 'PATCH',
    body: JSON.stringify({ amount: Number(amount) }),
  });
}

export function addProductVariant(productId, payload) {
  return apiClient(`/products/${productId}/variants`, {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function toggleProductFeatured(productId) {
  return apiClient(`/products/${productId}/featured`, {
    method: 'PATCH',
  });
}

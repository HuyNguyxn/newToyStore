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

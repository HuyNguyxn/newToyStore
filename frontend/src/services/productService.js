import { apiClient } from './apiClient.js';

function toQueryString(params) {
  const searchParams = new URLSearchParams();

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      searchParams.set(key, value);
    }
  });

  return searchParams.toString();
}

export function getProducts(params = {}) {
  const query = toQueryString(params);
  return apiClient(`/products${query ? `?${query}` : ''}`);
}

export function searchProducts(keyword, params = {}) {
  const query = toQueryString({ keyword, ...params });
  return apiClient(`/products/search?${query}`);
}

export function filterProducts(params = {}) {
  const query = toQueryString(params);
  return apiClient(`/products/filter${query ? `?${query}` : ''}`);
}

export function getProductsByCategory(categoryId, params = {}) {
  const query = toQueryString(params);
  return apiClient(`/products/category/${categoryId}${query ? `?${query}` : ''}`);
}

export function getProductDetails(productId) {
  return apiClient(`/products/${productId}`);
}

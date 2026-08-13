import { apiClient } from './apiClient.js';

const qs = (params = {}) => {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') search.set(key, value);
  });
  return search.toString();
};

export const getSuppliers = (params = {}) => apiClient(`/suppliers${qs(params) ? `?${qs(params)}` : ''}`);

export async function getAllSuppliers(params = {}) {
  const pageSize = 200;
  const firstPage = await getSuppliers({ ...params, page: 0, size: pageSize });
  if (Array.isArray(firstPage)) return firstPage;

  const items = [...(firstPage?.content || [])];
  const totalPages = Number(firstPage?.totalPages || 1);
  if (totalPages <= 1) return items;

  const remainingPages = await Promise.all(
    Array.from({ length: totalPages - 1 }, (_, index) => (
      getSuppliers({ ...params, page: index + 1, size: pageSize })
    )),
  );
  return items.concat(remainingPages.flatMap((page) => page?.content || []));
}
export const createSupplier = (payload) => apiClient('/suppliers', { method: 'POST', body: JSON.stringify(payload) });
export const updateSupplier = (id, payload) => apiClient(`/suppliers/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
export const changeSupplierStatus = (id, status) => apiClient(`/suppliers/${id}/status`, { method: 'PATCH', body: JSON.stringify({ status }) });
export const restoreSupplier = (id) => apiClient(`/suppliers/${id}/restore`, { method: 'PATCH' });
export const deleteSupplier = (id) => apiClient(`/suppliers/${id}`, { method: 'DELETE' });

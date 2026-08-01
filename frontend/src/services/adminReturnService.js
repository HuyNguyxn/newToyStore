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

export function getCustomerReturns(params = {}) {
  const query = toQueryString(params);
  return apiClient(`/api/returns${query ? `?${query}` : ''}`);
}

export function requireCustomerReturnInfo(returnId, adminMessage) {
  return apiClient(`/api/returns/${returnId}/require-info?adminMessage=${encodeURIComponent(adminMessage)}`, {
    method: 'PATCH',
  });
}

export function receiveCustomerReturn(returnId) {
  return apiClient(`/api/returns/${returnId}/receive`, {
    method: 'PATCH',
  });
}

export function inspectCustomerReturn(returnId, { isPassed, qcNote }) {
  return apiClient(`/api/returns/${returnId}/inspect?isPassed=${encodeURIComponent(isPassed)}&qcNote=${encodeURIComponent(qcNote)}`, {
    method: 'PATCH',
  });
}

export function resolveCustomerReturnDispute(returnId, { isApproved, resolutionNote }) {
  return apiClient(`/api/returns/${returnId}/resolve-dispute?isApproved=${encodeURIComponent(isApproved)}&resolutionNote=${encodeURIComponent(resolutionNote)}`, {
    method: 'PATCH',
  });
}

export function finalizeCustomerReturnRefund(returnId, note) {
  return apiClient(`/api/returns/${returnId}/finalize-refund?note=${encodeURIComponent(note)}`, {
    method: 'PATCH',
  });
}

export function getSupplierReturns(params = {}) {
  const query = toQueryString(params);
  return apiClient(`/api/supplier-returns${query ? `?${query}` : ''}`);
}

export function createSupplierReturn(payload) {
  return apiClient('/api/supplier-returns', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function submitSupplierReturn(id) {
  return apiClient(`/api/supplier-returns/${id}/submit`, { method: 'PATCH' });
}

export function approveSupplierReturn(id) {
  return apiClient(`/api/supplier-returns/${id}/approve`, { method: 'PATCH' });
}

export function rejectSupplierReturn(id, reason) {
  return apiClient(`/api/supplier-returns/${id}/reject?reason=${encodeURIComponent(reason)}`, { method: 'PATCH' });
}

export function shipSupplierReturn(id) {
  return apiClient(`/api/supplier-returns/${id}/ship`, { method: 'PATCH' });
}

export function completeSupplierReturn(id) {
  return apiClient(`/api/supplier-returns/${id}/complete`, { method: 'PATCH' });
}

export function inspectSupplierReturn(returnId, payload) {
  return apiClient(`/api/supplier-returns/${returnId}/inspect`, {
    method: 'PATCH',
    body: JSON.stringify(payload),
  });
}

export function getSupplierReturnCriticalAlerts() {
  return apiClient('/api/supplier-returns/sla/critical-alerts');
}

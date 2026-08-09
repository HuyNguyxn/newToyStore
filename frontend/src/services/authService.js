import { apiClient } from './apiClient.js';

export function loginUser(payload) {
  return apiClient('/users/login', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function registerUser(payload) {
  return apiClient('/users/register', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function getCurrentUser() {
  return apiClient('/users/me');
}

export function updateCurrentUser(payload) {
  return apiClient('/users/me', {
    method: 'PUT',
    body: JSON.stringify(payload),
  });
}

export function changeCurrentPassword(payload) {
  return apiClient('/users/me/password', {
    method: 'PATCH',
    body: JSON.stringify(payload),
  });
}

export function addCurrentAddress(payload) {
  return apiClient('/users/me/addresses', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function setCurrentDefaultAddress(addressId) {
  return apiClient(`/users/me/addresses/${addressId}/default`, {
    method: 'PATCH',
  });
}

export function removeCurrentAddress(addressId) {
  return apiClient(`/users/me/addresses/${addressId}`, {
    method: 'DELETE',
  });
}

export function requestPasswordReset(payload) {
  return apiClient('/users/forgot-password', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function resetPassword(payload) {
  return apiClient('/users/reset-password', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function verifyEmail(token) {
  return apiClient(`/users/verify?token=${encodeURIComponent(token)}`);
}

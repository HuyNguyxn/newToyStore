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

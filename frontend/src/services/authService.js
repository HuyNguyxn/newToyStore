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

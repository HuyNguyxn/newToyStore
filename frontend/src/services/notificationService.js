import { apiClient } from './apiClient.js';

export function getNotifications(params = {}) {
  const query = new URLSearchParams(params).toString();
  return apiClient(`/notifications${query ? `?${query}` : ''}`);
}

export function getUnreadNotificationCount() {
  return apiClient('/notifications/unread-count');
}

export function markNotificationAsRead(notificationId) {
  return apiClient(`/notifications/${notificationId}/read`, {
    method: 'PATCH',
  });
}

export function archiveNotification(notificationId) {
  return apiClient(`/notifications/${notificationId}/archive`, {
    method: 'PATCH',
  });
}

export function markAllNotificationsAsRead() {
  return apiClient('/notifications/read-all', {
    method: 'PATCH',
  });
}

export function getNotificationPreferences() {
  return apiClient('/notifications/preferences');
}

export function updateNotificationPreferences(payload) {
  return apiClient('/notifications/preferences', {
    method: 'PUT',
    body: JSON.stringify(payload),
  });
}

export function broadcastNotification(payload) {
  return apiClient('/notifications/broadcast', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

import { apiClient } from './apiClient.js';

function buildQueryString(params = {}) {
  const parts = [];
  Object.keys(params).forEach((key) => {
    const val = params[key];
    if (val !== undefined && val !== null && val !== '') {
      if (key === 'sort') {
        parts.push(`sort=${val}`);
      } else {
        parts.push(`${encodeURIComponent(key)}=${encodeURIComponent(val)}`);
      }
    }
  });
  return parts.length > 0 ? `?${parts.join('&')}` : '';
}

export function getNotifications(params = {}) {
  return apiClient(`/notifications${buildQueryString(params)}`);
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

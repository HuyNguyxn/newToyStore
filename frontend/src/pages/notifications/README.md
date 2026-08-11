# Notification Pages

## Responsibility

`NotificationPage.jsx` provides the authenticated customer's notification inbox at `/notifications`.

## Capabilities

- Load the current user's notifications.
- Mark individual notifications or the whole inbox as read.
- Archive individual notifications.
- Display navigation context associated with a notification.

Administrative broadcasts and preference controls live in `pages/admin` and use the same `notificationService.js` boundary.

## Maintenance notes

- Treat unread counts as server-owned state and refresh them after mutations.
- Validate notification targets before navigating to feature pages.
- Add pagination or incremental loading if inbox size grows substantially.

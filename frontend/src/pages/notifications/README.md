# Notification Pages

## Responsibility

`NotificationPage.jsx` provides the authenticated customer's notification inbox and preference controls at `/notifications`.

## Capabilities

- Load the current user's notifications.
- Mark individual or grouped notifications as read where supported.
- Display navigation context associated with a notification.
- Read and update notification preferences through `notificationService.js`.

Administrative notification operations live in `pages/admin` and use the same service boundary where applicable.

## Maintenance notes

- Treat unread counts as server-owned state and refresh them after mutations.
- Validate notification targets before navigating to feature pages.
- Keep preference labels synchronized with backend channels and event types.
- Add pagination or incremental loading if inbox size grows substantially.

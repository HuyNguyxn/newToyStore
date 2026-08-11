# Module: Notification

## 1. Purpose

Notification owns in-app notifications, read/archive lifecycle and per-user channel/category preferences; email delivery is delegated to infrastructure.

## 2. Package Structure

`api/` controller/advice; `application/` facade/services/maintenance, DTOs and event listeners; `domain/` notification/preference entities, repositories, enums/converters/exceptions; `mapper/` mapping.

## 3. Entities & Aggregates

- `Notification` — aggregate root, `notifications`, `BaseRootEntity`; recipient ID, type/status/reference, title/message/action URL, unique deduplication key, occurrence/read/expiry times.
- `NotificationPreference` — aggregate root, `notification_preferences`, `BaseRootEntity`; user ID and in-app/email plus category toggles.

## 4. Relationships

No JPA relationships. Recipient and referenced records are IDs; `NotificationReferenceType` supplies action-path metadata.

## 5. Domain Dependencies & Communication

Notification resolves recipients through UserFacade. Listeners consume Cart, Order, Payment, Shipment, Customer Return and Review events. Email requests are published as `NotificationEmailRequestedEvent` and handled by MailService.

## 6. Main Flows / Use Cases

`Business event -> derive type/reference/deduplication key -> read/create preferences -> persist in-app notification if enabled -> request email if enabled`. User actions mark one/all read or archive; admin broadcast creates deduplicated notices for recipients.

## 7. Business Rules

Broadcast key/title/message are required and bounded. All preference booleans are required. Status transitions are `UNREAD -> READ -> ARCHIVED`, with direct `UNREAD -> ARCHIVED` also allowed. Type/category preferences decide delivery; deduplication keys prevent repeated event notices.

## 8. Persistence & Data Strategy

Soft delete/versioning on both roots. Pageable specification filters, unread counts, bulk mark-read and bulk expiry soft delete. Scheduled retention cleanup uses configured days/cron. No entity relationships are loaded for enrichment.

## 9. Transaction Strategy

Creation/user actions/preference updates/broadcast/cleanup are transactional; query paths are read-only where declared. Event listeners invoke local application services; email is event-triggered, not durably queued.

## 10. API Design

Current-user resource API plus admin broadcast; DTOs, Page and direct responses.

## 11. APIs

`NotificationController`, base `/notifications`: `GET /`, `/unread-count`, `/{id}`, `/preferences`; `PATCH /{id}/read`, `/{id}/archive`, `/read-all`; `PUT /preferences`; `POST /broadcast`.

## 12. Error Handling

Missing/deleted, invalid operation and access errors are handled by `NotificationExceptionHandler`.

## 13. Security & Authorization

All notification routes require authentication. Broadcast has `@PreAuthorize(MANAGER, ADMIN)`, while the URL rule says ADMIN only; both checks apply, so effective access is ADMIN-only.

## 14. Algorithms & Performance Considerations

Deduplication avoids duplicate event fan-out; bulk database updates handle read-all and expiry cleanup. Broadcast size depends on recipient lookup; no external queue/batch worker was found.

## 15. Architecture & Design Principles

Event listeners make Notification a downstream module; reference IDs and URLs avoid importing source entities.

## 16. Notes / Design Decisions

Notification type carries category and default email behavior; user preferences can disable channels/categories.

## 17. Known Limitations / Technical Debt

Method and URL broadcast role policies disagree (effective ADMIN-only), and email/event delivery has no durable queue or outbox.

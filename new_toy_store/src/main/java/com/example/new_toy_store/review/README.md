# Module: Review

## 1. Purpose

Review owns verified-purchase product reviews, attached media, public visibility, moderation state and administrative replies.

## 2. Package Structure

`api/` controller/advice; `application/` service and DTOs; `domain/` Review/ReviewMedia, repository, enums/exceptions; `mapper/` mapping.

## 3. Entities & Aggregates

- `Review` — aggregate root, table `reviews`, `BaseRootEntity`; user/product/order-item IDs, variant snapshot, rating, comment, reply, status and media.
- `ReviewMedia` — entity, table `review_media`, `BaseSoftDeleteEntity`; type (`IMAGE` or `VIDEO`), URL and display order.

## 4. Relationships

Review 1-N ReviewMedia uses `cascade = ALL`, `orphanRemoval = true`; media has LAZY `@ManyToOne`. User/Product/OrderItem remain ID references.

## 5. Domain Dependencies & Communication

Review uses Order to prove the completed order item, Product for product data/rating updates, User for identity and Moderation to check content. Review events update Product rating and Notifications.

## 6. Main Flows / Use Cases

`ReviewCreateRequest -> resolve user/completed order item -> reject duplicate -> moderate text -> snapshot variant -> attach bounded media -> save -> recalculate/publish product rating`. Update/delete/status/reply publish corresponding events.

## 7. Business Rules

Rating is 1–5; comment/reply <=1000; up to five image URLs and two video URLs, each HTTP(S) and <=1000. Only a completed eligible order item can be reviewed, and duplicate review conflicts are rejected. Statuses `PUBLISHED` and `HIDDEN` control public visibility and enum transitions.

## 8. Persistence & Data Strategy

Soft delete/versioning, child soft delete, specification/pageable filtering, duplicate/summary/aggregate rating queries. Variant details are snapshotted. Product rating is denormalized via event.

## 9. Transaction Strategy

Create/update/delete/status/reply are transactional; list/summary queries are read-only where declared. Product rating listener uses BEFORE_COMMIT.

## 10. API Design

Customer CRUD and public product list/summary are separated from administrative filters/actions. Direct DTO responses and Page are used.

## 11. APIs

`ReviewController`, base `/reviews`: `POST /`, `PUT /{id}`, `DELETE /{id}`, `GET /me`; public/authenticated product `GET /products/{productId}/summary` and `/products/{productId}`; staff `GET /admin/products/{productId}`, `/admin/all`, `PATCH /admin/{id}/status`, `/admin/{id}/reply`.

## 12. Error Handling

Missing, conflict, invalid operation and access errors are mapped by `ReviewExceptionHandler`.

## 13. Security & Authorization

No explicit public review matcher exists; fallback authentication applies to non-admin review reads/writes. Admin endpoints require STAFF/MANAGER/ADMIN through `@PreAuthorize`.

## 14. Algorithms & Performance Considerations

Moderation scans cached blacklist data; rating summary uses repository aggregation rather than loading all reviews. Media count is bounded.

## 15. Architecture & Design Principles

External records are IDs/snapshots. Facades and events avoid cross-domain repositories, though the service imports other application/domain DTOs.

## 16. Testing Strategy

No tests were found. Recommended: purchase eligibility, duplicate reviews, media bounds, moderation decisions, rating aggregation and ownership.

## 17. Notes / Design Decisions

Variant attributes are snapshotted so a review continues to describe the purchased variant after catalog edits.

## 18. Known Limitations / Technical Debt

Review product reads are not public in security configuration, and no automated tests were found.

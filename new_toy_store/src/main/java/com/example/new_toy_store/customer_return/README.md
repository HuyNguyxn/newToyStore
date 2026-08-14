
# Module: Customer Return

## 1. Purpose

Customer Return owns post-sale return requests, items, evidence, status history, inspection/dispute outcomes and refund finalization.

## 2. Package Structure

`api/` controller/advice; `application/` DTOs, facade, service and logistics listener; `domain/` aggregate, child entities, repository, enums/converter/exceptions; `mapper/` response mapping.

## 3. Entities & Aggregates

- `CustomerReturn` — aggregate root, `customer_returns`, `BaseRootEntity`; order ID, shipping fee, status, notes/deadline/risk flag and owned items/images/history.
- `CustomerReturnItem` — `customer_return_items`; order-item/product/variant IDs, quantity, reason and expected refund.
- `CustomerReturnImage` — `customer_return_images`; evidence URL.
- `CustomerReturnHistory` — `customer_return_histories`; old/new state, actor, date and note.

All children extend `BaseSoftDeleteEntity`.

## 4. Relationships

CustomerReturn owns three LAZY child collections using `cascade = ALL` and `orphanRemoval = true`; children use `@ManyToOne`. Order, product and variant are loose ID references.

## 5. Domain Dependencies & Communication

OrderFacade validates ownership/order items and computes risk; LogisticsFacade creates/observes return shipments. Status events request payment refunds and sellable-stock restoration. Shipment events move the return through transit/receipt/failure states.

## 6. Main Flows / Use Cases

`Request -> validate customer/order/items and duplicate active return -> create items/evidence/history -> arrange return shipment -> receive -> inspect -> dispute/resolve if needed -> finalize refund -> publish stock/refund events`.

## 7. Business Rules

Requests require an order and non-empty items; item quantity is at least 1, expected refund non-negative, evidence is limited to five URLs and notes to 500 characters. Only one active return per order is permitted. `CustomerReturnStatus` encodes transitions across `REQUESTED`, `NEEDS_MORE_INFO`, `APPROVED`, `RETURNING`, `SHIPPING_FAILED`, `RECEIVED`, inspection outcomes, `DISPUTED`, terminal rejection/cancellation/refund/replacement. Reason codes determine sellability.

## 8. Persistence & Data Strategy

Soft delete/versioning applies to the aggregate, soft delete to children. Specifications and paging support filtering; queries find active duplicates and expired information requests. No explicit lock annotation is present in the repository.

## 9. Transaction Strategy

Each lifecycle command is transactional; filter queries are read-only. Events connect refund, logistics and inventory work; AFTER_COMMIT listeners mean some follow-up work is eventually consistent within the monolith.

## 10. API Design

Action-oriented PATCH endpoints reflect the state machine. DTOs and pageable filtering are used; no version prefix or wrapper.

## 11. APIs

`CustomerReturnController`, base `/api/returns`: `GET /`; `POST /`; customer/staff `PATCH /{id}/cancel|update-info|dispute`; staff `PATCH /{id}/require-info|receive|inspect|resolve-dispute|finalize-refund`.

## 12. Error Handling

Exceptions cover not found, duplicate request, invalid data/operation/transition, deleted conflicts and access denial. `CustomerReturnExceptionHandler` maps them.

## 13. Security & Authorization

GET permits authenticated customer/staff roles; POST is CUSTOMER-only; customer-safe PATCH routes allow customers and staff; operational lifecycle routes require STAFF/MANAGER/ADMIN. Ownership is resolved in controller/service logic.

## 14. Algorithms & Performance Considerations

Risk classification and returned-quantity aggregation use order snapshots/maps. Scheduled expiry handling searches overdue `NEEDS_MORE_INFO` requests. No unusual structure beyond aggregate collections and specifications.

## 15. Architecture & Design Principles

The aggregate owns its evidence/history while external identities remain IDs. Events separate physical logistics, payment refund and inventory restoration.

## 16. Notes / Design Decisions

`expectedRefundAmount` is stored per item, and history records actor/status changes. Sellable return reasons drive stock restoration rather than directly changing inventory inside this module.

## 17. Known Limitations / Technical Debt

No locking or retry policy was found for concurrent lifecycle operations.

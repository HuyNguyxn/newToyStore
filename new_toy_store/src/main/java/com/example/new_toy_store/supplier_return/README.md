# Module: Supplier Return

## 1. Purpose

Supplier Return owns store-to-supplier return drafts, approval, stock deduction, shipment progress, inspection and completion.

## 2. Package Structure

`api/` controller/advice; `application/` service, facade, DTOs and logistics listener; `domain/` aggregate/children/repository/status/reason/converter/exceptions; `mapper/` mappings.

## 3. Entities & Aggregates

- `SupplierReturn` — aggregate root, `supplier_returns`, `BaseRootEntity`; supplier/import IDs, status, costs/refund total/note, items/images/history.
- `SupplierReturnItem` — `supplier_return_items`; product/variant IDs, product snapshot, requested/accepted quantity, price/discount/reason/batch/expiry.
- `SupplierReturnImage` — `supplier_return_images`; evidence URL.
- `SupplierReturnHistory` — `supplier_return_histories`; old/new status, actor and note.

Children extend `BaseSoftDeleteEntity`.

## 4. Relationships

SupplierReturn owns items/images/history with `cascade = ALL`, `orphanRemoval = true`; children use LAZY `@ManyToOne`. Supplier, import, product and variant are ID references.

## 5. Domain Dependencies & Communication

Supplier and Import facades validate sources. Logistics creates/updates the outbound shipment. Product listens to supplier-return completion to adjust inventory batches/stock. The module publishes status/completion events.

## 6. Main Flows / Use Cases

`Create DRAFT -> submit PENDING_APPROVAL -> approve/reject -> ship and deduct stock -> logistics events -> inspect accepted quantities -> complete and publish event`. A scheduler queries critical SLA alerts.

## 7. Business Rules

Supplier and non-empty items are required; quantities >=1; prices/discounts/fees non-negative; batch number and expiry date required. Inspection accepted quantity is non-negative and cannot exceed returned quantity. Statuses: `DRAFT`, `PENDING_APPROVAL`, `APPROVED`, `SHIPPED`, `SHIPPING_FAILED`, `COMPLETED`, `REJECTED`, `CANCELLED`; enum controls transitions/read-only state. Reason codes determine restockability.

## 8. Persistence & Data Strategy

Root soft delete/versioning and child soft delete. Specifications, paging, status/SLA queries and duplicate-return checks are present. No pessimistic lock annotation was found.

## 9. Transaction Strategy

Every command is transactional; reads are read-only where declared. Logistics and stock are coordinated by facades/events inside the modular monolith.

## 10. API Design

Action-oriented state-machine endpoints and pageable filtering. DTOs are separated from entities.

## 11. APIs

`SupplierReturnController`, base `/api/supplier-returns`: `GET /sla/critical-alerts`, `GET /`, `GET /{id}`, `POST /`, then `PATCH /{id}/submit|approve|reject|ship|inspect|complete`.

## 12. Error Handling

Exceptions cover duplicate/missing/deleted return, invalid operation and access denial; `SupplierReturnExceptionHandler` maps them.

## 13. Security & Authorization

No module-specific `@PreAuthorize` annotations were found on this controller. Because `SecurityConfig` has no explicit supplier-return matcher, endpoints fall through to `authenticated()` rather than documented staff/manager role restrictions.

## 14. Algorithms & Performance Considerations

Totals and accepted quantities are aggregate calculations. SLA scheduler/classification identifies warning/critical overdue returns from configured timing.

## 15. Architecture & Design Principles

The return aggregate owns evidence/history and references other domains by ID. Events separate logistics and inventory reactions.

## 16. Notes / Design Decisions

Batch number and expiry are snapshotted per return line so the exact inbound lot can be identified.

## 17. Known Limitations / Technical Debt

Only generic authentication protects the controller; an explicit role policy is absent.

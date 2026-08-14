
# Module: Imports

## 1. Purpose

Imports owns inbound supplier notes and their line-item cost/quantity snapshots. Completion feeds inventory and supplier payables.

## 2. Package Structure

`api/` controller/advice; `application/` `ImportService`, facade and DTOs; `domain/` aggregate/repository/status/exceptions; `mapper/` mapping.

## 3. Entities & Aggregates

- `ImportNote` — aggregate root, table `import_notes`, `BaseRootEntity`; supplier ID, status, total amount, note and items.
- `ImportNoteItem` — table `import_note_items`, `BaseTimeEntity`; product/variant IDs, product-name snapshot, quantity and import price.

## 4. Relationships

ImportNote 1-N ImportNoteItem is LAZY through the child's `@ManyToOne`; root collection uses `cascade = ALL`, `orphanRemoval = true`. Supplier/product/variant are ID references.

## 5. Domain Dependencies & Communication

Imports validates Supplier via `SupplierFacade` and Product variants via `ProductFacade`. Completion publishes `ImportNoteCompletedEvent`; Product increases stock/batches and Supplier Payment creates a payable.

## 6. Main Flows / Use Cases

`ImportNoteRequest -> validate supplier/items -> snapshot product and prices -> create PENDING note -> complete -> atomic status update -> publish completion payload -> inventory/payable listeners`.

## 7. Business Rules

Supplier is required; items are non-empty; product/variant/name are required; quantity >= 1; import price >= 0. `PENDING` permits item modification and transition to `COMPLETED` or `CANCELLED`; terminal states cannot be changed through the normal workflow. Duplicate active-note rules are represented by `DuplicateActiveImportNoteException`.

## 8. Persistence & Data Strategy

Root soft delete/versioning, child timestamps. Specifications/pageable search, fetch-with-items, version-checked status updates and inventory movement aggregate queries are present.

## 9. Transaction Strategy

Create/complete/cancel are transactional; reads are read-only where declared. BEFORE_COMMIT inventory listeners participate in completion; supplier-payment listener creates the payable from the event.

## 10. API Design

Staff-only pageable CRUD/action API with request/response DTOs and validation.

## 11. APIs

`ImportController`, base `/imports`: `GET /`, `GET /{id}`, `POST /`, `PATCH /{id}/complete`, `PATCH /{id}/cancel`.

## 12. Error Handling

Not found, duplicate active, deleted/version conflict, invalid data/operation, access denial and cross-module exceptions are handled by `ImportExceptionHandler`.

## 13. Security & Authorization

The controller is restricted to STAFF/MANAGER/ADMIN with class-level `@PreAuthorize`.

## 14. Algorithms & Performance Considerations

Completion emits a compact list of item payloads for batch stock processing. Repository SQL aggregates inbound/outbound movements for statistics.

## 15. Architecture & Design Principles

The note is an aggregate and uses snapshots/IDs. Completion events decouple inventory and payable side effects.

## 16. Notes / Design Decisions

Product name and import price are captured on the line so receipt history does not depend on future catalog changes.

## 17. Known Limitations / Technical Debt

No durable event/outbox mechanism was found.

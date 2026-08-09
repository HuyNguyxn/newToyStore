# Module: Warehouse

## 1. Purpose

Warehouse is an application-facing view over Imports and Product operations for staff. It owns no separate entity or repository.

## 2. Package Structure

```text
warehouse/
|- api/WarehouseController.java
|- application/WarehouseService.java
`- README.md
```

## 3. Entities & Aggregates

N/A. It returns `ImportNoteResponse` and `ProductResponse` from source modules.

## 4. Relationships

N/A at JPA level.

## 5. Domain Dependencies & Communication

Warehouse calls `ImportService` to list/detail/complete/cancel inbound batches and `ProductFacade` to publish a product associated with a batch.

## 6. Main Flows / Use Cases

`Staff warehouse request -> validate/load import batch -> delegate completion/cancellation`; publishing loads batch/product context and delegates product status/publication behavior.

## 7. Business Rules

No independent aggregate invariants. Import status and Product status rules remain authoritative in their modules.

## 8. Persistence & Data Strategy

No owned persistence. Pageable import queries and source DTOs are reused.

## 9. Transaction Strategy

List/detail use read-only transactions; complete/cancel/publish use `@Transactional` around delegated calls.

## 10. API Design

Staff resource/action facade, without separate warehouse DTOs or version prefix.

## 11. APIs

`WarehouseController`, base `/warehouse/batches`: `GET /`, `GET /{batchId}`, `PATCH /{batchId}/complete`, `PATCH /{batchId}/cancel`, `PATCH /{batchId}/products/{productId}/publish`.

## 12. Error Handling

No warehouse-specific exceptions/advice; Import/Product/global handlers determine responses.

## 13. Security & Authorization

The controller requires STAFF, MANAGER or ADMIN with class-level `@PreAuthorize`.

## 14. Algorithms & Performance Considerations

No special algorithm; it delegates to existing pageable/fetch logic.

## 15. Architecture & Design Principles

Warehouse is an application composition module rather than a data-owning domain. It reuses public services/facades, though direct `ImportService` dependency is tighter than facade-only communication.

## 16. Testing Strategy

No tests were found. Recommended: authorization and correct delegation/transaction behavior.

## 17. Notes / Design Decisions

Warehouse terminology presents import notes as batches without duplicating the underlying model.

## 18. Known Limitations / Technical Debt

The service depends directly on another module's application service and reuses its DTOs; no tests were found.

# Module: Supplier Payment

## 1. Purpose

Supplier Payment owns accounts-payable invoices created from completed import notes and the payment transactions applied to them.

## 2. Package Structure

`api/` controller/advice; `application/` service, facade, DTOs and import listener; `domain/` invoice/transaction entities, repository, enums/exceptions; `mapper/` mapping.

## 3. Entities & Aggregates

- `SupplierPaymentInvoice` — aggregate root, table `supplier_payment_invoices`, `BaseRootEntity`; supplier/import IDs, unique invoice code, status, total/paid amounts, due date/note and transactions.
- `SupplierPaymentTransaction` — entity, table `supplier_payment_transactions`, `BaseRootEntity`; invoice relation, amount, method, reference code, paid date and note.

## 4. Relationships

Invoice 1-N Transaction uses `cascade = ALL`, `orphanRemoval = true`; transaction has LAZY `@ManyToOne`. Supplier and import note are scalar ID references.

## 5. Domain Dependencies & Communication

Supplier/Import facades validate and enrich IDs. `SupplierPaymentImportListener` consumes `ImportNoteCompletedEvent` and calls an idempotent create-if-missing operation.

## 6. Main Flows / Use Cases

`Completed import -> find/create invoice -> record payment -> validate positive amount and remaining balance -> append transaction -> update paid amount/status -> response`. Cancellation records a bounded reason/note and performs an allowed status transition.

## 7. Business Rules

Payments must be at least 0.01 and specify `CASH`, `BANK_TRANSFER` or `OTHER`; reference/note lengths are bounded. Invoice states: `PENDING`, `PARTIALLY_PAID`, `PAID`, `OVERDUE`, `CANCELLED`; enum transitions and remaining-balance rules prevent overpayment/invalid changes.

## 8. Persistence & Data Strategy

Aggregate and transactions are soft-deletable/versioned. Specifications and pagination support filtering; repository duplicate checks enforce one payable per import. No special lock annotation was found.

Important: the checked-in Flyway migrations do not create either supplier-payment table, although JPA maps both and `ddl-auto: update` may create them at runtime.

## 9. Transaction Strategy

Invoice creation, event handling, payment recording and cancellation are transactional; list/detail queries are read-only.

## 10. API Design

Manager/admin action API with DTO validation and pageable filtering.

## 11. APIs

`SupplierPaymentController`, base `/api/supplier-payments`: `GET /`, `GET /{id}`, `POST /imports/{importNoteId}`, `PATCH /{id}/payments`, `PATCH /{id}/cancel`.

## 12. Error Handling

Exceptions cover duplicate/missing/deleted payments, invalid operations and domain failures; `SupplierPaymentExceptionHandler` maps them.

## 13. Security & Authorization

All endpoints require MANAGER or ADMIN through class-level `@PreAuthorize`.

## 14. Algorithms & Performance Considerations

Create-if-missing makes the import event handler retry-tolerant at the application level. No explicit concurrent row lock was found for recording payments.

## 15. Architecture & Design Principles

The invoice is the aggregate boundary; external records are IDs/facade lookups, while its transaction children are tightly owned.

## 16. Notes / Design Decisions

The payable is derived from the completed inbound note rather than independently entered.

## 17. Known Limitations / Technical Debt

- Missing Flyway DDL for mapped tables.
- No explicit locking was found for concurrent payment recording.

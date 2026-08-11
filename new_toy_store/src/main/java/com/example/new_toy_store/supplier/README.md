# Module: Supplier

## 1. Purpose

Supplier owns supplier master data and availability for imports/product assignment. It does not own inbound notes, payables or returns.

## 2. Package Structure

`api/` controller/advice; `application/` service, facade and DTOs; `domain/` Supplier, repository, status and exceptions; `mapper/` mapping.

## 3. Entities & Aggregates

`Supplier` is an aggregate root mapped to `suppliers` and extends `BaseRootEntity`. Main fields are `id`, `name`, `phoneNumber`, `email`, `address`, `status` and inherited audit/delete/version fields.

## 4. Relationships

No JPA relationship to other modules exists. Product, Import and Supplier Return store `supplierId`.

## 5. Domain Dependencies & Communication

Supplier imports no other business module. It exposes `SupplierFacade` for required/batch lookup and publishes deleted/status-change events.

## 6. Main Flows / Use Cases

Create/update validates duplicate contact identity and fields; status change delegates allowed transitions to `SupplierStatus`; delete/restore use version-aware soft deletion and publish events.

## 7. Business Rules

Name and phone are required and bounded; email, when provided, is valid and <=150 characters; address <=500. Statuses: `ACTIVE`, `SUSPENDED`, `BLACKLISTED`; enum capabilities decide whether importing/product assignment is allowed and which transitions are valid.

## 8. Persistence & Data Strategy

Soft delete and optimistic versioning apply. Specifications/pageable filtering and batch ID loading are used. Repository queries enforce duplicate checks and version-aware changes.

## 9. Transaction Strategy

Mutations are transactional; details, filters and facade lookups use read-only transactions.

## 10. API Design

Staff-oriented pageable REST API with DTO isolation and Bean Validation.

## 11. APIs

`SupplierController`, base `/suppliers`: `GET /`, `GET /{id}`, `POST /`, `PUT /{id}`, `PATCH /{id}/status`, `PATCH /{id}/restore`, `DELETE /{id}`.

## 12. Error Handling

`SupplierNotFoundException`, `DuplicateSupplierException`, invalid operation, deleted conflict, access denial and domain errors are mapped by `SupplierExceptionHandler`.

## 13. Security & Authorization

All routes require STAFF/MANAGER/ADMIN; delete is further restricted to MANAGER/ADMIN.

## 14. Algorithms & Performance Considerations

No special algorithm beyond specifications and batch lookup.

## 15. Architecture & Design Principles

Supplier has a clean ID-based boundary and a facade used by dependent modules.

## 16. Notes / Design Decisions

Availability rules live in `SupplierStatus`, keeping them near the domain model.

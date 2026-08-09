# Module: Admin

## 1. Purpose

Admin is a thin application-composition module that aggregates menu badge counts for the administrative UI. It owns no business entity or repository.

## 2. Package Structure

```text
admin/
|- api/AdminBadgeController.java
|- application/dto/response/AdminMenuBadgeResponse.java
`- README.md
```

## 3. Entities & Aggregates

N/A. No entity, aggregate, value object or table exists.

## 4. Relationships

N/A at JPA level.

## 5. Domain Dependencies & Communication

The controller calls application facades from Order, Product, Imports, Customer Return and Supplier Return to collect operational counts. This is synchronous in-process composition.

## 6. Main Flows / Use Cases

`Authenticated admin/staff request -> query each source facade -> assemble AdminMenuBadgeResponse`.

## 7. Business Rules

No independent domain invariant/state machine. Counts and their semantics are supplied by source modules.

## 8. Persistence & Data Strategy

No persistence, delete strategy, locking or pagination. Multiple source queries are issued for one response.

## 9. Transaction Strategy

No transaction annotation is declared in this module; called facade methods define their own read boundaries.

## 10. API Design

Single read endpoint returning a dedicated response DTO.

## 11. APIs

`AdminBadgeController`: `GET /api/admin/menu-badges` returns `AdminMenuBadgeResponse`.

## 12. Error Handling

No module-specific exceptions/handler. Source-module or global exception handling applies.

## 13. Security & Authorization

Requires ADMIN, STAFF or MANAGER via `@PreAuthorize`; `/admin/**` URL rules also apply to paths beginning `/api/admin` only through method security, not the `/admin/**` matcher.

## 14. Algorithms & Performance Considerations

Simple aggregation; response latency is the sum/maximum of several sequential in-process database queries.

## 15. Architecture & Design Principles

This is an application-layer read composition, not a DDD domain. It uses public facades instead of cross-domain repositories.

## 16. Testing Strategy

No tests were found. Recommended: authorization, empty counts and source failure behavior.

## 17. Notes / Design Decisions

Keeping dashboard composition outside source domains avoids assigning dashboard ownership to one transactional aggregate.

## 18. Known Limitations / Technical Debt

Sequential cross-module queries can make the endpoint sensitive to slow aggregates; no tests were found.

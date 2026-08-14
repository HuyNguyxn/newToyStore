
# Module: Promotion

## 1. Purpose

Promotion owns discount definitions, applicability, activation and usage counts for product, order and shipping scopes.

## 2. Package Structure

`api/` controller/advice; `application/` service, facade and DTOs; `domain/` Promotion, repository, type/scope strategies and exceptions; `mapper/` mapping.

## 3. Entities & Aggregates

`Promotion` is an aggregate root mapped to `promotions` and extends `BaseRootEntity`. Fields include code/name, `PromotionType`, `PromotionScope`, discount value/cap, minimum order value, target product ID, usage limit/count, start/end dates and active flag.

## 4. Relationships

No JPA relationship to Product or Order exists. Product targeting uses `targetProductId`; orders carry the promotion code.

## 5. Domain Dependencies & Communication

Promotion imports no other business module. `PromotionFacade` is used by Product, Cart and Order. It publishes state/usage events.

## 6. Main Flows / Use Cases

`PromotionRequest -> validate type/scope setup and unique code -> save`; calculation selects scope-specific eligible promotion and delegates math to `PromotionType`; consume/release updates usage with version-aware persistence.

## 7. Business Rules

Codes are required uppercase letters/numbers/underscore and <=50; name required <=255; values/caps/minimums non-negative; product target ID positive. Percentage and fixed-amount strategies calculate differently. Scope validates required/forbidden setup. Active time window and usage limit determine applicability; duplicate active promotion rules are enforced.

## 8. Persistence & Data Strategy

Soft delete/optimistic versioning; specifications/paging; queries for active product promotions and batched target IDs; version-checked active/usage/delete updates.

## 9. Transaction Strategy

Create/update/activate/deactivate/delete/consume/release are transactional; lookup and calculation paths are read-only where declared.

## 10. API Design

Versioned `/api/v1` resource/action API with DTO validation, paging and direct DTO/scalar responses.

## 11. APIs

`PromotionController`, base `/api/v1/promotions`: create/update/get/by-code/list; activate/deactivate/delete; consume/release; calculate product/order/shipping discounts; batch `POST /active-for-products`.

## 12. Error Handling

Not found, duplicate active, invalid data/operation, deleted conflict, access and cross-module exceptions are handled by `PromotionExceptionHandler`; the controller also contains a runtime logic-exception handler.

## 13. Security & Authorization

No explicit promotion URL rules or method annotations were found; all routes therefore require authentication through the fallback rule, including management and calculation endpoints.

## 14. Algorithms & Performance Considerations

Discount calculation uses enum strategy methods; batched product promotion lookup avoids per-product queries. Usage updates are optimistic/version-checked.

## 15. Architecture & Design Principles

Type and scope behavior lives in enums, and ID targeting avoids Product entity coupling. The facade is the cross-module application boundary.

## 16. Notes / Design Decisions

One model supports product, whole-order and shipping discounts; nullable setup fields are validated according to scope/type.

## 17. Known Limitations / Technical Debt

Management endpoints lack explicit role restrictions.

# Module: Product

## 1. Purpose

Product owns catalog products, variants, dynamic attributes, images and sellable inventory. Category and Supplier identities are referenced/validated across module boundaries.

### Responsibilities

- Manage product information, categories, supplier assignment, status and featured flag.
- Manage variants, attribute values, images and thumbnails.
- Track stock, reservations and expiry-dated inventory batches.
- Apply imported stock, order deduction/cancellation restoration and return adjustments.

### Out of Scope

Category hierarchy, supplier master data, orders, payments and promotions are owned elsewhere.

## 2. Package Structure

```text
product/
|- api/                    ProductController, InventoryController, advice
|- application/            DTOs, facade, services and event listeners
|- domain/                 entities, repositories, enums and exceptions
|- mapper/                 ProductMapper
`- README.md
```

## 3. Entities & Aggregates

- `Product` — aggregate root, table `products`, `BaseRootEntity`; owns name, base price, status, supplier ID, rating counters, featured flag, categories, variants and images.
- `ProductVariant` — aggregate/entity, table `product_variants`, `BaseRootEntity`; type, sale price, cost price, product, inventory and attribute values.
- `Inventory` — entity, table `inventories`, `BaseSoftDeleteEntity` plus its own `@Version`; stock/reserved quantities, variant and batches.
- `InventoryBatch` — entity, table `inventory_batches`, `BaseSoftDeleteEntity` plus `@Version`; batch number, expiry date, quantity and inventory.
- `ProductAttributeValue` — entity, table `product_attribute_values`; dynamic name/value attached to a variant.
- `ProductImage` — entity, table `product_images`; URL, thumbnail flag and product.

## 4. Relationships

- Product N-N Category via `product_categories` (`@ManyToMany`). This is direct cross-domain entity coupling.
- Product 1-N ProductVariant and ProductImage: `cascade = ALL`, `orphanRemoval = true`.
- ProductVariant 1-1 Inventory and 1-N ProductAttributeValue: cascaded, orphan-removing, LAZY where configured.
- Inventory 1-N InventoryBatch: LAZY, cascaded, orphan-removing.
- Supplier is only `supplierId`, preserving a loose boundary.

## 5. Domain Dependencies & Communication

Product calls Category, Supplier and Promotion facades for validation/enrichment/discount views. It listens to import, supplier-return, customer-return and review-rating events. Order and Cart call `ProductFacade`. The Category JPA relationship is the notable direct entity coupling.

## 6. Main Flows / Use Cases

### Create Product

```text
CreateProductRequest -> validate categories/supplier/status
-> create Product and default/declared variants
-> create inventory and attributes -> save -> ProductResponse
```

### Stock Movement

```text
Import/return/order event or command -> resolve variant/inventory
-> validate quantity -> update inventory/batches with versioning
-> update product availability when applicable -> persist
```

## 7. Business Rules

### 7.1 Validation Rules

Name is required (maximum 255), prices/costs and initial stock are non-negative, at least one category is required, supplier is required, added stock is at least 1, and variant attribute maps are required for non-default variants.

### 7.2 Invariants

Stock cannot be deducted below available quantity. Variant type controls whether attributes can be added and which type transitions are legal. Product status controls visibility/purchasability. A product must retain valid category and supplier references during create/update.

### 7.3 State Transitions

Product states are `ACTIVE`, `INACTIVE`, `OUT_OF_STOCK`; valid next states are encoded in `ProductStatus`. Variant types are `DEFAULT`, `MASTER`, `REGULAR` and have enum-defined transition/attribute capabilities.

## 8. Persistence & Data Strategy

All mapped product entities are soft-deletable; roots and inventory records use optimistic versions. Repositories provide specifications, pageable search, fetch queries and batch `IN` loading. Hibernate batch fetching is globally configured. Inventory batch reads support expiry-aware availability. No pessimistic inventory lock was found.

## 9. Transaction Strategy

All product/stock mutations are transactional; catalog queries are read-only. BEFORE_COMMIT listeners apply import/supplier-return stock and review rating changes in the publisher transaction; customer-return restocking is AFTER_COMMIT and begins its own service transaction.

## 10. API Design

Separate DTOs, pageable catalog/search/filter endpoints and command-specific PATCH operations are used. Public reads and staff writes are controlled globally. Responses are direct DTOs, without a standard wrapper or version prefix.

## 11. APIs

`ProductController`, base `/products`:

- `GET /`, `/category/{categoryId}`, `/search`, `/filter`, `/{id}` — catalog queries.
- `POST /` and `POST /{id}/variants` — create product/variant.
- `PUT /{id}` — update product info.
- `PATCH /{id}/status`, `/{id}/featured` — lifecycle/display changes.
- `POST /{id}/images`, `DELETE /{id}/images/{imageId}`, `PATCH /{productId}/images/{imageId}/thumbnail` — media.
- `PATCH /{productId}/variants/{variantId}/price` and `/stock` — variant changes.
- `DELETE /{id}` — soft-delete product.

`InventoryController`, base `/inventory`: `GET /variants/{variantId}/batches` returns available batches.

## 12. Error Handling

`ProductNotFoundException`, `InvalidProductOperationException` and `ProductDomainException` are handled by `ProductExceptionHandler`; external failures may be translated by facade/service code or global advice.

## 13. Security & Authorization

Product GET routes are public. POST/PUT/PATCH require STAFF/MANAGER/ADMIN; DELETE requires ADMIN. The inventory batch endpoint falls through to authenticated access because no explicit matcher is present.

## 14. Algorithms & Performance Considerations

Batch ID loading and map lookup support order/cart enrichment. Inventory batches enable expiry-aware/FIFO-style selection where service code requests available batches. Aggregate statistics execute in repository queries. `@Version` detects concurrent inventory writes.

## 15. Architecture & Design Principles

Product presents an application facade and owns its variant/inventory lifecycle. Scalar supplier IDs and events reduce coupling; the direct N-N Category entity relation is an exception to domain isolation.

## 16. Notes / Design Decisions

Dynamic variant attributes are represented by `ProductAttributeValue` rows rather than fixed columns. Rating is denormalized on Product and refreshed from review events.

## 17. Known Limitations / Technical Debt

- Category is directly mapped as a cross-domain JPA entity.
- Inventory has optimistic locking but no detected retry policy for conflicts.

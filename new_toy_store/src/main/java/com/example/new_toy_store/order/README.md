# Module: Order

## 1. Purpose

Order owns the commercial order record, item snapshots, history and lifecycle. It coordinates Product, Promotion and User data but does not own their entities; payment and shipment details remain in their modules.

### Responsibilities

- Create orders and immutable purchase-time item snapshots.
- Validate stock/product/user/promotion inputs and calculate totals.
- Control confirmation, shipping, completion, cancellation and refund-derived status.
- Expose customer/admin queries and publish lifecycle events.

### Out of Scope

Payment processing, shipment tracking and physical inventory ownership.

## 2. Package Structure

```text
order/
|- api/                 controller and advice
|- application/         service, facade, DTOs and event listeners
|- domain/              Order aggregate, repository, status, exceptions
|- mapper/              OrderMapper
`- README.md
```

## 3. Entities & Aggregates

### Order — Aggregate Root

File: `order/domain/Order.java`; table: `orders`; extends `BaseRootEntity`. Main data: `id`, `userId`, `status`, `totalAmount`, `shippingAddress`, `promoCode`, `discountAmount`, `items`, `histories`.

### OrderItem — Entity

Table `order_items`; extends `BaseSoftDeleteEntity`. Stores `productId`, `variantId`, quantity and snapshots of `productName`, sale `price`, `costPriceSnapshot` and serialized variant attributes.

### OrderHistory — Entity

Table `order_histories`; extends `BaseSoftDeleteEntity`. Stores status, note and owning order.

## 4. Relationships

Order owns items and histories using `@OneToMany(cascade = ALL, orphanRemoval = true)`; both children map back with LAZY `@ManyToOne`. Product, variant and user links are scalar IDs, not cross-domain JPA associations.

## 5. Domain Dependencies & Communication

- Order -> Product: `ProductFacade` resolves variants and deducts/restores stock.
- Order -> Promotion: validates/calculates/consumes/releases a promotion.
- Order -> User: `UserFacade` resolves the authenticated user.
- Cart -> Order: checkout request event invokes order creation.
- Payment/Logistics -> Order: payment and shipment events synchronize lifecycle.
- Order publishes created, cancelled, failed and status-change events used by other modules.

## 6. Main Flows / Use Cases

### Create Order

```text
OrderRequest or cart checkout -> validate user/items
-> batch resolve product variants -> validate/reserve stock
-> calculate promotion and snapshot item data
-> create Order + OrderItems + history -> save
-> consume promotion -> publish OrderCreatedEvent -> OrderResponse
```

Completion commits reserved stock. Failure/cancellation releases the reservation and promotion usage through application coordination.

## 7. Business Rules

### 7.1 Validation Rules

Shipping address is required (maximum 255 characters), promo code is at most 50 characters, items are non-empty, IDs are required and quantity is at least 1. Filter monetary bounds must be non-negative.

### 7.2 Invariants

Items must resolve to purchasable variants with sufficient stock. Promotion reuse is checked by user/code/status. Only completed/delivered/shipped order items qualify for reviews. Shipping data can change only in states allowed by `OrderStatus`.

### 7.3 State Transitions

```text
PENDING -> CONFIRMED -> SHIPPED -> COMPLETED
    |          |           |
    +----------+-----------+-> CANCELLED (where enum rules allow)
COMPLETED -> PARTIALLY_REFUNDED -> FULLY_REFUNDED
```

Exact valid next states and capabilities are encoded in `OrderStatus`; invalid changes raise `InvalidOrderStatusException`.

## 8. Persistence & Data Strategy

The aggregate uses soft delete and optimistic versioning. Repository queries provide specifications, paging, fetch queries, eligibility checks, timeout selection, reporting aggregates and version-checked soft-delete updates. Response enrichment obtains payment/logistics snapshots through their facades. Item snapshots preserve order history when catalog data changes.

## 9. Transaction Strategy

Create, lifecycle changes, deletion, address updates and refund-status updates are transactional; lookup/filter/snapshot methods use read-only transactions. Product and promotion calls execute in the same application/database transaction where invoked synchronously; this is not a distributed transaction.

## 10. API Design

DTO-separated, paginated REST/action endpoints with Bean Validation and domain advice. No version prefix, response wrapper or idempotency mechanism is exposed for direct order creation.

Order exposes three mapping depths instead of one response shape being populated identically everywhere:

- Customer history maps order fields and item snapshots, but leaves `histories` empty. Review and return screens therefore receive stable `orderItemId`, product, variant, quantity and purchase-price context without fetching every status history.
- Administrative paged filters use a lightweight summary without items or histories.
- Order detail maps both items and histories.

## 11. APIs

`OrderController`, base `/orders`:

- `GET /my-orders` — customer-scoped paged history with item snapshots and without status histories.
- `GET /{id}` — full customer/staff detail with items and status histories.
- `POST /` — create from `OrderRequest`.
- `PATCH /{id}/confirm`, `/ship`, `/complete` — staff lifecycle actions.
- `PATCH /{id}/cancel` — customer or staff cancellation.
- `PATCH /{id}/shipping-address` — permitted-state address update.
- `GET /admin/filter` — paged administrative filter.
- `DELETE /{id}` — admin deletion.

## 12. Error Handling

`OrderNotFoundException`, `InsufficientStockException`, `InvalidOrderDataException`, `InvalidOrderOperationException`, `InvalidOrderStatusException`, `DuplicateActiveOrderException`, `OrderAccessDeniedException`, `OrderDeletedConflictException` and `OrderCrossModuleException` are mapped by `OrderExceptionHandler`/global advice.

## 13. Security & Authorization

Authenticated CUSTOMER/STAFF/MANAGER/ADMIN may read/create. Staff roles control most status changes; cancel is also available to customers; delete is ADMIN-only. Controller/service ownership checks restrict customer detail and mutation access.

## 14. Algorithms & Performance Considerations

Creation batches product lookups and uses map-based variant resolution to avoid one query per item. Customer history loads `items` through an entity graph and maps them inside the read-only transaction, while deliberately skipping `histories`; this supports review/return selection without an N+1 history query. Repository-level aggregate queries power statistics. Timeout cancellation is scheduled by `OrderTimeoutScheduler`.

## 15. Architecture & Design Principles

Order is a clear aggregate boundary with snapshots and ID-based external references. Facades/events provide most module communication, although the application service directly imports external facade and DTO types.

## 16. Notes / Design Decisions

Snapshots deliberately prevent later product name, variant, price or cost changes from rewriting order history. `OrderMapper.toCustomerHistoryResponse` is the shared contract for purchase history, review selection and return selection; `toSummaryResponse` remains reserved for tables that do not need line items.

## 17. Known Limitations / Technical Debt

- Cross-module synchronous calls share one monolithic transaction and would require redesign before service extraction.

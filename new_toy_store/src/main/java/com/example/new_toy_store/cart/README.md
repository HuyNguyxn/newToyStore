# Module: Cart

## 1. Purpose

Cart owns a user's active shopping basket, selected items and the transition into checkout. It does not own product data, promotions, inventory or orders.

### Responsibilities

- Add, synchronize, update, select and remove cart items.
- Enforce cart size/quantity and checkout-state rules.
- Refresh price changes and remove invalid/expired entries through events and maintenance.
- Publish checkout requests that the Order module consumes.

### Out of Scope

Order creation, stock ownership and discount calculation remain in Order, Product and Promotion.

## 2. Package Structure

```text
cart/
|- api/                  controller and exception advice
|- application/          DTOs, facade, services and event listeners
|- domain/               Cart, CartItem, status, repositories, exceptions
|- mapper/               CartMapper
`- README.md
```

## 3. Entities & Aggregates

### Cart — Aggregate Root

File: `cart/domain/Cart.java`; table: `carts`; extends `BaseRootEntity` (`createdAt`, `updatedAt`, `deletedAt`, optimistic `version`). Main data: `id`, `userId`, `status`, `items`. Statuses: `ACTIVE`, `CHECKING_OUT`.

### CartItem — Entity

File: `cart/domain/CartItem.java`; table: `cart_items`; extends `BaseTimeEntity`. Main data: `id`, `cart`, `productId`, `variantId`, `quantity`, `isSelected`, `addedPrice`.

## 4. Relationships

`Cart 1-N CartItem` is a tight JPA aggregate relation: `@OneToMany(cascade = ALL, orphanRemoval = true)` and `CartItem @ManyToOne(fetch = LAZY)`. Product and variant are loose ID references; no Product entity is mapped into the cart aggregate.

## 5. Domain Dependencies & Communication

- Cart -> Product: `ProductFacade` validates/enriches product and variant IDs and prices.
- Cart -> Promotion: checkout data can carry a promotion code.
- Cart -> Order: `CartCheckoutRequestedEvent` starts order creation; success/failure status events complete or roll back checkout state.
- Product events update affected cart item prices or remove invalid variants.

## 6. Main Flows / Use Cases

### Checkout Cart

```text
CheckoutCartRequest -> validate cart ownership/state/selected items
-> publish CartCheckoutRequestedEvent -> Order creates order
-> OrderCreatedEvent or OrderCreationFailedEvent -> update cart state
-> OrderResponse
```

### Synchronize Client Cart

The service validates at most 50 input items, resolves current product/variant data, merges quantities, and persists one server-side cart.

## 7. Business Rules

### 7.1 Validation Rules

Product/variant IDs must be positive, quantity must be at least 1, checkout address is required (maximum 500 characters), promotion code is at most 50 characters, and sync contains 1–50 items.

### 7.2 Invariants

The aggregate limits cart size and item quantities using constants in `Cart`; checkout requires selected items and a valid cart state. Cross-module lookup failures are translated to cart-specific exceptions.

### 7.3 State Transitions

```text
ACTIVE -> CHECKING_OUT -> ACTIVE
```

The second transition occurs after checkout completion/failure handling; arbitrary state assignment is not exposed.

## 8. Persistence & Data Strategy

Cart is soft-deletable; CartItem has audit timestamps but no `deletedAt`, and maintenance queries hard-delete expired items. Repository queries include specification filtering, bulk price updates/removals and `findForUpdate...` methods. List responses use product enrichment. No explicit `@Lock` annotation is present on the cart locking queries.

## 9. Transaction Strategy

Mutating `CartService`, checkout/event handling and maintenance methods use `@Transactional`; read paths use read-only transactions where declared. A checkout spans local database work and synchronous Spring event listeners, not a distributed transaction.

## 10. API Design

JSON DTOs and Bean Validation are used. The API is resource/action oriented and returns `CartResponse` directly; no response wrapper or API version is used.

## 11. APIs

`CartController` (`cart/api/CartController.java`), base path `/carts`:

- `GET /{userId}` — get a user's cart.
- `POST /{userId}/items` — add an item (`AddCartItemRequest`).
- `POST /{userId}/sync` — synchronize client items (`SyncCartRequest`).
- `PUT /{userId}/items/{itemId}` — change quantity.
- `PATCH /{userId}/items/{itemId}/toggle` — toggle selection.
- `DELETE /{userId}/items/{itemId}` — remove an item.
- `DELETE /{userId}` — clear the cart.
- `POST /{userId}/checkout` — initiate checkout and return `OrderResponse`.

## 12. Error Handling

`CartNotFoundException`, `CartItemNotFoundException`, `CartAccessDeniedException`, `InvalidCartDataException`, `InvalidCartOperationException`, `CartDataConflictException` and `CartCrossModuleException` are handled by `CartExceptionHandler`; shared failures can fall through to `GlobalExceptionHandler`.

## 13. Security & Authorization

All unmatched endpoints require authentication. Intended cart authorization in `SecurityConfig` targets `/cart/**`, while this controller exposes `/carts/**`; the intended CUSTOMER/ADMIN restriction therefore does not match. Service methods also receive a path `userId`, so ownership enforcement is important.

## 14. Algorithms & Performance Considerations

Sync uses bounded collections and product lookup/enrichment. Bulk repository updates avoid loading every cart for variant price changes. No special algorithm beyond aggregation and standard JPA operations was found.

## 15. Architecture & Design Principles

Cart is an aggregate with owned items; product references remain IDs. A facade exposes application operations and events decouple checkout/status reactions, providing pragmatic DDD Lite boundaries.

## 16. Notes / Design Decisions

`addedPrice` snapshots the price when an item is added, while product events can refresh it. Checkout is event-driven but currently runs inside the same application process.

## 17. Known Limitations / Technical Debt

- Security matcher path differs from the controller path.
- `findForUpdate...` method names imply locking, but their declarations do not include `@Lock`.

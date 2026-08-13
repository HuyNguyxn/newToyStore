# Order Pages

## Responsibility

This area lets authenticated customers inspect their order history, review a single order and perform permitted follow-up actions.

## Screens and routes

| File | Route | Purpose |
|---|---|---|
| `OrderListPage.jsx` | `/orders` | List and filter the current customer's orders |
| `OrderDetailPage.jsx` | `/orders/:id` | Show items, totals, status and available actions |

## Dependencies

`orderService.js` supplies order queries and cancellation. `customerPaymentService.js` initializes payment or retry flows from an eligible order.

## Business behavior

Available UI actions depend on the current server status. Cancellation and payment controls should be hidden or disabled when the backend no longer permits them, but the backend remains responsible for enforcing transitions.

Order line data is historical snapshot data. The UI should display names, prices and variant attributes returned with the order rather than replacing them with the current product catalog.

`GET /orders/my-orders` returns a paged list whose `items` collection is intentionally populated for customers. The list uses these items to render product names, images when available and total quantity. Status histories remain detail-only and are obtained from `GET /orders/{id}`.

## Maintenance notes

- Refresh details after every lifecycle mutation.
- Handle stale status conflicts as normal concurrency outcomes.
- Link shipment, return, review and payment actions only when the order response indicates eligibility.
- Do not replace a missing `items` collection with sample products; display a clear empty/error state because review and return flows depend on the same contract.

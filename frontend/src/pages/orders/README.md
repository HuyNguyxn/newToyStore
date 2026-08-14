
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

## Maintenance notes

- Refresh details after every lifecycle mutation.
- Handle stale status conflicts as normal concurrency outcomes.
- Link shipment, return, review and payment actions only when the order response indicates eligibility.
- Add automated coverage for cancellation and payment retry states.

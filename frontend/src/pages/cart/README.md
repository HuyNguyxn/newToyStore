
# Cart and Checkout Pages

## Responsibility

This area lets authenticated customers manage selected product variants and turn cart selections into an order and payment attempt.

## Screens and routes

| File | Route | Purpose |
|---|---|---|
| `CartPage.jsx` | `/cart` | Load the cart, change quantities and item selection, and remove items |
| `CheckoutPage.jsx` | `/checkout` | Submit the selected cart state and initialize customer payment |

## Dependencies

- `cartService.js` for cart reads, item mutations and checkout.
- `orderService.js` to reconcile the order created by checkout.
- `customerPaymentService.js` for COD or VNPay initialization and idempotency keys.
- Authentication context through the protected route wrapper.

## Main flow

```text
Load cart -> select valid items -> review totals -> submit checkout
-> locate created order -> initialize payment -> show result or redirect to VNPay
```

Cart totals shown by the UI are advisory. Product availability, price, promotion eligibility, stock and payable totals must be recalculated by the backend during checkout.

## Maintenance notes

- Prevent duplicate checkout submissions while a request is running.
- Preserve payment idempotency behavior when retrying.
- Refresh cart data after mutations so inventory and server totals remain current.
- Treat partial checkout or payment failures as recoverable states and retain enough context for the customer to continue.

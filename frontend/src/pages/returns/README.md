
# Customer Return Pages

## Responsibility

This area lets authenticated customers request a return, review existing returns and perform actions allowed by the return lifecycle.

## Screens and routes

| File | Route | Purpose |
|---|---|---|
| `ReturnCreatePage.jsx` | `/returns/new` | Select eligible order items, provide reasons and upload evidence |
| `ReturnListPage.jsx` | `/returns`, `/returns/me` | List returns and expose permitted update, cancel or dispute actions |

## Dependencies

- `customerReturnService.js` for return lifecycle operations.
- `orderService.js` for eligible order context.
- `uploadService.js` for supporting images.

## Business behavior

Return eligibility, allowed quantities and valid transitions are server-owned rules. The UI should guide the customer using returned state but must handle rejection when data becomes stale.

## Maintenance notes

- Keep evidence upload failures separate from return submission failures.
- Refresh the return after update, cancellation or dispute.
- Display status history and rejection or inspection reasons when available.
- Do not assume a received return automatically produces a refund; inspection and payment processing are separate stages.

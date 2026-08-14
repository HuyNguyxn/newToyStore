
# Customer Payment Pages

## Responsibility

This area displays payment history and handles the customer-facing return from VNPay.

## Screens and routes

| File | Primary route | Purpose |
|---|---|---|
| `CustomerPaymentListPage.jsx` | `/payments` | List the signed-in customer's payment records |
| `CustomerVnpayReturnPage.jsx` | `/payments/vnpay-return` | Interpret the browser return and query payment status |

Legacy aliases redirect to these routes, including `/payment`, `/customer-payments`, `/vnpay-return` and `/payment/vnpay-return`.

## Data flow

Both screens use `customerPaymentService.js`. Checkout can begin from cart or order details, while the VNPay return page receives browser query parameters and confirms the result through backend APIs.

The browser return is not proof of settlement. Final payment state must come from the backend after it validates provider data or processes the provider callback.

## Maintenance notes

- Keep return aliases only while old provider or bookmarked URLs need compatibility.
- Never display provider query data as trusted payment status without backend confirmation.
- Provide clear pending, successful and failed states because callback processing may complete asynchronously.
- Preserve idempotency for repeated checkout attempts.

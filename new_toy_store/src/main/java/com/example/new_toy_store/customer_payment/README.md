# Module: Customer Payment

## 1. Purpose

Customer Payment owns customer-facing payment attempts and refunds for orders, supporting COD and VNPay. It stores order/user IDs and does not own Order or User entities.

### Responsibilities

- Create idempotent payment attempts and VNPay checkout URLs.
- Handle VNPay return/IPN data and manual status actions.
- Create, approve/process, reject and delete refund requests.
- Notify Order and Notification flows through application events.

### Out of Scope

Order totals/lifecycle, customer identity and return inspection are owned by other modules.

## 2. Package Structure

`api/` contains controller/advice; `application/` contains DTOs, `PaymentService`, facade and listeners; `domain/` contains payment/refund entities, repositories, enums and exceptions; `mapper/` maps responses. VNPay infrastructure is under the system-level `infrastructure/payment/vnpay/` package.

## 3. Entities & Aggregates

- `CustomerPaymentTransaction` — aggregate root, table `payment_transactions`, `BaseRootEntity`; `orderId`, `userId`, method, status, amount, provider ID, failure/cancel data, timestamps, optional `idempotencyKey`.
- `CustomerPaymentRefund` — aggregate root, table `payment_refunds`, `BaseRootEntity`; payment/order/user IDs, refund code, method/status, amount, reason and provider processing data.

## 4. Relationships

There are no JPA relations between payment/refund and Order/User. References are IDs. Refund-to-payment is also `paymentId`, so their lifecycles are coordinated by service/repository queries rather than cascade mapping.

## 5. Domain Dependencies & Communication

Payment -> Order uses `OrderFacade` snapshots/ownership; Payment -> User resolves the authenticated user. VNPay is accessed through `VnpayService`. Shipment/return listeners initiate payment status/refund behavior; payment events inform Order and Notification.

## 6. Main Flows / Use Cases

### Checkout

`CustomerPaymentCheckoutRequest -> validate order ownership/amount -> reuse idempotency key or reject duplicate active payment -> create COD/VNPay transaction -> save -> optional VNPay URL -> response`.

### Refund

`Refund request -> validate succeeded payment and refundable amount -> create refund -> lock/process or reject -> call VNPay/manual path -> update payment/refund -> publish refund event`. Hủy một đơn đã thu tiền cũng tự tạo yêu cầu hoàn phần tiền còn lại; hệ thống không âm thầm coi khoản thu đó là doanh thu.

## 7. Business Rules

Bean Validation requires order/method, positive refund amount, bounded reasons/provider IDs and an idempotency key of at most 80 characters. Payment states: `PENDING`, `SUCCEEDED`, `FAILED`, `CANCELLED`, `EXPIRED`, `REFUND_PENDING`, `PARTIALLY_REFUNDED`, `REFUNDED`, `REFUND_FAILED`. Refund states: `PENDING`, `PROCESSING`, `SUCCEEDED`, `FAILED`, `REJECTED`, `CANCELLED`. Enum transition maps reject invalid changes; refund totals cannot exceed eligible payment value.

## 8. Persistence & Data Strategy

Both roots use soft delete and optimistic versioning. Payment and refund repositories use `PESSIMISTIC_WRITE` for update-sensitive lookups, specifications/paging, idempotency/duplicate queries and aggregate reporting. No JPA cascade exists between the two tables.

## 9. Transaction Strategy

Checkout, callbacks, manual status changes and refund decisions are transactional; reads/filtering are read-only where declared. External VNPay calls occur from application/infrastructure code; no distributed transaction or outbox is present.

## 10. API Design

Direct DTO responses, paging and Bean Validation are used. VNPay callbacks are GET query-parameter endpoints. Checkout idempotency is body-based, not an HTTP idempotency header.

## 11. APIs

`CustomerPaymentController`, base `/payments`: `POST /checkout`; public `GET /vnpay-return` and `/vnpay-ipn`; `GET /my-payments`, `/{id}`, `/admin/filter`; `POST /{id}/refunds`; `GET /{id}/refunds`, `/refunds`; `PATCH /refunds/{refundId}/process|reject`; `DELETE /refunds/{refundId}`; `PATCH /{id}/succeed|fail|cancel`; `DELETE /{id}`.

## 12. Error Handling

Notable exceptions cover missing/deleted payments or refunds, duplicates, invalid data/status/operations, access denial and cross-module errors. `CustomerPaymentExceptionHandler` maps these; global advice handles shared validation/technical failures.

## 13. Security & Authorization

Callbacks are public. Customer and staff roles can read/create/cancel according to global rules and ownership checks. Refund processing requires MANAGER/ADMIN; refund/payment deletion is ADMIN-only; manual success/failure permits STAFF/MANAGER/ADMIN.

## 14. Algorithms & Performance Considerations

Idempotency lookup avoids duplicate retries. Pessimistic locking serializes state-changing payment/refund decisions. Aggregate queries support dashboards.

## 15. Architecture & Design Principles

ID references isolate persistence, while a facade and events form the module API. VNPay details are kept in infrastructure rather than the entity model.

## 16. Notes / Design Decisions

COD and VNPay share one payment model; refund method distinguishes manual COD handling from VNPay provider refund.

Khi đơn hàng bị hủy, mọi giao dịch `PENDING` của đơn được chuyển sang `CANCELLED`; giao dịch đã thành công được chuyển vào luồng hoàn tiền. Tác vụ reconciliation chạy khi ứng dụng khởi động và theo lịch để sửa các giao dịch chờ cũ còn gắn với đơn đã hủy.

## 17. Known Limitations / Technical Debt

No durable outbox was found; external calls and local state therefore need careful failure/retry handling.

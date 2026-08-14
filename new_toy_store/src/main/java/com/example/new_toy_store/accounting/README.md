# Module: Accounting

## 1. Purpose

Accounting provides NewToyStore with an internal double-entry ledger and cash-flow view. It owns the chart of accounts, journal entries and journal lines, and derives account balances, the general ledger, trial balance, income statement and management dashboard.

### Responsibilities

- Record balanced debit/credit journal entries.
- Automatically post customer payments, refunds, completed imports and supplier payments from application events.
- Maintain internal account balances and expose available liquidity to `supplier_payment`.
- Allow administrators to create manual entries and reverse posted entries.
- Provide management reports and the internal-fund dashboard.
- Reconcile historical business transactions that do not yet have journal entries while preserving idempotency.

### Out of Scope

- Real bank-account connections.
- Bank-statement synchronization or reconciliation.
- Tax processing, accounting-period closing or statutory financial statements.

## 2. Package Structure

```text
accounting/
|- api/
|  |- AccountingController.java
|  `- advice/AccountingExceptionHandler.java
|- application/
|  |- AccountingFacade.java
|  |- AccountingService.java
|  |- InternalFundQuery.java
|  |- dto/
|  |- listener/AccountingEventListener.java
|  `- reconciliation/
|- domain/
|  |- LedgerAccount.java
|  |- JournalEntry.java
|  |- JournalEntryLine.java
|  |- repository interfaces
|  |- enums
|  `- exception/
`- README.md
```

- `api` contains the REST controller and module exception handler.
- `application` coordinates use cases, response mapping, reporting, event listeners and reconciliation.
- `domain` contains entities, balancing rules, enums and repository abstractions.

The current module does not have dedicated `mapper` or `infrastructure` packages. Entity-to-response conversion is currently implemented in `AccountingService`.

## 3. Entities and Aggregates

### LedgerAccount — Aggregate Root

`LedgerAccount` represents an account in the chart of accounts. It stores the code, name, account type, normal balance side, liquidity flag, system flag and active state. Its domain behavior derives the balance from accumulated debit and credit amounts.

### JournalEntry — Aggregate Root

`JournalEntry` represents a journal header containing the entry number, posting date, description, business source, source reference, state, posting user, original reversed entry and journal lines.

### JournalEntryLine — Entity

`JournalEntryLine` represents one debit or credit movement against a `LedgerAccount`. Each line contains the account, description, debit amount and credit amount.

## 4. Relationships

- `JournalEntry` owns a one-to-many relationship with `JournalEntryLine`; lines are added through the aggregate root.
- Each `JournalEntryLine` references one independently managed `LedgerAccount`.
- A reversal entry references its original entry through `reversed_entry_id`, preserving history instead of overwriting or deleting a posted record.

## 5. Domain Communication

### Customer Payment

`AccountingEventListener` handles `PaymentCompletedEvent`, `PaymentRefundedEvent` and `OrderStatusChangedEvent`. Customer receipts are initially posted to account `3388` as customer advances. Revenue (`511`) and cost of goods sold (`632/156`) are recognized only when the order becomes `COMPLETED`. A cancelled-order refund reduces `3388`; an after-sale refund reduces revenue through `521`.

### Imports

The listener handles `ImportNoteCompletedEvent`, calculates `quantity * importPrice`, increases inventory and recognizes supplier accounts payable.

### Supplier Payment

The listener handles `SupplierPaymentRecordedEvent` to reduce payable and cash. In the opposite query direction, `supplier_payment` calls `InternalFundQuery` to verify available liquidity before recording a payment. The accounting dashboard also calls `SupplierPaymentFacade` for open and overdue supplier debt.

## 6. Main Flows

### Automatic Posting

```text
Source transaction commits
        -> AFTER_COMMIT event
        -> verify sourceType + sourceReference
        -> build debit and credit lines
        -> validate balance
        -> persist JournalEntry
```

| Business event | Debit | Credit |
|---|---|---|
| COD receipt before order completion | `111` | `3388` |
| Electronic receipt before completion | `112` | `3388` |
| Complete order and recognize revenue | `3388` | `511` |
| Complete order and recognize cost | `632` | `156` |
| Refund cancelled order | `3388` | `111` or `112` |
| Refund completed sale | `521` | `111` or `112` |
| Complete import note | `156` | `331` |
| Pay supplier | `331` | `111` or `112` |
| Ship supplier return | `331`/`642` | `156` |
| Restore supplier-return stock | `156` | `331`/`642` |

### Manual Posting and Reversal

Administrators submit a posting date, description, source, reference and debit/credit lines. The service only accepts permitted manual sources, rejects duplicate references and requires balanced totals. Reversal loads the original entry, swaps debit and credit values, links the new reversal to the original entry and marks the original as `REVERSED`.

### Historical Reconciliation

```text
Read source transactions in batches
        -> exclude existing source keys
        -> preview missing entries
        -> administrator confirms
        -> post each entry in an independent transaction
        -> database unique constraint prevents duplicates
```

Opening the dashboard never writes reconciliation data. `AccountingReconciliationSourceReader` uses aggregate and `NOT EXISTS` queries to avoid per-entity loading and N+1 queries.

## 7. Business Rules

- Manual-entry descriptions, references, account codes and line collections are required.
- Debit and credit values cannot be negative; each line must use exactly one side.
- Total debit must equal total credit within a tolerance below `0.01`.
- Accounts must exist and be active.
- `sourceType + sourceReference` must be unique.
- Automatic sources cannot be impersonated through the manual-entry API.
- A reversed entry cannot be reversed again.
- Report `from` dates cannot be after `to` dates.

The only posting-state transition is `POSTED -> REVERSED`. Reversal creates a new entry and never changes historical amounts.

## 8. Persistence and Transactions

- Flyway V8 creates `ledger_accounts`, `journal_entries` and `journal_entry_lines`, and seeds nine system accounts.
- `uk_journal_source` enforces idempotency; `chk_journal_line_one_side` enforces one-sided lines.
- Indexes cover account codes, posting dates, sources, statuses and journal-line foreign keys.
- Balance reports use aggregate projections; general-ledger queries use pagination.
- Entities inherit the timestamp/version behavior of the shared base entities.
- Monetary values use `DOUBLE` for project-wide compatibility and are rounded to two decimal places with `Math.round`.
- Reporting uses read-only transactions. Automatic postings use `REQUIRES_NEW` after the source transaction commits through `@TransactionalEventListener(AFTER_COMMIT)`.

The current design does not use distributed transactions or a transactional outbox for accounting events.

## 9. API

Base path: `/api/accounting`

| Method | Endpoint | Purpose | Authorization |
|---|---|---|---|
| GET | `/dashboard` | Fund, inventory, payable and profit overview | MANAGER, ADMIN |
| GET | `/accounts` | Chart-of-account balances | MANAGER, ADMIN |
| GET | `/journal-entries` | Paginated general journal | MANAGER, ADMIN |
| GET | `/journal-entries/{id}` | Journal-entry details | MANAGER, ADMIN |
| GET | `/general-ledger/{accountCode}` | Account ledger for a date range | MANAGER, ADMIN |
| GET | `/reports/trial-balance` | Trial balance at a date | MANAGER, ADMIN |
| GET | `/reports/income-statement` | Income statement for a period | MANAGER, ADMIN |
| GET | `/reconciliation/preview` | Preview missing historical postings | MANAGER, ADMIN |
| POST | `/journal-entries` | Create a manual entry | ADMIN |
| POST | `/journal-entries/{id}/reverse` | Reverse a posted entry | ADMIN |
| POST | `/reconciliation/execute` | Post missing historical entries | ADMIN |

The API uses request/response DTOs, Bean Validation, pagination and ISO date parameters. It intentionally provides no update or delete endpoint for posted entries.

## 10. Error Handling and Security

- `LedgerAccountNotFoundException` covers missing or inactive accounts.
- `JournalEntryNotFoundException` covers missing journal entries.
- `InvalidJournalEntryException` covers imbalance, invalid sources, duplicate references, invalid date ranges and invalid reversal operations.
- `AccountingExceptionHandler` maps accounting-domain failures to the shared HTTP error response.

Controller queries require `MANAGER` or `ADMIN`. Manual posting, reversal and reconciliation execution require `ADMIN`.

## 11. Performance and Design Notes

- Aggregate calculations are delegated to the database instead of loading every journal line.
- Account totals are assembled through an account-ID map for constant-time lookup.
- Idempotency combines application checks with a database unique constraint.
- Historical reconciliation runs in batches instead of issuing one repository call per business record.
- The journal list currently maps entries inside the service and may trigger additional queries when lazily reading lines.

Accounting follows DDD Lite with aggregates, repository abstractions, an application facade and domain exceptions. Cross-module writes arrive through events; available-fund reads use `InternalFundQuery`. It is an internal management subledger in the modular monolith, not a legal accounting system or independent microservice.

## 12. Known Limitations

- Mapping remains inside `AccountingService`; there is no dedicated mapper package yet.
- Journal filtering currently uses pagination and report-date parameters rather than a dynamic specification.
- There is no accounting-period lock; historical backfill is explicitly triggered through the reconciliation API.
- Internal events do not use a transactional outbox.
- Financial production systems should replace `DOUBLE` with `DECIMAL`/`BigDecimal`.
- Real bank integration and bank reconciliation remain intentionally outside project scope.

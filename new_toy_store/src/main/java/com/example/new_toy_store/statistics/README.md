
# Module: Statistics

## 1. Purpose

Statistics is a read-oriented administrative reporting module. It owns no persisted business entity; it combines repository/facade aggregates from commerce modules into KPI responses.

## 2. Package Structure

`api/` controller/advice; `application/` `StatisticsService`, facade and request/response DTOs; `domain/` period/date/grouping enums and request exceptions.

## 3. Entities & Aggregates

N/A. No `@Entity` or repository is defined in this module. `StatisticPeriod`, `StatisticGroupBy` and `StatisticDateField` are value-like enums for query interpretation.

## 4. Relationships

N/A at JPA level. All data is read through other module facades/repositories.

## 5. Domain Dependencies & Communication

Statistics reads Customer Payment, Imports, Logistics, Order, Product, Promotion and User aggregates through their exposed services/facades. It does not write those domains.

## 6. Main Flows / Use Cases

`StatisticsOverviewRequest -> validate timezone/period/grouping -> resolve date range and comparison range -> execute aggregate queries -> calculate KPI deltas/breakdowns -> response`. Separate endpoints return top/slow products, revenue trends and inventory/profit views.

## 7. Business Rules

Timezone must be non-blank/valid; limits are bounded (overview top limit 1–20, product limit 1–50); low-stock threshold is non-negative. Grouping supports AUTO/DAY/WEEK/MONTH/QUARTER/YEAR, and AUTO chooses a granularity from date-range length. Period/date-field parsing rejects unsupported values.

## 8. Persistence & Data Strategy

No owned persistence. Repository-level count/sum/group queries avoid loading full transaction tables. Responses are projections assembled from `Object[]` aggregates and DTOs.

## 9. Transaction Strategy

Statistics service/facade query methods use read-only transactions where declared; no mutations occur.

## 10. API Design

ADMIN-only GET reporting endpoints use query-bound validated DTOs and return DTO lists/objects directly.

## 11. APIs

`StatisticsController`, base `/statistics`: `/overview`, `/products/top-selling`, `/products/slow-selling`, `/revenue/trend`, `/revenue/by-payment-method`, `/revenue/by-category`, `/customers/top-spending`, `/inventory/snapshot`, `/inventory/movements`, `/profit-margin`.

## 12. Error Handling

Invalid period, grouping and request exceptions are mapped by `StatisticsExceptionHandler`.

## 13. Security & Authorization

The controller has class-level `@PreAuthorize("hasRole('ADMIN')")`; SecurityConfig also restricts `/statistics/**` to ADMIN.

## 14. Algorithms & Performance Considerations

Date bucketing and comparison-period calculation are the main algorithms. Heavy aggregation is pushed to SQL/JPA queries. Top lists are bounded. Complexity at Java level is proportional to returned buckets/products, not raw transaction count.

## 15. Architecture & Design Principles

This is an application/read-model composition module, not an aggregate-owning domain. Its broad dependencies are intentional for reporting but would be a service-extraction boundary concern.

## 16. Notes / Design Decisions

Reporting uses current operational tables rather than a separate warehouse/read database.

## 17. Known Limitations / Technical Debt

Strong read-time coupling to many modules and untyped `Object[]` repository projections make refactoring and query changes fragile.

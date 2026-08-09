# Module: Logistics

## 1. Purpose

Logistics owns shipment records and tracking for forward orders, customer returns and supplier returns.

## 2. Package Structure

`api/` controller/advice; `application/` service, facade, DTOs and order/return listeners; `domain/` shipment entities, repositories, enums/converters/exceptions; `mapper/` mappings.

## 3. Entities & Aggregates

- `Shipment` — aggregate root, table `shipments`, `BaseRootEntity`; tracking/order/user/provider/address/fees/status/type and optional customer/supplier-return IDs; owns items.
- `ShipmentItem` — table `shipment_items`, `BaseSoftDeleteEntity`; product/variant IDs and product/variant snapshots plus quantity.
- `ShipmentTrackingLog` — table `shipment_tracking_logs`, `BaseTimeEntity`; shipment ID, status, location, description and occurrence time.

## 4. Relationships

Shipment 1-N ShipmentItem uses LAZY child ownership, `cascade = ALL`, `orphanRemoval = true`. Tracking logs reference `shipmentId` rather than a JPA entity. Order/return/user/supplier references are IDs.

## 5. Domain Dependencies & Communication

Logistics uses Order, Customer Return, Supplier Return, Supplier, User and Customer Payment facades/snapshots to create/enrich shipments. It listens to order/return events and publishes shipment lifecycle events consumed by Order, returns and notifications.

## 6. Main Flows / Use Cases

`Confirmed order/approved return -> obtain source snapshot -> reject duplicate shipment -> create typed shipment/items -> append tracking log -> execute allowed actions -> publish status events -> synchronize source lifecycle`.

## 7. Business Rules

Shipment actions are typed (`HAND_OVER_TO_CARRIER`, `MARK_DELIVERED`, delivery failure/retry/return/cancel). `ShipmentStatus` controls allowed actions/transitions across pickup, transit, failure, delivered, returned and cancelled. `ShipmentType` requires the appropriate source ID. Action reasons/locations have length limits.

## 8. Persistence & Data Strategy

Shipment is soft-deletable/versioned; items are soft-deletable; logs are timestamped. Repository duplicate checks cover order/customer-return/supplier-return. State mutations use `PESSIMISTIC_WRITE`; filters use specifications and Page. Migration V4/V6 made `order_id` nullable and added multi-direction columns.

## 9. Transaction Strategy

Create, action, delete and synchronization operations are transactional. Detail/filter methods are not consistently marked read-only. Events coordinate source modules without distributed transactions.

## 10. API Design

Resource/action endpoints use DTOs, paging and authenticated-user ownership. Direct responses, no version prefix/wrapper.

## 11. APIs

`ShipmentController`, base `/shipments`: `POST /orders/{orderId}`; `GET /my-shipments`, `/{id}`, `/{id}/tracking-logs`, `/admin/filter`; `POST /{id}/actions`; `DELETE /{id}`. Customer/supplier return shipments are initiated through application listeners/facades, not separate public controller routes.

## 12. Error Handling

Exceptions cover duplicate/missing/deleted shipment, invalid data/operation/status, access denial and cross-module failures; `LogisticsExceptionHandler` maps them.

## 13. Security & Authorization

Authenticated customer/staff roles can read, with ownership enforced for customer detail. Creation/actions/deletion require STAFF/MANAGER/ADMIN (method security explicitly covers create/action/delete).

## 14. Algorithms & Performance Considerations

Snapshot creation avoids later source-data drift. Pessimistic locks serialize shipment actions. Specification filters and repository aggregates support lists/statistics.

## 15. Architecture & Design Principles

One typed aggregate handles three shipment directions. IDs and source snapshots limit persistent coupling, while facades/events handle integration.

## 16. Testing Strategy

No tests were found. Recommended: action matrix, concurrent actions, duplicate source shipments, ownership and all three source directions.

## 17. Notes / Design Decisions

`SELF_SHIPPING` is implemented; `GHN` is described in the enum as reserved for a later phase.

## 18. Known Limitations / Technical Debt

No external carrier gateway implementation or automated lifecycle tests were found.

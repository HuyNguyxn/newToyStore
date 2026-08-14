
# Shipment Pages

## Responsibility

`ShipmentListPage.jsx` provides authenticated customers with shipment summaries, details and tracking history at `/shipments`.

## Dependencies

`shipmentService.js` loads the current user's shipments, a selected shipment's details and its tracking logs.

## Behavior

The page presents backend-owned shipment status and chronological tracking events. Shipments may represent fulfillment or return logistics, so labels should use the shipment direction and related business reference supplied by the API.

## Maintenance notes

- Keep tracking events ordered by their recorded occurrence time.
- Handle shipments that exist before the first tracking event.
- Do not infer delivery or refund completion solely from a carrier-style label.
- Consider pagination or incremental refresh when shipment histories become large.

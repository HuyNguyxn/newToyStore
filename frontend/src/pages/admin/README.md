
# Administration Pages

## Responsibility

This area provides the operational interface for staff, managers and administrators. All routes are nested below `/admin`, use `AdminLayout` and are protected by role-aware routing.

## Screens

| Screen | Main purpose |
|---|---|
| `AdminDashboardPage.jsx` | Administrative landing page |
| `AdminProductPage.jsx` | Product, variant and media management |
| `AdminCategoryPage.jsx` | Category tree management |
| `AdminPromotionPage.jsx` | Promotion configuration and product selection |
| `AdminOrderPage.jsx` | Order search and lifecycle operations |
| `AdminCustomerPaymentPage.jsx` | Customer payment monitoring |
| `AdminCustomerRefundPage.jsx` | Refund processing |
| `AdminSupplierPage.jsx` | Supplier records and supplied products |
| `AdminImportPage.jsx` | Inbound import workflow |
| `AdminSupplierPaymentPage.jsx` | Supplier invoices and payments |
| `AdminSupplierReturnPage.jsx` | Supplier return workflow |
| `AdminLogisticsPage.jsx` | Shipment operations and tracking |
| `AdminReturnInspectionPage.jsx` | Customer-return inspection |
| `AdminReviewModerationPage.jsx` | Review status and replies |
| `AdminModerationPage.jsx` | Moderation vocabulary |
| `AdminNotificationPage.jsx` | Administrative notifications |
| `AdminInventoryPage.jsx` | Warehouse inventory operations |
| `AdminStatisticsPage.jsx` | Operational and sales reporting |
| `AdminUploadPage.jsx` | Manual media upload |
| `AdminUserPage.jsx` | Account administration |

`AdminResourcePage.jsx` is a reusable resource-oriented page helper rather than a routed business screen.

## Access rules

The `/admin` area accepts `STAFF`, `MANAGER` and `ADMIN`. User management is limited to `ADMIN`. Promotions, notifications and statistics are limited to `MANAGER` and `ADMIN`. Staff users are redirected to products instead of statistics at the admin index.

These route checks support navigation only; backend authorization remains authoritative.

## Data flow

Pages call dedicated `admin*Service.js` modules, with shared product, upload, warehouse and statistics services used where appropriate. Each screen generally owns its loading, filters, mutation and error state locally.

## Maintenance notes

- Keep route declarations synchronized with `AdminSidebar.jsx`.
- Confirm frontend role visibility whenever backend authorization changes.
- Preserve explicit confirmation and refresh behavior around destructive or lifecycle-changing operations.
- The administration area is broad; reusable tables, forms and error handling should move into shared components as duplication increases.


# NewToyStore Frontend

## Overview

The frontend is a React 18 single-page application built with Vite. It provides the public storefront, authenticated customer workflows and a role-based administration interface for the NewToyStore backend.

## Technology stack

- React 18 and React DOM.
- React Router 6 for client-side routing.
- Vite 6 for local development and production builds.
- Recharts for statistics and reporting visualizations.
- React Flow, Dagre and React D3 Tree for relationship and hierarchy views.
- Native `fetch` through a shared API client; no external state-management library is used.

## Architecture

```text
App routes
   |
   v
Page components ----> shared components and layouts
   |
   v
Feature services ----> apiClient ----> Spring Boot API
   ^
   |
AuthContext and local component state
```

`App.jsx` owns route composition and role gates. Page components coordinate presentation and feature calls. Files under `services` isolate HTTP endpoints. `AuthContext` owns the current session and profile, while most feature state remains local to its page.

## Source structure

```text
src/
|- components/     Shared layouts, navigation and route guards
|- contexts/       Authentication context
|- hooks/          Context access hooks
|- pages/          Customer and administrative screens
|- services/       API client and endpoint functions
|- styles/         Global reset, variables and application styles
|- utils/          Validation and formatting helpers
|- App.jsx         Route definitions
`- main.jsx        Browser bootstrap and providers
```

## Feature documentation

| Area | Documentation |
|---|---|
| Administration | [pages/admin](src/pages/admin/README.md) |
| Authentication | [pages/auth](src/pages/auth/README.md) |
| Cart and checkout | [pages/cart](src/pages/cart/README.md) |
| Customer payments | [pages/customer-payments](src/pages/customer-payments/README.md) |
| Notifications | [pages/notifications](src/pages/notifications/README.md) |
| Orders | [pages/orders](src/pages/orders/README.md) |
| Products | [pages/products](src/pages/products/README.md) |
| Profile | [pages/profile](src/pages/profile/README.md) |
| Returns | [pages/returns](src/pages/returns/README.md) |
| Reviews | [pages/reviews](src/pages/reviews/README.md) |
| Shipments | [pages/shipments](src/pages/shipments/README.md) |
| API services | [services](src/services/README.md) |
| Shared components | [components](src/components/README.md) |

Home, policy and not-found pages are small presentation areas and are documented here instead of receiving separate README files.

## Routing and access

Customer routes use `CustomerLayout`. Cart, checkout, orders, payments, profile, notifications, returns, reviews and shipments require an authenticated session. Public routes include the catalog, authentication pages and policy pages.

Routes below `/admin` require `STAFF`, `MANAGER` or `ADMIN`. User management is restricted to `ADMIN`; promotions, notifications and statistics require `MANAGER` or `ADMIN`. Other administrative routes accept all staff roles unless the backend applies a stricter rule.

## Authentication and API behavior

The token is stored in `localStorage` under `newToyStoreToken`. `apiClient.js` attaches it as a bearer token, parses JSON or text responses and clears it after an authenticated `401`. `AuthContext` restores the current user on startup and exposes login, registration, profile update and logout operations.

Client-side route protection improves navigation but is not a security boundary. The backend must continue to validate every protected operation.

## Setup and run

Requirements: Node.js and npm.

```powershell
npm install
npm run dev
```

The API base URL is configured with:

```text
VITE_API_URL=http://localhost:8080
```

When omitted, the client uses `http://localhost:8080`.

Create a production build with:

```powershell
npm run build
```

Preview the generated build with:

```powershell
npm run preview
```

## Development conventions

- Keep endpoint construction and HTTP calls in `src/services`.
- Keep route-level orchestration in page components and reusable presentation in `src/components`.
- Use `ProtectedRoute` for session and role-based navigation rules.
- Reuse formatters and validation helpers instead of duplicating formatting rules.
- Update the nearest README when routes, roles, endpoints or major workflows change.

## Known limitations

The current UI uses page-local state for most workflows, has no centralized caching layer and contains some text that appears to have been saved with inconsistent character encoding. These areas should be addressed as the client grows.

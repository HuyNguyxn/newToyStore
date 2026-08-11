# Shared Components

## Responsibility

This directory contains reusable structure and navigation shared by multiple pages. Business-specific route screens remain under `pages`.

## Groups

### `admin`

- `AdminLayout.jsx` renders the nested administration outlet and common structure.
- `AdminSidebar.jsx` exposes administrative navigation and should stay synchronized with `/admin` routes and role visibility.

### `layout`

- `CustomerLayout.jsx` wraps storefront and customer routes.
- `Header.jsx` provides primary navigation and session-aware actions.
- `Footer.jsx` provides shared footer and policy navigation.

### `common`

- `ProtectedRoute.jsx` redirects unauthenticated users and checks optional allowed roles.
- `BackLink.jsx` provides reusable backward navigation.

Home-specific reusable components remain beside the home page because they are currently coupled to that feature.

## Conventions

- Keep shared components driven by props, routing context or narrowly scoped contexts.
- Avoid direct feature API calls unless the component explicitly owns that cross-page responsibility.
- Preserve semantic HTML, keyboard navigation and visible focus states.
- Route guards improve the user experience but never replace backend authorization.
- Move a page-local component here only after it has a genuine second consumer or stable shared responsibility.

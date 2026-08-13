# Product Pages

## Responsibility

This area provides customer-facing catalog browsing and product details.

## Screens and routes

| File | Route | Purpose |
|---|---|---|
| `ProductListPage.jsx` | `/products` | Browse and filter visible products |
| `ProductListPage.jsx` | `/products/category/:categoryId` | Browse within a category |
| `ProductDetailPage.jsx` | `/products/:id` | Inspect variants, media, stock context and reviews |

The home page also consumes product data and reusable `ProductCard` and `CategoryMenu` components.

## Dependencies

- `productService.js` for catalog and product detail queries.
- `reviewService.js` for public product reviews.
- `cartService.js` when an authenticated customer adds a selected variant to the cart.

## Maintenance notes

- Use stable product and variant identifiers when building navigation or cart requests.
- Require a valid variant selection before adding to the cart.
- Display server-provided pricing and availability; do not derive authoritative totals in the browser.
- Keep filter parameters shareable through URLs where practical.
- Plan for loading, empty and unavailable-product states independently.
- Keep customer-facing detail, review, stock and variant labels in Vietnamese; translate technical fallback text before rendering it.

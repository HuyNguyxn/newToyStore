# Frontend Services

## Responsibility

This directory is the HTTP boundary between React pages and the Spring Boot API. Services construct endpoints and request bodies, then delegate transport behavior to `apiClient.js`.

## Shared client

`apiClient.js`:

- Reads the API origin from `VITE_API_URL`, defaulting to `http://localhost:8080`.
- Adds JSON content headers except for `FormData` uploads.
- Attaches the stored JWT as a bearer token.
- Returns `null` for `204` responses.
- Parses JSON or text responses and throws backend error payloads.
- Clears an existing token after a `401` response.

The client does not currently provide cancellation, retries, timeout handling, response caching or automatic token refresh.

## Service groups

- Customer services: authentication, products, categories, cart, orders, payments, returns, reviews, shipments and notifications.
- Administrative services: users, products, categories, orders, payments, refunds, suppliers, imports, supplier payments, returns, logistics, promotions, moderation and reviews.
- Shared operational services: uploads, inventory, warehouse, statistics and badge counts.

## Conventions

- Keep URLs, query parameters and request serialization in services rather than page components.
- Return parsed domain data and let pages own presentation state.
- Reuse `apiClient` so authentication and errors remain consistent.
- Use `FormData` only for media or multipart endpoints.
- Preserve backend idempotency requirements for payment and other retry-sensitive commands.
- Do not hide authorization failures or convert them into successful empty results.

## Adding or changing an endpoint

1. Add the smallest feature-specific exported function.
2. Match the backend method, path, parameter names and body exactly.
3. Update the consuming page and its nearest README.
4. Verify unauthenticated, unauthorized, validation, empty and successful responses.
5. Add tests when a frontend test harness is introduced.

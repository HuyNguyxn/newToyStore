# Review Pages

## Responsibility

This area lets authenticated customers create reviews for eligible purchases and manage their existing reviews.

## Screens and routes

| File | Route | Purpose |
|---|---|---|
| `ReviewCreatePage.jsx` | `/reviews/new` | Select an eligible order item and submit rating, content and media |
| `ReviewListPage.jsx` | `/reviews/me` | List, update or delete the customer's reviews |

Public product-review display is part of `ProductDetailPage.jsx`; administrative moderation lives under `pages/admin`.

## Dependencies

- `orderService.js` for verified-purchase context.
- `reviewService.js` for review queries and mutations.
- `uploadService.js` for image and video evidence.

## Maintenance notes

- Let the backend determine verified-purchase eligibility and moderation state.
- Separate media upload progress from review submission state.
- Refresh the review list after update or deletion.
- Make pending, visible, hidden and rejected states understandable without exposing internal moderation details.

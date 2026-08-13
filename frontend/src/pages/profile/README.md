# Profile Page

## Responsibility

`ProfilePage.jsx` lets an authenticated customer view and update account information and related profile data at `/profile`.

## Dependencies

- `authService.js` for current-user and profile operations.
- `uploadService.js` for avatar media upload.
- `AuthContext` to replace the in-memory user after a successful update.
- Shared user validation and formatting utilities.

## Data flow

The page loads server-owned profile data, validates editable fields, optionally uploads media, submits the update and synchronizes the returned user into the authentication context.

Backend role codes remain `CUSTOMER`, `STAFF`, `MANAGER` and `ADMIN`, but the page renders their Vietnamese labels. Avatar URL and upload controls also use customer-facing Vietnamese text; the stored value remains the Cloudinary URL returned by the upload API.

## Maintenance notes

- Do not update context optimistically with values the backend has not accepted.
- Keep profile validation aligned with backend rules.
- Treat uploaded media as a separate failure boundary from profile persistence.
- Avoid exposing authentication or recovery data in editable profile forms.

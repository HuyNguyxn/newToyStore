# Authentication Pages

## Responsibility

This area handles account registration, login, email verification and password recovery. Session state is owned by `AuthContext`, while endpoint calls live in `authService.js`.

## Screens and routes

| File | Route | Purpose |
|---|---|---|
| `LoginPage.jsx` | `/login` | Authenticate and return the user to the requested location |
| `RegisterPage.jsx` | `/register` | Create a customer account and support verification resend |
| `VerifyEmailPage.jsx` | `/verify-email` | Confirm the email token supplied in the URL |
| `ForgotPasswordPage.jsx` | `/forgot-password` | Request a password-reset message |
| `ResetPasswordPage.jsx` | `/reset-password` | Apply a new password using the reset token |

## Session flow

After login, the access token is stored through `apiClient.js`, and `AuthContext` stores the returned user. On application startup, an existing token triggers a current-user request. Invalid authenticated sessions are cleared after a `401`.

## Validation and security

Client validation improves feedback but does not replace backend validation. Do not log or expose access, verification or reset tokens. Query-string tokens should be consumed only by their intended endpoint, and error messages should avoid revealing whether an account exists.

## Maintenance notes

- Keep redirects compatible with `ProtectedRoute` and the `location.state.from` value.
- Keep password and profile rules aligned with backend validation.
- Add automated coverage for session restoration, failed login, verification and reset-token handling.

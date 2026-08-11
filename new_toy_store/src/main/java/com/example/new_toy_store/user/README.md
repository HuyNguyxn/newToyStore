# Module: User

## 1. Purpose

User owns accounts, credentials, email/password tokens, profile data, roles/status and delivery addresses. It supplies identity snapshots to other modules.

## 2. Package Structure

`api/` controller/advice; `application/` service, facade, configuration and DTOs; `domain/` User/Address/VerificationToken, repositories, enums/exceptions; `mapper/` mapping. JWT implementation is in system infrastructure.

## 3. Entities & Aggregates

- `User` — aggregate root, table `users`, `BaseRootEntity`; email, encoded password, name, phone, avatar, role, status and addresses.
- `Address` — entity, table `addresses`, `BaseSoftDeleteEntity`; receiver name/phone, detail and default flag.
- `VerificationToken` — entity, table `verification_tokens`; token value, expiry, token type and user. It does not inherit the shared audit base.

## 4. Relationships

User 1-N Address uses `cascade = ALL`, `orphanRemoval = true`; Address has LAZY `@ManyToOne`. VerificationToken has LAZY `@ManyToOne` to User but is stored through a separate repository.

## 5. Domain Dependencies & Communication

User imports no other business module. `UserFacade` exposes ID/email/profile/recipient lookups to Order, Payment, Logistics, Review, Notification and Statistics. User deletion publishes `UserDeletedEvent`.

## 6. Main Flows / Use Cases

### Registration and Login

`RegisterRequest -> validate unique email -> BCrypt encode -> create UNVERIFIED user/token -> send verification mail`; verification activates the account. `LoginRequest -> load user -> verify password and canLogin -> issue JWT -> AuthResponse`.

Password recovery creates a reset token; profile/address/admin lifecycle actions update the aggregate.

## 7. Business Rules

Emails are required/valid/unique; passwords are at least six characters on registration/reset/change; name required. Only one default address is maintained. Statuses `UNVERIFIED`, `ACTIVE`, `LOCKED` encode login/order/data capabilities and valid transitions. Roles `CUSTOMER`, `STAFF`, `MANAGER`, `ADMIN` encode capabilities/assignability.

## 8. Persistence & Data Strategy

User/Address use soft delete; User has optimistic versioning. Repositories support specifications, paging, case-insensitive email lookup, status/role counts and token lookup/expiry. User responses exclude passwords and token values except the purpose-specific reset-token response returned by the current API.

## 9. Transaction Strategy

Registration, verification, recovery/reset, profile/password/address and admin mutations are transactional; lookup/filter methods are read-only where declared.

## 10. API Design

Public auth routes, authenticated `/me` resources and ADMIN collection/member routes share `/users`. Request/response DTOs and Page are used; several legacy duplicate profile/password paths remain.

## 11. APIs

`UserController`, base `/users`:

- Public: `POST /register`, `/login`, `/forgot-password`, `/reset-password`; `GET /verify`.
- Current user: `GET /me`, `PUT /me`, `PATCH /me/password`; legacy `GET|PUT /me/profile`, `PUT /me/password`; address add/default/delete.
- Admin: `GET /`, `/summary`, `/{id}`; `PATCH /{id}/role|status|lock|unlock`; `DELETE /{id}`.

## 12. Error Handling

`UserNotFoundException`, `InvalidUserOperationException` and `UserDomainException` are handled by `UserExceptionHandler`; Spring Security handles authentication failures.

## 13. Security & Authorization

Auth/recovery routes are public. `/users/me/**` requires authentication. All remaining user administration routes require ADMIN. Passwords use BCrypt; JWT is stateless and validated by `JwtAuthenticationFilter`.

## 14. Algorithms & Performance Considerations

BCrypt is deliberately CPU-costly for password storage. Batch recipient lookup supports notification fan-out. No specialized data structure beyond repository queries.

## 15. Architecture & Design Principles

User owns credentials and exposes a facade rather than its repository to most modules. Security implementation remains in infrastructure.

## 16. Notes / Design Decisions

Token types define expiration minutes (`VERIFICATION`, `RESET_PASSWORD`, `ACCESS_TOKEN`); JWT signing configuration comes from environment, not persisted tokens.

## 17. Known Limitations / Technical Debt

Duplicate current-user profile/password endpoints increase API surface. `JwtProvider` contains a fallback signing value when `jwt.secret` is not configured; production deployments should require external configuration.

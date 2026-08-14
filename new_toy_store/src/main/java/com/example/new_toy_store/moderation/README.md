
# Module: Moderation

## 1. Purpose

Moderation owns blacklisted vocabulary used to classify/hide unsuitable review content.

## 2. Package Structure

`api/` admin controller/advice; `application/` blacklist service, facade, cache/listener and DTOs; `domain/` entity/repository/category/exceptions; `mapper/` mapping.

## 3. Entities & Aggregates

`BlacklistedWord` is an aggregate root mapped to `blacklisted_words`, extends `BaseRootEntity`, and owns `id`, unique `word`, `WordCategory` plus audit/delete/version fields. Categories: `PROFANITY`, `SPAM`, `COMPETITOR`, `OTHER`.

## 4. Relationships

No JPA relationships. Review calls the moderation facade/cache.

## 5. Domain Dependencies & Communication

Moderation has no direct business-module imports. It publishes blacklist create/update/delete/restore events; `BlacklistedWordCacheListener` keeps the in-memory cache synchronized. Review queries the facade.

## 6. Main Flows / Use Cases

`Admin command -> normalize/validate word -> version-aware create/update/soft-delete/restore/hard-delete -> publish event -> update cache`. Review scans content using cached words and category severity behavior.

## 7. Business Rules

Word is required, unique and <=100 characters; category is required. Category behavior specifies severity/immediate-hide policy. Deleted records can be restored; hard deletion is a separate action.

## 8. Persistence & Data Strategy

Combined soft/hard delete. Native queries load deleted records; version-aware update/delete/restore operations detect conflicts. Specifications/pageable search are used. An in-memory cache avoids a database lookup for each review.

## 9. Transaction Strategy

Blacklist mutations are transactional; search/cache loads are read-only where declared. Cache synchronization is event-driven.

## 10. API Design

Admin action API with paging/filtering and explicit soft, restore and hard delete endpoints.

## 11. APIs

`BlacklistController`, base `/admin/moderation/blacklists`: `GET /`, `POST /`, `PUT /{id}`, `DELETE /{id}` (soft), `PUT /{id}/restore`, `DELETE /{id}/hard`.

## 12. Error Handling

Missing word, conflict, access and invalid-operation errors are handled by `ModerationExceptionHandler`.

## 13. Security & Authorization

The `/admin/**` rule requires STAFF/MANAGER/ADMIN. No method narrows hard delete to ADMIN.

## 14. Algorithms & Performance Considerations

The normalized in-memory cache enables repeated review text matching without N database queries. Complexity depends on text length and number of cached terms; no trie/automaton implementation was found.

## 15. Architecture & Design Principles

Moderation exposes a facade and events; Review does not access its repository/entity directly.

## 16. Notes / Design Decisions

Soft delete supports reversible policy changes; hard delete is explicitly separate.

## 17. Known Limitations / Technical Debt

The cache is process-local and is not shared between application instances.

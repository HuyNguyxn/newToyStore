
# Module: Upload

## 1. Purpose

Upload is a media application adapter for authenticated image/video upload to Cloudinary. It owns no database entity.

## 2. Package Structure

`api/` controller/advice; `application/` `UploadService` and response DTO; `domain/exception/` upload error. Cloudinary implementation/configuration is under `infrastructure/storage/cloudinary/`.

## 3. Entities & Aggregates

N/A. No entity, aggregate, repository or table.

## 4. Relationships

N/A at JPA level. Returned URLs can later be supplied to Product, Review, Return or User APIs.

## 5. Domain Dependencies & Communication

Upload depends on `CloudinaryStorageService`; it does not directly call other business modules.

## 6. Main Flows / Use Cases

`Multipart file + optional folder -> validate file -> Cloudinary upload -> map provider response -> UploadMediaResponse` for images or videos.

## 7. Business Rules

Accepted file/content constraints are enforced by `UploadService`/provider logic. No upload lifecycle/status enum exists.

## 8. Persistence & Data Strategy

No local database persistence, pagination or locking. Cloudinary stores the media; configured folder values come from environment-backed properties.

## 9. Transaction Strategy

No `@Transactional`; the operation is an external storage call.

## 10. API Design

Multipart POST endpoints return a dedicated media response DTO.

## 11. APIs

`UploadController`, base `/uploads`: `POST /images` and `POST /videos`, both consuming `multipart/form-data`.

## 12. Error Handling

`FileUploadException` is mapped by `UploadExceptionHandler`; provider failures are translated by the storage/service layer.

## 13. Security & Authorization

Both routes require authentication. No role-specific restriction is configured.

## 14. Algorithms & Performance Considerations

No special algorithm. Upload latency/size is dominated by the external provider; no asynchronous upload or local queue was found.

## 15. Architecture & Design Principles

The controller depends on an application service and Cloudinary remains in infrastructure, separating provider details from callers.

## 16. Notes / Design Decisions

Business modules store returned URLs rather than Cloudinary SDK objects.

## 17. Known Limitations / Technical Debt

No provider abstraction interface or cleanup workflow was found.

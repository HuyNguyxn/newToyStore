# Module: Category

## 1. Purpose

Category owns the hierarchical catalog taxonomy and its visibility. It does not own products; Product associates itself with categories.

### Responsibilities

- Create, edit, move, show, hide, delete and restore category tree nodes.
- Maintain slug uniqueness, materialized path, level and display order.
- Prevent self-parenting and category cycles.

### Out of Scope

Product pricing, inventory and product publication are outside this module.

## 2. Package Structure

```text
category/
|- api/             controller and exception advice
|- application/     DTOs, facade and CategoryService
|- domain/          Category, repository, status and exceptions
|- mapper/          CategoryMapper
`- README.md
```

## 3. Entities & Aggregates

### Category — Aggregate Root

File: `category/domain/Category.java`; table: `categories`; extends `BaseRootEntity`. Main data: `id`, `name`, `slug`, `description`, `iconUrl`, `displayOrder`, `status`, `path`, `level`, `parent`, `children`. Statuses: `VISIBLE`, `HIDDEN`, `DELETED`.

## 4. Relationships

`Category 1-N Category` is a self-referencing tree. Parent is `@ManyToOne(fetch = LAZY)`; children use `@OneToMany(cascade = ALL, orphanRemoval = true)`. Deleting/removing children from the aggregate can therefore cascade through descendants.

## 5. Domain Dependencies & Communication

Category has no direct imports from another business module. It publishes `CategoryCreatedEvent`, `CategoryUpdatedEvent` and `CategoryStateChangedEvent`; Product can react through its own integration code.

## 6. Main Flows / Use Cases

### Move Category

```text
CategoryMoveRequest -> validate version/parent
-> reject self-parent or descendant cycle -> update parent/path/level/order
-> version-checked persistence -> response
```

Tree queries build customer-visible or admin trees and compute ancestor paths.

## 7. Business Rules

### 7.1 Validation Rules

Names are required and at most 100 characters; slugs are required, unique, at most 150 characters and match lowercase letters/numbers/hyphens; display order is non-negative; parent IDs are positive; update/move requests carry a version.

### 7.2 Invariants

A category cannot parent itself or create a cycle. Slugs remain unique. Domain status rules control visibility and deletion.

### 7.3 State Transitions

`VISIBLE` can become `HIDDEN` or `DELETED`; `HIDDEN` can be shown or deleted. The enum supplies the valid transition list.

## 8. Persistence & Data Strategy

Soft delete and optimistic versioning come from `BaseRootEntity`. `CategoryRepository` supports specifications, root ordering, and a version-checked status update. Tree construction operates on loaded categories; no cursor pagination is used.

## 9. Transaction Strategy

Category mutations are transactional; query/tree methods use read-only transactions where declared. Moving a node updates its structural data within one local transaction.

## 10. API Design

REST-style DTO endpoints support pagination/search, separate public/admin tree views, command-specific PUT/PATCH routes and exception advice.

## 11. APIs

`CategoryController`, base `/api/categories`:

- `GET /` — paged category search.
- `GET /tree` — customer-visible tree.
- `GET /admin/tree` — full administrative tree.
- `GET /{id}` and `GET /{id}/path` — detail and ancestors.
- `POST /` — create.
- `PUT /{id}/info` — update descriptive fields.
- `PUT /{id}/move` — move/reorder with version.
- `PATCH /{id}/hide`, `PATCH /{id}/show` — visibility.
- `DELETE /{id}` — delete.

## 12. Error Handling

Notable exceptions include `CategoryNotFoundException`, `DuplicateCategorySlugException`, `InvalidCategoryDataException`, `InvalidCategoryOperationException`, `CategoryDeletedConflictException`, `CategoryAccessDeniedException` and `CategoryCrossModuleException`. Category-specific advice also handles data-integrity failures.

## 13. Security & Authorization

Public GET access is limited to configured category patterns; admin tree/search and all writes require STAFF/MANAGER/ADMIN, with delete restricted to ADMIN by URL rules.

## 14. Algorithms & Performance Considerations

Tree construction and cycle detection are the significant algorithms. Materialized `path` and `level` support hierarchy navigation; complexity depends on affected subtree size.

## 15. Architecture & Design Principles

The hierarchy is kept inside one aggregate model and repository abstraction. Events expose changes without importing Product entities.

## 16. Notes / Design Decisions

Both adjacency (`parent_id`) and materialized hierarchy data (`path`, `level`) are stored, trading more complex moves for efficient navigation.

## 17. Known Limitations / Technical Debt

No automated verification of deep-tree moves or concurrent edits was found.

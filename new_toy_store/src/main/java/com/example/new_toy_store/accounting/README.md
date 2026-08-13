# Module: Accounting

## 1. Purpose

Accounting cung cấp sổ kế toán kép và góc nhìn dòng tiền nội bộ cho NewToyStore. Module sở hữu hệ thống tài khoản, nhật ký chung và các dòng bút toán; đồng thời tổng hợp số dư, sổ cái, bảng cân đối thử và kết quả kinh doanh.

### Responsibilities

- Ghi nhận bút toán cân bằng Nợ/Có.
- Tự động hạch toán thanh toán khách hàng, hoàn tiền, nhập hàng và thanh toán nhà cung cấp từ application event.
- Quản lý số dư tài khoản nội bộ và cung cấp hạn mức tiền khả dụng cho module `supplier_payment`.
- Cho phép ADMIN tạo bút toán thủ công và đảo bút toán.
- Cung cấp báo cáo quản trị và dashboard dòng tiền.

### Out of Scope

- Không kết nối tài khoản ngân hàng thật.
- Không đồng bộ hoặc đối soát sao kê ngân hàng.
- Không xử lý thuế, khóa sổ theo kỳ hoặc lập báo cáo tài chính theo chuẩn pháp lý.

## 2. Package Structure

```text
accounting/
|- api/
|  |- AccountingController.java
|  `- advice/AccountingExceptionHandler.java
|- application/
|  |- AccountingFacade.java
|  |- AccountingService.java
|  |- InternalFundQuery.java
|  |- dto/
|  `- listener/AccountingEventListener.java
|- domain/
|  |- LedgerAccount.java
|  |- JournalEntry.java
|  |- JournalEntryLine.java
|  |- repository interfaces
|  |- enums
|  `- exception/
`- README.md
```

- `api`: REST controller và exception handler của accounting.
- `application`: điều phối use case, mapping response hiện tại, báo cáo và event listener.
- `domain`: entity, quy tắc cân bằng, enum và repository abstraction.

Không có package `mapper` hoặc `infrastructure` riêng trong module hiện tại. Các hàm chuyển đổi entity sang response đang nằm trong `AccountingService`.

## 3. Entities & Aggregates

### LedgerAccount — Aggregate Root

**File:** `accounting/domain/LedgerAccount.java`

**Table:** `ledger_accounts`

Đại diện một tài khoản kế toán. Dữ liệu chính gồm mã, tên, loại tài khoản, phía số dư thông thường, cờ tài khoản thanh khoản, cờ hệ thống và trạng thái hoạt động. Entity có phương thức tính số dư dựa trên tổng Nợ/Có.

### JournalEntry — Aggregate Root

**File:** `accounting/domain/JournalEntry.java`

**Table:** `journal_entries`

Đại diện đầu bút toán, gồm số bút toán, ngày ghi sổ, mô tả, nguồn nghiệp vụ, mã tham chiếu, trạng thái, người ghi, bút toán gốc bị đảo và danh sách dòng bút toán.

### JournalEntryLine — Entity

**File:** `accounting/domain/JournalEntryLine.java`

**Table:** `journal_entry_lines`

Đại diện một phát sinh Nợ hoặc Có trên `LedgerAccount`. Mỗi dòng chứa tài khoản, mô tả, số tiền Nợ và số tiền Có.

## 4. Relationships

### JournalEntry 1-N JournalEntryLine

- JPA: `@OneToMany` từ bút toán đến các dòng và `@ManyToOne` từ dòng về bút toán.
- `JournalEntry` sở hữu lifecycle của các dòng; thêm dòng thông qua aggregate root.
- Các dòng được tải LAZY theo mapping hiện tại.

### JournalEntry N-1 LedgerAccount

`JournalEntryLine` tham chiếu `LedgerAccount` bằng `@ManyToOne`. Tài khoản tồn tại độc lập và không bị cascade khi bút toán thay đổi.

### JournalEntry self-reference

Bút toán đảo giữ tham chiếu tới bút toán gốc qua `reversed_entry_id`. Lịch sử được bảo toàn thay vì sửa hoặc xóa bút toán đã ghi.

## 5. Domain Dependencies & Communication

### Accounting <- Customer Payment

`AccountingEventListener` nhận `PaymentCompletedEvent`, `PaymentRefundedEvent` và `OrderStatusChangedEvent`. Tiền khách thanh toán được ghi vào tài khoản `3388` (tiền khách trả trước); chỉ khi đơn chuyển `COMPLETED` hệ thống mới kết chuyển sang doanh thu `511` và ghi nhận giá vốn `632/156`. Hoàn tiền đơn đã hủy giảm `3388`, còn hoàn tiền sau bán giảm doanh thu qua `521`.

### Accounting <- Imports

Listener nhận `ImportNoteCompletedEvent`, tính tổng `quantity * importPrice`, sau đó ghi tăng hàng hóa và công nợ nhà cung cấp.

### Accounting <- Supplier Payment

Listener nhận `SupplierPaymentRecordedEvent` để giảm công nợ và giảm tiền. Ở chiều ngược lại, `supplier_payment` gọi abstraction `InternalFundQuery` để kiểm tra tiền thanh khoản trước khi ghi nhận thanh toán.

Accounting còn gọi `SupplierPaymentFacade` khi dựng dashboard để lấy tổng công nợ mở và quá hạn. Đây là dependency application-level trong cùng modular monolith.

## 6. Main Flows / Use Cases

### Automatic Posting

```text
Nghiệp vụ nguồn commit
        -> AFTER_COMMIT event
        -> kiểm tra sourceType + sourceReference
        -> dựng các dòng Nợ/Có
        -> kiểm tra cân bằng
        -> lưu JournalEntry
```

Các bút toán tự động chính:

| Nghiệp vụ | Nợ | Có |
|---|---|---|
| Thu tiền COD trước khi hoàn tất đơn | `111` | `3388` |
| Thu tiền điện tử trước khi hoàn tất đơn | `112` | `3388` |
| Hoàn tất đơn, ghi nhận doanh thu | `3388` | `511` |
| Hoàn tất đơn, ghi nhận giá vốn | `632` | `156` |
| Hoàn tiền đơn đã hủy | `3388` | `111` hoặc `112` |
| Hoàn tiền hàng bán | `521` | `111` hoặc `112` |
| Hoàn thành phiếu nhập | `156` | `331` |
| Thanh toán NCC | `331` | `111` hoặc `112` |
| Xuất kho trả NCC | `331`/`642` | `156` |
| Hàng trả NCC quay lại kho | `156` | `331`/`642` |

### Manual Posting

ADMIN gửi ngày, mô tả, nguồn, mã tham chiếu và các dòng Nợ/Có. Service chỉ chấp nhận các nguồn thủ công được cho phép, kiểm tra mã tham chiếu chưa tồn tại và kiểm tra tổng Nợ bằng tổng Có trước khi lưu.

### Reversal

Service tải bút toán gốc, từ chối nếu đã đảo, tạo các dòng có Nợ/Có ngược lại, liên kết bút toán đảo với bút toán gốc và đánh dấu bút toán gốc `REVERSED`.

### Reporting

Repository tổng hợp phát sinh theo tài khoản và thời gian. Service chuyển kết quả thành số dư tài khoản, sổ cái, bảng cân đối thử, kết quả kinh doanh và dashboard.

## 7. Business Rules

### 7.1 Validation Rules

- `description` và `sourceReference` của bút toán thủ công không được trống.
- Danh sách dòng không được rỗng.
- `accountCode` không được trống.
- Số tiền Nợ/Có không được âm.
- `from` không được sau `to`.

### 7.2 Invariants

- Tổng Nợ phải bằng tổng Có với sai số nhỏ hơn `0.01`.
- Một dòng chỉ được có số tiền ở đúng một phía.
- Tài khoản phải tồn tại và đang hoạt động.
- Cặp `sourceType + sourceReference` là duy nhất.
- Nguồn tự động không được giả lập qua API bút toán thủ công.
- Bút toán đã đảo không được đảo lần thứ hai.

### 7.3 State Transitions

```text
POSTED -> REVERSED
```

Việc đảo tạo một bút toán `REVERSAL` mới; không cập nhật trực tiếp số tiền trên bút toán cũ.

## 8. Persistence & Data Strategy

- Flyway V8 tạo `ledger_accounts`, `journal_entries`, `journal_entry_lines` và seed 9 tài khoản hệ thống.
- Unique constraint `uk_journal_source` bảo vệ idempotency tại database.
- Check constraint `chk_journal_line_one_side` bảo vệ một dòng chỉ ghi Nợ hoặc Có.
- Index được đặt trên mã tài khoản, ngày bút toán, nguồn, trạng thái và khóa ngoại dòng bút toán.
- Query số dư dùng aggregate projection `Object[]`; sổ cái dùng `Pageable`.
- Entity kế thừa các base entity dùng timestamp/version theo mapping hiện tại.
- Số tiền dùng `DOUBLE` để đồng bộ với phần còn lại của dự án và được làm tròn hai chữ số bằng `Math.round`.

## 9. Transaction Strategy

- Query báo cáo dùng `@Transactional(readOnly = true)`.
- Tạo và đảo bút toán dùng transaction ghi thông thường.
- `postAutomatic` dùng `REQUIRES_NEW` sau khi transaction nghiệp vụ nguồn đã commit.
- Event listener dùng `@TransactionalEventListener(phase = AFTER_COMMIT, fallbackExecution = true)`.

Không có distributed transaction hoặc transactional outbox trong luồng accounting hiện tại.

## 10. API Design

API tách request/response khỏi entity, dùng Bean Validation, `Pageable` cho danh sách và tham số ngày ISO cho báo cáo. Endpoint đọc là resource/query-oriented; endpoint ghi thủ công và đảo bút toán là command rõ ràng.

Không có API sửa hoặc xóa trực tiếp bút toán.

## 11. APIs

### AccountingController

**File:** `accounting/api/AccountingController.java`

**Base path:** `/api/accounting`

| Method | Endpoint | Chức năng | Authorization |
|---|---|---|---|
| GET | `/dashboard` | Tổng quan quỹ, tồn kho, công nợ và lợi nhuận | MANAGER, ADMIN |
| GET | `/accounts` | Số dư hệ thống tài khoản | MANAGER, ADMIN |
| GET | `/journal-entries` | Nhật ký chung có phân trang | MANAGER, ADMIN |
| GET | `/journal-entries/{id}` | Chi tiết bút toán | MANAGER, ADMIN |
| GET | `/general-ledger/{accountCode}` | Sổ cái theo tài khoản và khoảng ngày | MANAGER, ADMIN |
| GET | `/reports/trial-balance` | Bảng cân đối thử tại một ngày | MANAGER, ADMIN |
| GET | `/reports/income-statement` | Kết quả kinh doanh theo khoảng ngày | MANAGER, ADMIN |
| POST | `/journal-entries` | Tạo bút toán thủ công | ADMIN |
| POST | `/journal-entries/{id}/reverse` | Đảo bút toán | ADMIN |

## 12. Error Handling

- `LedgerAccountNotFoundException`: mã tài khoản không tồn tại hoặc không hoạt động.
- `JournalEntryNotFoundException`: không tìm thấy bút toán.
- `InvalidJournalEntryException`: bút toán không cân bằng, sai nguồn, trùng tham chiếu, sai khoảng ngày hoặc thao tác đảo không hợp lệ.
- `AccountingExceptionHandler` chuyển domain exception thành HTTP error response theo cơ chế advice của module.

## 13. Security & Authorization

Controller yêu cầu `MANAGER` hoặc `ADMIN` ở cấp class. Hai command nhạy cảm là tạo bút toán thủ công và đảo bút toán yêu cầu riêng vai trò `ADMIN`.

## 14. Algorithms & Performance Considerations

- Query tổng hợp phát sinh được đẩy xuống database thay vì tải toàn bộ dòng bút toán.
- Số dư được ghép theo `Map<accountId, totals>` để tra cứu O(1) khi dựng danh sách tài khoản.
- `sourceType + sourceReference` kết hợp kiểm tra ứng dụng và unique constraint để chống ghi trùng.
- Danh sách nhật ký gọi mapper nội bộ trong service; do không fetch collection theo trang, việc đọc `lines` có nguy cơ phát sinh thêm query cho từng bút toán.

## 15. Architecture & Design Principles

Module áp dụng DDD Lite với aggregate, repository abstraction, application facade và domain exception. Giao tiếp ghi từ module khác đi qua event; giao tiếp đọc tiền khả dụng đi qua `InternalFundQuery`. DTO được tách khỏi entity.

Accounting là sổ phụ quản trị trong modular monolith, không phải một microservice độc lập và không phải hệ thống kế toán pháp lý.

## 16. Notes / Design Decisions

- Tài khoản `111` và `112` được đánh dấu là tài khoản thanh khoản.
- `ledgerAccountsPayable` lấy từ tài khoản `331`; `supplierOutstanding` lấy từ invoice của `supplier_payment`. Sự chênh lệch giúp phát hiện dữ liệu lịch sử chưa được hạch toán.
- Hệ thống không tự suy đoán vốn ban đầu. ADMIN phải tạo bút toán `OPENING_BALANCE` trước khi thanh toán NCC nếu sổ chưa có tiền.
- Bút toán lịch sử được bảo toàn bằng reversal thay vì update/delete.

## 17. Known Limitations / Technical Debt

- Chưa có package `mapper`; logic `JournalEntry -> JournalEntryResponse`, `LedgerAccount -> AccountBalanceResponse` và request-to-posting nằm trong `AccountingService`.
- Chưa có specification/filter động cho nhật ký chung ngoài `Pageable` và các tham số ngày của báo cáo.
- Chưa có khóa kỳ kế toán và chưa backfill giao dịch phát sinh trước Flyway V8.
- Chưa có transactional outbox; event nội bộ sau commit vẫn chạy trong cùng process.
- Số tiền dùng `DOUBLE`; hệ thống tài chính thực tế nên dùng `DECIMAL`/`BigDecimal`.
- Chưa tích hợp hoặc đối soát ngân hàng thật theo phạm vi chủ động của dự án.

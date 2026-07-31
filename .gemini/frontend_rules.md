# 🎮 NewToyStore Frontend Development Rules

> Bộ rule này dành cho **intern backend** xây dựng giao diện frontend.
> Được tạo từ phân tích source code thực tế của dự án backend.

---

## 1. BỐI CẢNH DỰ ÁN

```
Dự án      : NewToyStore — Hệ thống quản lý & bán hàng cửa hàng đồ chơi
Backend    : Spring Boot 3.3.5 · Java 21 · MySQL · Spring Security + JWT
API Server : http://localhost:8080
Frontend   : http://localhost:5173 (Vite dev server)
Kiến trúc  : Domain-Driven Design (DDD) + Facade Pattern + Event-Driven
```

Backend gồm 14 domain: **User, Product, Category, Cart, Order, Payment, Review, Promotion, Supplier, Imports, Logistics, Customer Return, Supplier Return, Moderation**.

---

## 2. TECH STACK FRONTEND

```
Framework       : React 18+ (Vite)
Styling         : Vanilla CSS (KHÔNG TailwindCSS, KHÔNG Bootstrap)
HTTP Client     : fetch API hoặc axios
Routing         : react-router-dom v6
State           : React Context + useReducer (auth, cart)
Ngôn ngữ        : JavaScript thuần (KHÔNG TypeScript)
```

---

## 3. QUY TẮC CHUNG CHO INTERN

- Giữ code ĐƠN GIẢN, DỄ ĐỌC. Ưu tiên "hoạt động đúng" trước, "đẹp" sau.
- Mỗi component chỉ làm MỘT việc. Nếu dài hơn 150 dòng → tách ra.
- Đặt tên file và biến bằng tiếng Anh. Comment tiếng Việt khi logic phức tạp.
- KHÔNG copy-paste. Nếu lặp lại → tạo component/hook tái sử dụng.
- Mỗi domain backend tương ứng 1 file service ở frontend (`src/services/<domain>Service.js`).

---

## 4. CẤU TRÚC THƯ MỤC

```
src/
├── assets/                  # Ảnh, icon tĩnh
├── components/
│   ├── common/              # Button, Modal, Pagination, StatusBadge, Loading
│   ├── layout/              # Header, Sidebar, Footer, CustomerLayout, AdminLayout
│   └── forms/               # InputField, SelectField, TextArea, FormGroup
├── contexts/                # AuthContext, CartContext
├── hooks/                   # useAuth, useCart, useFetch, useDebounce
├── pages/
│   ├── auth/                # LoginPage, RegisterPage, VerifyPage, ForgotPasswordPage
│   ├── home/                # HomePage
│   ├── products/            # ProductListPage, ProductDetailPage
│   ├── cart/                # CartPage, CheckoutPage
│   ├── orders/              # OrderListPage, OrderDetailPage
│   ├── profile/             # ProfilePage, AddressPage
│   ├── reviews/             # MyReviewsPage
│   ├── returns/             # ReturnRequestPage, ReturnListPage
│   └── admin/               # AdminDashboard, AdminProducts, AdminOrders, AdminUsers...
├── services/                # API service files
├── utils/                   # formatPrice, formatDate, statusMaps
├── styles/
│   ├── variables.css
│   ├── reset.css
│   └── global.css
├── App.jsx
└── main.jsx
```

---

## 5. API CLIENT & AUTHENTICATION

### 5.1 API Client

```javascript
// src/services/apiClient.js
const API_BASE = 'http://localhost:8080';

async function apiClient(endpoint, options = {}) {
  const token = localStorage.getItem('token');
  const headers = { 'Content-Type': 'application/json', ...options.headers };
  if (token) headers['Authorization'] = `Bearer ${token}`;

  const response = await fetch(`${API_BASE}${endpoint}`, { ...options, headers });

  if (response.status === 401) {
    localStorage.removeItem('token');
    window.location.href = '/login';
    return;
  }

  if (!response.ok) {
    const error = await response.json();
    throw error; // { status, error, message, path, details }
  }

  if (response.status === 204) return null;
  return response.json();
}
```

### 5.2 Endpoints công khai (KHÔNG cần JWT)

```
POST   /users/register
POST   /users/login
GET    /users/verify?token=...
POST   /users/forgot-password
POST   /users/reset-password
GET    /products/**
GET    /api/categories/**
GET    /payments/vnpay-return
GET    /payments/vnpay-ipn
```

### 5.3 JWT Authentication Flow

```
Đăng ký:
  POST /users/register { email, password, fullName, phoneNumber }
  → Server gửi email xác thực → User click link verify

Đăng nhập:
  POST /users/login { email, password }
  → Response: { token, user: { id, email, fullName, role, status } }
  → Lưu token vào localStorage, user vào AuthContext

Lấy thông tin user hiện tại:
  GET /users/me (cần JWT)
  → Response: UserProfileResponse

Đăng xuất:
  Xóa token khỏi localStorage → Reset AuthContext → Redirect /login
```

### 5.4 Phân quyền theo Role

```
CUSTOMER  : Mua sắm, giỏ hàng, đặt hàng, đánh giá, đổi trả
STAFF     : Xem đơn hàng, vận chuyển, thanh toán
MANAGER   : Quản lý thanh toán, hoàn tiền, vận chuyển
ADMIN     : Toàn quyền quản trị
```

---

## 6. BẢNG THAM CHIẾU API ĐẦY ĐỦ

> ⚠️ Tất cả ID trong hệ thống là kiểu `Integer` (không phải Long).

### 6.1 USER & AUTH (`/users`)

| Method | Endpoint | Body / Params | Response | Auth |
|--------|----------|---------------|----------|------|
| POST | `/users/register` | `{ email, password, fullName, phoneNumber }` | `UserResponse` | Public |
| POST | `/users/login` | `{ email, password }` | `AuthResponse` | Public |
| GET | `/users/verify` | `?token=...` | message | Public |
| POST | `/users/forgot-password` | `{ email }` | message | Public |
| POST | `/users/reset-password` | `{ token, newPassword }` | message | Public |
| GET | `/users/me` | — | `UserProfileResponse` | ✅ |
| PUT | `/users/me` | `ProfileUpdateRequest` | `UserProfileResponse` | ✅ |
| PATCH | `/users/me/password` | `{ currentPassword, newPassword }` | message | ✅ |
| POST | `/users/me/addresses` | `AddressRequest` | `UserProfileResponse` | ✅ |
| PATCH | `/users/me/addresses/{id}/default` | — | `UserProfileResponse` | ✅ |
| DELETE | `/users/me/addresses/{id}` | — | 204 | ✅ |
| GET | `/users` | `?keyword&status&role` + Pageable | `Page<UserAdminResponse>` | ADMIN |
| GET | `/users/{id}` | — | `UserAdminResponse` | ADMIN |
| PATCH | `/users/{id}/role` | `{ role }` | `UserAdminResponse` | ADMIN |
| PATCH | `/users/{id}/status` | `{ status }` | `UserAdminResponse` | ADMIN |
| PATCH | `/users/{id}/lock` | — | `UserAdminResponse` | ADMIN |
| PATCH | `/users/{id}/unlock` | — | `UserAdminResponse` | ADMIN |
| DELETE | `/users/{id}` | — | 204 | ADMIN |

**AuthResponse:**
```json
{ "token": "eyJ...", "user": { "id", "email", "fullName", "role", "status" } }
```

**UserProfileResponse:**
```json
{
  "id": 1, "email": "...", "fullName": "...", "phoneNumber": "...",
  "avatarUrl": "...", "role": "CUSTOMER", "status": "ACTIVE",
  "addresses": [
    { "receiverName", "receiverPhone", "detailAddress", "isDefault": true }
  ]
}
```

**UserRole:** `CUSTOMER | STAFF | MANAGER | ADMIN`
**UserStatus:** `PENDING | ACTIVE | LOCKED | DELETED`

---

### 6.2 PRODUCT (`/products`)

| Method | Endpoint | Body / Params | Response | Auth |
|--------|----------|---------------|----------|------|
| GET | `/products` | Pageable | `Page<ProductResponse>` | Public |
| GET | `/products/category/{categoryId}` | Pageable | `Page<ProductResponse>` | Public |
| GET | `/products/search` | `?keyword=` + Pageable | `Page<ProductResponse>` | Public |
| GET | `/products/filter` | `?minPrice&maxPrice&status` + Pageable | `Page<ProductResponse>` | Public |
| GET | `/products/{id}` | — | `ProductResponse` | Public |
| POST | `/products` | `CreateProductRequest` | `ProductResponse` | ADMIN |
| PUT | `/products/{id}` | `UpdateProductRequest` | `ProductResponse` | ADMIN |
| POST | `/products/{id}/images` | `{ imageUrl, thumbnail }` | `ProductResponse` | ADMIN |
| DELETE | `/products/{id}/images/{imageId}` | — | 204 | ADMIN |
| PATCH | `/products/{id}/variants/{variantId}/price` | `{ price }` | void | ADMIN |
| PATCH | `/products/{id}/variants/{variantId}/stock` | `{ amount }` | void | ADMIN |
| PATCH | `/products/{id}/images/{imageId}/thumbnail` | — | void | ADMIN |
| DELETE | `/products/{id}` | — | 204 | ADMIN |

**CreateProductRequest:**
```json
{
  "name": "Xe tăng điều khiển",
  "basePrice": 350000,
  "categoryIds": [1, 2],
  "status": "ACTIVE",
  "defaultInitialStock": 10,
  "supplierId": 1,
  "variants": [
    {
      "attributes": { "Màu": "Đỏ", "Size": "L" },
      "initialStock": 50,
      "price": 350000,
      "isMaster": true
    }
  ]
}
```

**ProductResponse:**
```json
{
  "id": 1,
  "name": "Xe tăng điều khiển",
  "basePrice": 350000,
  "status": "ACTIVE",
  "supplierId": 1,
  "supplierName": "Nhà CC ABC",
  "categoryIds": [1, 2],
  "averageRating": 4.5,
  "reviewCount": 12,
  "statusDetail": "ACTIVE",
  "allowedNextStatuses": ["INACTIVE"],
  "allowedActions": ["UPDATE", "DELETE", "ADD_IMAGE"],
  "variants": [
    {
      "id": 1,
      "type": "MASTER",
      "price": 350000,
      "discountedPrice": 280000,
      "stockQuantity": 50,
      "attributes": { "Màu sắc": "Đỏ", "Kích thước": "Lớn" },
      "typeDetail": "MASTER",
      "allowedNextTypes": ["REGULAR"],
      "allowedActions": ["UPDATE_PRICE", "ADD_STOCK"]
    }
  ]
}
```

**ProductStatus:** `ACTIVE | INACTIVE | OUT_OF_STOCK`
**VariantType:** `DEFAULT | MASTER | REGULAR`

---

### 6.3 INVENTORY (`/inventory`)

| Method | Endpoint | Response | Auth |
|--------|----------|----------|------|
| GET | `/inventory/variants/{variantId}/batches` | `List<InventoryBatchResponse>` | ADMIN |

**InventoryBatchResponse:** `{ batchNumber, expiryDate, quantity }`

---

### 6.4 CATEGORY (`/api/categories`)

| Method | Endpoint | Body / Params | Response | Auth |
|--------|----------|---------------|----------|------|
| GET | `/api/categories` | `?keyword&status` + Pageable | `Page<CategorySummaryResponse>` | Public |
| GET | `/api/categories/tree` | — | `List<CategoryDetailResponse>` | Public |
| GET | `/api/categories/admin/tree` | — | `List<CategoryDetailResponse>` | ADMIN |
| GET | `/api/categories/{id}` | — | `CategoryDetailResponse` | Public |
| GET | `/api/categories/{id}/path` | — | `List<CategorySummaryResponse>` | Public |
| POST | `/api/categories` | `CategoryCreateRequest` | `CategoryDetailResponse` (201) | ADMIN |
| PUT | `/api/categories/{id}/info` | `CategoryUpdateInfoRequest` | `CategoryDetailResponse` | ADMIN |
| PUT | `/api/categories/{id}/move` | `CategoryMoveRequest` | `CategoryDetailResponse` | ADMIN |
| PATCH | `/api/categories/{id}/hide` | — | 204 | ADMIN |
| PATCH | `/api/categories/{id}/show` | — | 204 | ADMIN |
| DELETE | `/api/categories/{id}` | — | 204 | ADMIN |

**CategoryDetailResponse:**
```json
{
  "id": 1, "name": "Xe điều khiển", "slug": "xe-dieu-khien",
  "description": "...", "iconUrl": "...", "displayOrder": 1,
  "level": 1, "path": "/xe-dieu-khien/",
  "status": "VISIBLE",
  "allowedNextActions": ["HIDDEN"],
  "parentId": null, "parentName": null,
  "subCategories": []
}
```

**CategoryStatus:** `VISIBLE | HIDDEN | DELETED`
**Max depth:** 3 levels

---

### 6.5 CART (`/carts`)

| Method | Endpoint | Body / Params | Response | Auth |
|--------|----------|---------------|----------|------|
| GET | `/carts/{userId}` | `?promoCode=` | `CartResponse` | ✅ |
| POST | `/carts/{userId}/items` | `{ productId, variantId, quantity }` | `CartResponse` | ✅ |
| POST | `/carts/{userId}/sync` | `{ items: [{ productId, variantId, quantity }] }` | `CartResponse` | ✅ |
| PUT | `/carts/{userId}/items/{itemId}` | `{ quantity }` | `CartResponse` | ✅ |
| PATCH | `/carts/{userId}/items/{itemId}/toggle` | `{ selected: true/false }` | `CartResponse` | ✅ |
| DELETE | `/carts/{userId}/items/{itemId}` | — | `CartResponse` | ✅ |
| DELETE | `/carts/{userId}` | — | 204 | ✅ |
| POST | `/carts/{userId}/checkout` | `{ shippingAddress, promoCode? }` | message | ✅ |

**CartResponse:**
```json
{
  "cartId": 1, "userId": 1,
  "status": "ACTIVE",
  "allowedNextStates": ["CHECKING_OUT"],
  "cartTotal": 700000,
  "appliedPromoCode": "SUMMER2024",
  "orderDiscountAmount": 50000,
  "finalTotal": 650000,
  "promoMessage": "Giảm 50.000đ cho đơn từ 500.000đ",
  "allowedActions": ["ADD_ITEM", "CHECKOUT"],
  "items": [
    {
      "id": 1, "productId": 1, "variantId": 1,
      "productName": "Xe tăng", "variantAttributes": "Màu: Đỏ, Size: L",
      "thumbnailUrl": "https://...",
      "addedPrice": 350000,
      "originalPrice": 350000,
      "finalPrice": 280000,
      "quantity": 2,
      "isSelected": true,
      "isAvailable": true,
      "hasPriceChanged": false,
      "message": null
    }
  ]
}
```

**CartStatus:** `ACTIVE | CHECKING_OUT`

---

### 6.6 ORDER (`/orders`)

| Method | Endpoint | Body / Params | Response | Auth |
|--------|----------|---------------|----------|------|
| GET | `/orders/my-orders` | Pageable | `Page<OrderResponse>` | ✅ |
| GET | `/orders/{id}` | — | `OrderResponse` | Owner/ADMIN |
| POST | `/orders` | `OrderRequest` | `OrderResponse` | ✅ |
| PATCH | `/orders/{id}/confirm` | `?note=` | `OrderResponse` | ADMIN |
| PATCH | `/orders/{id}/ship` | `?note=` | `OrderResponse` | ADMIN |
| PATCH | `/orders/{id}/complete` | `?note=` | `OrderResponse` | ADMIN |
| PATCH | `/orders/{id}/cancel` | `?note=` | `OrderResponse` | Owner/ADMIN |
| DELETE | `/orders/{id}` | — | 204 | ADMIN |
| GET | `/orders/admin/filter` | `OrderFilterRequest` + Pageable | `Page<OrderResponse>` | ADMIN |
| PATCH | `/orders/{id}/shipping-address` | `{ newAddress, note }` | `OrderResponse` | Owner/ADMIN |

**OrderRequest:**
```json
{
  "userId": 1,
  "shippingAddress": "123 Nguyễn Huệ, Q1, HCM",
  "promoCode": "SUMMER2024",
  "items": [
    { "productId": 1, "variantId": 1, "quantity": 2 }
  ]
}
```

**OrderResponse:**
```json
{
  "id": 1, "userId": 1,
  "status": "PENDING",
  "totalAmount": 700000,
  "shippingAddress": "...",
  "promoCode": "SUMMER2024",
  "discountAmount": 50000,
  "createdAt": "...", "updatedAt": "...",
  "availableActions": ["CONFIRM", "CANCEL"],
  "allowedNextActions": ["CONFIRMED", "CANCELLED"],
  "items": [
    {
      "id": 1, "productId": 1, "variantId": 1,
      "productName": "Xe tăng",
      "variantAttributesSnapshot": "Màu sắc: Đỏ, Kích thước: Lớn",
      "quantity": 2, "price": 350000
    }
  ],
  "histories": [
    { "id": 1, "status": "PENDING", "note": "Đơn hàng được tạo", "createdAt": "..." }
  ]
}
```

**OrderStatus State Machine:**
```
PENDING → CONFIRMED → SHIPPED → COMPLETED
  ↓          ↓                      ↓
CANCELLED  CANCELLED        PARTIALLY_REFUNDED
                                    ↓
                             FULLY_REFUNDED
```

**OrderFilterRequest (Admin):** `?userId=&status=&fromDate=&toDate=&minAmount=&maxAmount=`

---

### 6.7 PAYMENT (`/payments`)

| Method | Endpoint | Body / Params | Response | Auth |
|--------|----------|---------------|----------|------|
| POST | `/payments/checkout` | `PaymentCheckoutRequest` | `PaymentResponse` | ✅ |
| GET | `/payments/vnpay-return` | Query params từ VNPay | `VnpayReturnResponse` | Public |
| GET | `/payments/vnpay-ipn` | Query params từ VNPay | `VnpayIpnResponse` | Public |
| GET | `/payments/my-payments` | Pageable | `Page<PaymentResponse>` | ✅ |
| GET | `/payments/{id}` | — | `PaymentResponse` | Owner/ADMIN |
| GET | `/payments/admin/filter` | FilterRequest + Pageable | `Page<PaymentResponse>` | STAFF+ |
| POST | `/payments/{id}/refunds` | `{ amount, method, reason }` | `PaymentRefundResponse` | MANAGER+ |
| GET | `/payments/{id}/refunds` | Pageable | `Page<PaymentRefundResponse>` | STAFF+ |
| PATCH | `/payments/refunds/{id}/process` | — | `PaymentRefundResponse` | MANAGER+ |
| PATCH | `/payments/refunds/{id}/reject` | `{ reason }` | `PaymentRefundResponse` | MANAGER+ |
| DELETE | `/payments/refunds/{id}` | — | 204 | MANAGER+ |
| PATCH | `/payments/{id}/succeed` | `{ providerTransactionId }` | `PaymentResponse` | STAFF+ |
| PATCH | `/payments/{id}/fail` | `{ reason }` | `PaymentResponse` | STAFF+ |
| PATCH | `/payments/{id}/cancel` | `{ reason }` | `PaymentResponse` | Owner/ADMIN |
| DELETE | `/payments/{id}` | — | 204 | MANAGER+ |

**PaymentCheckoutRequest:**
```json
{
  "orderId": 1,
  "method": "VNPAY",
  "idempotencyKey": "uuid-unique-key"
}
```

**PaymentResponse:**
```json
{
  "id": 1, "orderId": 1, "userId": 1,
  "method": "VNPAY", "status": "PENDING",
  "amount": 650000,
  "providerTransactionId": null,
  "failureReason": null, "cancelReason": null,
  "paidAt": null, "expiredAt": "...",
  "createdAt": "...", "updatedAt": "...",
  "idempotencyKey": "...",
  "paymentUrl": "https://sandbox.vnpayment.vn/...",
  "gatewayMessage": null,
  "availableActions": ["CANCEL"],
  "allowedNextStatuses": ["SUCCEEDED", "FAILED", "CANCELLED"],
  "nextActions": [
    { "code": "CANCEL", "label": "Hủy thanh toán", "description": "..." }
  ]
}
```

**PaymentMethod:** `COD | VNPAY`
**PaymentStatus:** `PENDING | SUCCEEDED | FAILED | CANCELLED | EXPIRED | REFUND_PENDING | REFUNDED | REFUND_FAILED`
**RefundMethod:** `COD_MANUAL | VNPAY`
**RefundStatus:** `PENDING | PROCESSING | SUCCEEDED | FAILED | REJECTED | CANCELLED`

---

### 6.8 REVIEW (`/reviews`)

| Method | Endpoint | Body / Params | Response | Auth |
|--------|----------|---------------|----------|------|
| POST | `/reviews` | `ReviewCreateRequest` | `ReviewResponse` | ✅ |
| PUT | `/reviews/{id}` | `ReviewUpdateRequest` | `ReviewResponse` | Owner |
| DELETE | `/reviews/{id}` | — | 204 | Owner |
| GET | `/reviews/me` | Pageable | `Page<ReviewResponse>` | ✅ |
| GET | `/reviews/products/{productId}/summary` | — | `ReviewSummaryResponse` | Public |
| GET | `/reviews/products/{productId}` | Pageable | `Page<ReviewResponse>` | Public |
| GET | `/reviews/admin/products/{productId}` | Pageable | `Page<ReviewResponse>` | ADMIN |
| GET | `/reviews/admin/all` | FilterRequest + Pageable | `Page<ReviewResponse>` | ADMIN |
| PATCH | `/reviews/admin/{id}/status` | `{ status }` | `ReviewResponse` | ADMIN |
| PATCH | `/reviews/admin/{id}/reply` | `{ reply }` | `ReviewResponse` | ADMIN |

**ReviewCreateRequest:**
```json
{
  "productId": 1,
  "orderItemId": 5,
  "rating": 5,
  "comment": "Đồ chơi rất đẹp!"
}
```

**ReviewSummaryResponse:** `{ averageRating, totalReviews, ratingDistribution: {1: 2, 2: 0, ...} }`
**ReviewStatus:** `PENDING | PUBLISHED | HIDDEN | REJECTED`

---

### 6.9 PROMOTION (`/api/v1/promotions`)

| Method | Endpoint | Body / Params | Response | Auth |
|--------|----------|---------------|----------|------|
| GET | `/api/v1/promotions` | FilterRequest + Pageable | `Page<PromotionResponse>` | ADMIN |
| GET | `/api/v1/promotions/{id}` | — | `PromotionResponse` | ADMIN |
| GET | `/api/v1/promotions/code/{code}` | — | `PromotionResponse` | ✅ |
| POST | `/api/v1/promotions` | `PromotionRequest` | `PromotionResponse` | ADMIN |
| PUT | `/api/v1/promotions/{id}` | `PromotionRequest` | `PromotionResponse` | ADMIN |
| DELETE | `/api/v1/promotions/{id}` | — | 204 | ADMIN |
| PATCH | `/api/v1/promotions/{id}/activate` | — | `PromotionResponse` | ADMIN |
| PATCH | `/api/v1/promotions/{id}/deactivate` | — | `PromotionResponse` | ADMIN |
| GET | `/api/v1/promotions/calculate-product` | `?productId&promoCode` | discount info | ✅ |
| GET | `/api/v1/promotions/calculate-order` | `?orderTotal&promoCode` | discount info | ✅ |
| POST | `/api/v1/promotions/active-for-products` | `{ productIds: [...] }` | promotions map | Public |

**PromotionType:** `PERCENTAGE | FIXED_AMOUNT`
**PromotionScope:** `PRODUCT | ORDER | SHIPPING`

---

### 6.10 SUPPLIER (`/suppliers`) — Admin only

| Method | Endpoint | Body / Params | Response |
|--------|----------|---------------|----------|
| GET | `/suppliers` | `?keyword&status` + Pageable | `Page<SupplierResponse>` |
| GET | `/suppliers/{id}` | — | `SupplierResponse` |
| POST | `/suppliers` | `SupplierCreateRequest` | `SupplierResponse` |
| PUT | `/suppliers/{id}` | `SupplierUpdateRequest` | `SupplierResponse` |
| DELETE | `/suppliers/{id}` | — | 204 |
| PATCH | `/suppliers/{id}/status` | `{ status }` | `SupplierResponse` |
| PATCH | `/suppliers/{id}/restore` | — | `SupplierResponse` |

**SupplierStatus:** `ACTIVE | INACTIVE`

---

### 6.11 IMPORTS (`/imports`) — Admin only

| Method | Endpoint | Body / Params | Response |
|--------|----------|---------------|----------|
| GET | `/imports` | FilterRequest + Pageable | `Page<ImportNoteResponse>` |
| GET | `/imports/{id}` | — | `ImportNoteResponse` |
| POST | `/imports` | `ImportNoteRequest` | `ImportNoteResponse` |
| PATCH | `/imports/{id}/complete` | — | `ImportNoteResponse` |
| PATCH | `/imports/{id}/cancel` | — | `ImportNoteResponse` |

**ImportStatus:** `DRAFT | COMPLETED | CANCELLED`

---

### 6.12 LOGISTICS (`/shipments`)

| Method | Endpoint | Body / Params | Response | Auth |
|--------|----------|---------------|----------|------|
| POST | `/shipments/orders/{orderId}` | ShipmentCreateRequest | `ShipmentResponse` | STAFF+ |
| GET | `/shipments/my-shipments` | Pageable | `Page<ShipmentResponse>` | ✅ |
| GET | `/shipments/{id}` | — | `ShipmentResponse` | Owner/STAFF+ |
| GET | `/shipments/{id}/tracking-logs` | — | `List<TrackingLogResponse>` | Owner/STAFF+ |
| GET | `/shipments/admin/filter` | FilterRequest + Pageable | `Page<ShipmentResponse>` | STAFF+ |
| POST | `/shipments/{id}/actions` | `{ action, note }` | `ShipmentResponse` | STAFF+ |
| DELETE | `/shipments/{id}` | — | 204 | MANAGER+ |

**ShipmentStatus:** `PENDING | PICKED_UP | IN_TRANSIT | DELIVERED | FAILED | CANCELLED | RETURNED`
**ShippingProviderCode:** `GHN | GHTK | VNPOST | NINJAVAN`

---

### 6.13 CUSTOMER RETURN (`/api/returns`)

| Method | Endpoint | Body / Params | Response | Auth |
|--------|----------|---------------|----------|------|
| GET | `/api/returns` | FilterRequest + Pageable | `Page<CustomerReturnResponse>` | ✅ |
| POST | `/api/returns` | `CustomerReturnRequest` | `CustomerReturnResponse` | ✅ |
| PATCH | `/api/returns/{id}/cancel` | — | `CustomerReturnResponse` | Owner |
| PATCH | `/api/returns/{id}/update-info` | request body | `CustomerReturnResponse` | Owner |
| PATCH | `/api/returns/{id}/dispute` | — | `CustomerReturnResponse` | Owner |
| PATCH | `/api/returns/{id}/require-info` | — | `CustomerReturnResponse` | ADMIN |
| PATCH | `/api/returns/{id}/receive` | — | `CustomerReturnResponse` | ADMIN |
| PATCH | `/api/returns/{id}/inspect` | — | `CustomerReturnResponse` | ADMIN |
| PATCH | `/api/returns/{id}/resolve-dispute` | — | `CustomerReturnResponse` | ADMIN |
| PATCH | `/api/returns/{id}/finalize-refund` | — | `CustomerReturnResponse` | ADMIN |

**CustomerReturnStatus Flow:**
```
REQUESTED → INFO_REQUIRED → APPROVED → ITEMS_RECEIVED → INSPECTED → REFUNDED
    ↓              ↓            ↓
 CANCELLED     CANCELLED    REJECTED → DISPUTED → (resolve)
```

---

### 6.14 SUPPLIER RETURN (`/api/supplier-returns`)

| Method | Endpoint | Body / Params | Response | Auth |
|--------|----------|---------------|----------|------|
| GET | `/api/supplier-returns/sla/critical-alerts` | — | alerts | ADMIN |
| GET | `/api/supplier-returns` | FilterRequest + Pageable | `Page<SupplierReturnResponse>` | ADMIN |
| GET | `/api/supplier-returns/{id}` | — | `SupplierReturnResponse` | ADMIN |
| POST | `/api/supplier-returns` | `SupplierReturnRequest` | `SupplierReturnResponse` | ADMIN |
| PATCH | `/api/supplier-returns/{id}/submit` | — | `SupplierReturnResponse` | ADMIN |
| PATCH | `/api/supplier-returns/{id}/approve` | — | `SupplierReturnResponse` | ADMIN |
| PATCH | `/api/supplier-returns/{id}/reject` | — | `SupplierReturnResponse` | ADMIN |
| PATCH | `/api/supplier-returns/{id}/ship` | — | `SupplierReturnResponse` | ADMIN |
| PATCH | `/api/supplier-returns/{id}/inspect` | — | `SupplierReturnResponse` | ADMIN |
| PATCH | `/api/supplier-returns/{id}/complete` | — | `SupplierReturnResponse` | ADMIN |

**SupplierReturnStatus:** `DRAFT → PENDING_APPROVAL → APPROVED → SHIPPED → INSPECTED → COMPLETED` (hoặc `REJECTED`)

---

### 6.15 MODERATION (`/admin/moderation/blacklists`) — Admin only

| Method | Endpoint | Body / Params | Response |
|--------|----------|---------------|----------|
| GET | `/admin/moderation/blacklists` | FilterRequest + Pageable | `Page<BlacklistedWordResponse>` |
| POST | `/admin/moderation/blacklists` | `{ word, category }` | `BlacklistedWordResponse` |
| PUT | `/admin/moderation/blacklists/{id}` | `{ word, category }` | `BlacklistedWordResponse` |
| DELETE | `/admin/moderation/blacklists/{id}` | — | 204 (soft) |
| PUT | `/admin/moderation/blacklists/{id}/restore` | — | `BlacklistedWordResponse` |
| DELETE | `/admin/moderation/blacklists/{id}/hard` | — | 204 (hard) |

**WordCategory:** `VULGAR | PROFANITY | SPAM | SENSITIVE | OTHER`

---

## 7. XỬ LÝ LỖI

### 7.1 ErrorResponse format từ Backend

```json
{
  "status": 404,
  "error": "PRODUCT_NOT_FOUND",
  "message": "Không tìm thấy sản phẩm với ID: 999",
  "path": "/products/999",
  "details": { "productId": 999 }
}
```

### 7.2 Validation Error format (400)

```json
{
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Dữ liệu không hợp lệ",
  "path": "/products",
  "details": {
    "name": "Tên sản phẩm không được để trống",
    "basePrice": "Giá bán không được nhỏ hơn 0"
  }
}
```

### 7.3 Quy tắc xử lý ở Frontend

```
400 (Validation)  → Hiển thị lỗi bên dưới input tương ứng (dùng details object)
400 (Bad Request) → Hiển thị toast với message
401 (Unauthorized)→ Xóa token, redirect /login
403 (Forbidden)   → Hiển thị "Bạn không có quyền thực hiện"
404 (Not Found)   → Hiển thị trang 404 hoặc toast
409 (Conflict)    → Hiển thị message (VD: trạng thái xung đột)
422 (Unprocessable)→ Hiển thị message (lỗi cross-module)
500 (Server Error)→ Toast "Đã xảy ra lỗi, vui lòng thử lại sau"
```

---

## 8. QUY TẮC NGHIỆP VỤ QUAN TRỌNG

### 8.1 Product Variant Logic

```
Sản phẩm không phân loại:
  → variants = null/empty khi tạo → Backend sinh 1 variant DEFAULT
  → Frontend: Ẩn phần chọn thuộc tính, hiển thị trực tiếp giá/tồn kho

Sản phẩm có phân loại:
  → variants[0].isMaster = true, các variant sau = REGULAR
  → Frontend: Hiển thị UI chọn thuộc tính từ variant.attributes
  → Khi user chọn xong → tìm variant khớp → hiển thị giá/tồn kho

Thêm vào giỏ:
  → LUÔN gửi cả productId + variantId (kể cả DEFAULT variant)
```

### 8.2 Cart Business Rules

```
- Một user chỉ có 1 cart.
- Cart items có isSelected: chỉ items được chọn mới vào checkout.
- hasPriceChanged = true → cảnh báo user giá đã thay đổi.
- isAvailable = false → disable item, hiển thị "Sản phẩm không khả dụng".
- Cart sync: khi user chưa login, lưu cart vào localStorage.
  Khi login → POST /carts/{userId}/sync để đồng bộ lên server.
- Cart status CHECKING_OUT → khóa giỏ, không thao tác được.
```

### 8.3 Checkout & Payment Flow

```
Luồng thanh toán:
1. Hiển thị cart items (chỉ isSelected=true) + tổng tiền + mã giảm giá
2. Nhập/chọn địa chỉ giao hàng
3. POST /carts/{userId}/checkout { shippingAddress, promoCode }
   → Backend tạo Order + khóa cart
4. POST /payments/checkout { orderId, method: "COD"|"VNPAY", idempotencyKey }

Nếu COD:
  → PaymentStatus = SUCCEEDED → Order auto CONFIRMED
  → Hiển thị trang thành công

Nếu VNPAY:
  → PaymentStatus = PENDING, response có paymentUrl
  → window.location.href = paymentUrl (redirect sang VNPay)
  → VNPay callback → GET /payments/vnpay-return?vnp_...
  → Frontend hiển thị kết quả từ VnpayReturnResponse
```

### 8.4 Order Snapshot Isolation

```
QUAN TRỌNG: Trong OrderItem, dữ liệu sau đã ĐÓNG BĂNG tại thời điểm mua:
  - productName
  - variantAttributesSnapshot
  - price

→ KHÔNG gọi lại API Product để lấy thông tin đơn hàng cũ.
→ Luôn dùng data có sẵn trong OrderResponse.items.
```

### 8.5 Action-Driven UI

```
Backend trả về availableActions / allowedActions trong response.
→ Frontend chỉ hiển thị nút hành động nếu có trong danh sách.
→ Ví dụ: order.availableActions = ["CONFIRM","CANCEL"]
  → Hiển thị 2 nút: "Xác nhận" và "Hủy đơn"
  → KHÔNG hardcode logic "nếu status=PENDING thì hiển thị nút X"
```

### 8.6 Phân trang (Pageable)

```
Backend dùng Spring Data Pageable.
Query params: ?page=0&size=10&sort=createdAt,desc

Response format:
{
  "content": [...],
  "totalPages": 5,
  "totalElements": 48,
  "number": 0,
  "size": 10,
  "first": true,
  "last": false
}
```

---

## 9. QUY TẮC UI/UX

### 9.1 Layout

```
CUSTOMER (Trang mua sắm):
  Header: Logo · Search · Cart icon (badge) · User menu
  Trang chủ: Banner + Sản phẩm nổi bật + Danh mục
  Danh sách SP: Filter sidebar (giá, danh mục, status) + Grid + Phân trang
  Chi tiết SP: Gallery ảnh · Thông tin · Chọn biến thể · Reviews · Thêm giỏ hàng
  Giỏ hàng: Checkbox chọn items · Cập nhật SL · Xóa · Mã giảm giá · Tổng · Thanh toán
  Profile: Thông tin cá nhân · Danh sách địa chỉ · Lịch sử đơn · Đánh giá

ADMIN (Trang quản trị):
  Sidebar: Dashboard · Sản phẩm · Danh mục · Đơn hàng · Thanh toán · User
           Khuyến mãi · Nhà CC · Nhập hàng · Vận chuyển · Đổi trả · Kiểm duyệt
  Content area: Bảng dữ liệu + Filter + Pagination + CRUD modals
```

### 9.2 Component Rules

```
- Mọi bảng (Table): Loading skeleton · Empty state · Phân trang
- Mọi form: Validation errors dưới input · Loading trên nút Submit · Reset sau thành công
- Mọi hành động xóa: Confirm dialog
- Hiển thị giá: format VNĐ (350.000đ)
- Hiển thị thời gian: dd/MM/yyyy HH:mm
- Status badge: Dùng màu + label tiếng Việt
- Nút hành động: Chỉ hiển thị dựa trên availableActions/allowedActions từ API
```

---

## 10. CSS DESIGN SYSTEM

```css
:root {
  /* Colors */
  --color-primary: #6C5CE7;
  --color-primary-light: #A29BFE;
  --color-primary-dark: #5A4BD1;
  --color-bg: #F8F9FA;
  --color-bg-card: #FFFFFF;
  --color-bg-sidebar: #2D3436;
  --color-text: #2D3436;
  --color-text-secondary: #636E72;
  --color-success: #00B894;
  --color-warning: #FDCB6E;
  --color-danger: #E17055;
  --color-info: #74B9FF;

  /* Spacing */
  --spacing-xs: 4px;
  --spacing-sm: 8px;
  --spacing-md: 16px;
  --spacing-lg: 24px;
  --spacing-xl: 32px;

  /* Border & Shadow */
  --border-radius: 8px;
  --border-color: #DFE6E9;
  --shadow-sm: 0 2px 4px rgba(0,0,0,0.05);
  --shadow-md: 0 4px 12px rgba(0,0,0,0.1);

  /* Font */
  --font-family: 'Inter', sans-serif;
  --font-size-sm: 0.875rem;
  --font-size-base: 1rem;
  --font-size-lg: 1.25rem;
  --font-size-xl: 1.5rem;
}
```

**Naming:** BEM hoặc theo module: `.product-card`, `.product-card__image`, `.product-card--sold-out`
**KHÔNG** dùng inline style, `!important`, hoặc CSS framework.

---

## 11. CUSTOM HOOKS

```javascript
useAuth()      → { user, token, login(), logout(), isAuthenticated, isAdmin, isStaff }
useCart()       → { cart, addItem(), updateQty(), removeItem(), toggleSelect(), checkout(), itemCount }
useFetch(url)   → { data, loading, error, refetch() }
useDebounce(val, delay) → debouncedValue
usePagination() → { page, size, sort, setPage(), setSize(), setSort() }
```

---

## 12. HÀM TIỆN ÍCH

```javascript
// src/utils/formatters.js

export const formatPrice = (price) =>
  new Intl.NumberFormat('vi-VN').format(price) + 'đ';

export const formatDate = (dateString) =>
  new Date(dateString).toLocaleString('vi-VN', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  });

export const ORDER_STATUS = {
  PENDING:            { label: 'Chờ xác nhận',      color: '#FDCB6E' },
  CONFIRMED:          { label: 'Đã xác nhận',       color: '#74B9FF' },
  SHIPPED:            { label: 'Đang giao',          color: '#6C5CE7' },
  COMPLETED:          { label: 'Hoàn thành',         color: '#00B894' },
  PARTIALLY_REFUNDED: { label: 'Hoàn trả một phần',  color: '#E17055' },
  FULLY_REFUNDED:     { label: 'Hoàn trả toàn bộ',   color: '#E17055' },
  CANCELLED:          { label: 'Đã hủy',             color: '#B2BEC3' },
};

export const PRODUCT_STATUS = {
  ACTIVE:       { label: 'Đang bán',   color: '#00B894' },
  INACTIVE:     { label: 'Ngừng bán',  color: '#B2BEC3' },
  OUT_OF_STOCK: { label: 'Hết hàng',   color: '#E17055' },
};

export const PAYMENT_STATUS = {
  PENDING:        { label: 'Chờ thanh toán',      color: '#FDCB6E' },
  SUCCEEDED:      { label: 'Thành công',          color: '#00B894' },
  FAILED:         { label: 'Thất bại',            color: '#E17055' },
  CANCELLED:      { label: 'Đã hủy',             color: '#B2BEC3' },
  EXPIRED:        { label: 'Hết hạn',             color: '#B2BEC3' },
  REFUND_PENDING: { label: 'Chờ hoàn tiền',       color: '#FDCB6E' },
  REFUNDED:       { label: 'Đã hoàn tiền',        color: '#74B9FF' },
  REFUND_FAILED:  { label: 'Hoàn tiền thất bại',  color: '#E17055' },
};

export const SHIPMENT_STATUS = {
  PENDING:    { label: 'Chờ lấy hàng',   color: '#FDCB6E' },
  PICKED_UP:  { label: 'Đã lấy hàng',    color: '#74B9FF' },
  IN_TRANSIT: { label: 'Đang vận chuyển', color: '#6C5CE7' },
  DELIVERED:  { label: 'Đã giao',         color: '#00B894' },
  FAILED:     { label: 'Giao thất bại',   color: '#E17055' },
  CANCELLED:  { label: 'Đã hủy',          color: '#B2BEC3' },
  RETURNED:   { label: 'Đã trả hàng',     color: '#E17055' },
};

export const USER_STATUS = {
  PENDING: { label: 'Chờ xác thực', color: '#FDCB6E' },
  ACTIVE:  { label: 'Hoạt động',    color: '#00B894' },
  LOCKED:  { label: 'Bị khóa',      color: '#E17055' },
  DELETED: { label: 'Đã xóa',       color: '#B2BEC3' },
};

export const CATEGORY_STATUS = {
  VISIBLE: { label: 'Hiển thị', color: '#00B894' },
  HIDDEN:  { label: 'Ẩn',      color: '#B2BEC3' },
  DELETED: { label: 'Đã xóa',  color: '#E17055' },
};

export const RETURN_STATUS = {
  REQUESTED:      { label: 'Yêu cầu đổi trả',   color: '#FDCB6E' },
  INFO_REQUIRED:  { label: 'Cần bổ sung',         color: '#74B9FF' },
  APPROVED:       { label: 'Đã duyệt',            color: '#00B894' },
  REJECTED:       { label: 'Từ chối',              color: '#E17055' },
  DISPUTED:       { label: 'Đang tranh chấp',      color: '#E17055' },
  ITEMS_RECEIVED: { label: 'Đã nhận hàng trả',    color: '#6C5CE7' },
  INSPECTED:      { label: 'Đã kiểm tra',          color: '#74B9FF' },
  REFUNDED:       { label: 'Đã hoàn tiền',         color: '#00B894' },
  CANCELLED:      { label: 'Đã hủy',               color: '#B2BEC3' },
};
```

---

## 13. THỨ TỰ PHÁT TRIỂN

```
Phase 1 — Nền tảng (Tuần 1-2):
  □ Setup Vite + React
  □ Cấu trúc thư mục + CSS variables + reset
  □ apiClient.js (fetch wrapper + JWT auto-attach + error handling)
  □ AuthContext + useAuth hook
  □ LoginPage + RegisterPage + VerifyPage
  □ Layout (CustomerLayout, AdminLayout, ProtectedRoute)

Phase 2 — Customer Core (Tuần 3-4):
  □ HomePage — sản phẩm nổi bật
  □ ProductListPage — search, filter giá/category, phân trang
  □ ProductDetailPage — gallery ảnh, chọn variant, reviews
  □ CartPage — CRUD items, toggle select, mã giảm giá
  □ CheckoutPage — chọn địa chỉ + payment method
  □ VNPay Return Page
  □ OrderListPage + OrderDetailPage
  □ ProfilePage — thông tin + địa chỉ

Phase 3 — Admin Core (Tuần 5-6):
  □ Admin Layout (Sidebar)
  □ Dashboard thống kê
  □ Quản lý sản phẩm — CRUD + variants + images + stock
  □ Quản lý danh mục — cây phân cấp + hide/show
  □ Quản lý đơn hàng — filter + chuyển trạng thái
  □ Quản lý users — danh sách + lock/unlock + role

Phase 4 — Nâng cao (Tuần 7+):
  □ Quản lý thanh toán + hoàn tiền
  □ Quản lý khuyến mãi
  □ Quản lý nhà cung cấp + nhập hàng
  □ Quản lý vận chuyển + tracking
  □ Quản lý đổi trả khách hàng
  □ Quản lý trả hàng nhà cung cấp
  □ Kiểm duyệt (blacklisted words)
  □ Kiểm duyệt đánh giá (approve/reject/reply)
```

# NewToyStore

NewToyStore is a full-stack toy-store management and e-commerce system. The repository contains a React/Vite client and a Spring Boot backend organized as a modular monolith.

## Applications

| Application | Responsibility | Documentation |
|---|---|---|
| Frontend | Customer storefront and role-based administration interface | [Frontend README](frontend/README.md) |
| Backend | REST API, business rules, persistence, authentication and integrations | [Backend README](new_toy_store/README.md) |

## Repository structure

```text
NewToyStore/
|- frontend/          React 18 and Vite client
|- new_toy_store/     Java 21 and Spring Boot API
`- README.md          System-level documentation
```

The frontend calls the backend over HTTP. Authentication uses a JWT stored by the client and sent in the `Authorization` header. The backend owns business state in MySQL and integrates with services such as VNPay, Cloudinary and email.

```mermaid
flowchart LR
    Browser["Customer or staff browser"] --> Frontend["React/Vite frontend"]
    Frontend -->|"HTTP + JSON / JWT"| Backend["Spring Boot backend"]
    Backend --> Database["MySQL"]
    Backend --> VNPay["VNPay"]
    Backend --> Cloudinary["Cloudinary"]
    Backend --> Mail["Email provider"]
```

## Main capabilities

- Customer catalog browsing, cart, checkout, payments, orders and shipment tracking.
- Customer profiles, notifications, reviews and return requests.
- Administrative catalog, inventory, suppliers, imports and logistics workflows.
- Administrative payments, refunds, promotions, moderation, users and statistics.

## Live demo

- Frontend: [https://toy-store-wine.vercel.app](https://toy-store-wine.vercel.app)
- Swagger UI: [https://toy-store-api-03ma.onrender.com/swagger-ui.html](https://toy-store-api-03ma.onrender.com/swagger-ui.html)
- Backend API: [https://toy-store-api-03ma.onrender.com](https://toy-store-api-03ma.onrender.com)

The backend uses a Render Free instance, so the first request after a period of inactivity can take approximately 30–60 seconds.

## Demo accounts

| Role | Email | Password | Suggested use |
|---|---|---|---|
| Customer | `customer@gmail.com` | `123456` | Browse products, manage the cart, place orders and review customer features. |
| Staff | `staff@gmail.com` | `123456` | Review operational product, inventory, order and logistics workflows. |
| Manager | `manager@gmail.com` | `123456` | Review management workflows and role-restricted operations. |
| Admin | `admin@gmail.com` | `123456` | Review the complete administration interface and API access. |

These accounts share the deployed demo environment. Data created, updated or deleted during evaluation is visible to other visitors. Do not enter personal, confidential or real payment information. VNPay runs in its sandbox environment.

## Quick start

### Backend

Requirements: JDK 21 and MySQL.

```powershell
cd new_toy_store
.\mvnw.cmd spring-boot:run
```

The default backend address is `http://localhost:8080`. See the [backend setup guide](new_toy_store/README.md#setup-and-run) for supported environment variables and integration configuration.

### Frontend

Requirements: Node.js and npm.

```powershell
cd frontend
npm install
npm run dev
```

Set `VITE_API_URL` when the backend is not available at `http://localhost:8080`. See the [frontend setup guide](frontend/README.md#setup-and-run) for more details.

## Documentation map

- [Frontend architecture and development guide](frontend/README.md)
- [Frontend pages and feature documentation](frontend/README.md#feature-documentation)
- [Backend architecture and development guide](new_toy_store/README.md)
- [Backend domain documentation](new_toy_store/README.md#domain-documentation)

## Verification

```powershell
cd new_toy_store
.\mvnw.cmd test
```

```powershell
cd frontend
npm run build
```

Automated backend and frontend test suites were not detected when these documents were prepared. A successful compile/build therefore verifies integration syntax but does not replace behavioral testing.

## Documentation responsibility

Use this file only for system-wide concerns. Put frontend-specific information in `frontend/README.md`, backend-specific information in `new_toy_store/README.md`, and feature details in the README closest to the relevant source code.

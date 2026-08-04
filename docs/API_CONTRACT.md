# EaaS Frontend API Contract

Base URL (development): `http://172.30.32.1:8080` (API Gateway)

**Rule: always call the gateway. Never call service ports (8081-8084) directly** —
downstream services only trust requests signed by the gateway
(`X-Internal-Api-Key` HMAC, verified per service by `GatewayRequestValidationFilter`).

All responses are wrapped in `ApiResponse<T>`:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": { },
  "timestamp": "2026-08-04T10:00:00.000000000Z"
}
```

## Authentication

### Login

`POST /api/v1/auth/login` (generic, auto-detects role)

Variants: `POST /api/v1/auth/login/customer`, `POST /api/v1/auth/login/merchant`,
`POST /api/v1/auth/login/admin`

Request body:

```json
{ "email": "user@example.com", "password": "secret" }
```

Response `data`:

```json
{
  "accessToken": "eyJ...",
  "refreshToken": "eyJ...",
  "expiresIn": 3600000,
  "tokenType": "Bearer",
  "user": {
    "id": "68cc8562-9171-49e1-9b24-4084d3b3f84f",
    "email": "user@example.com",
    "firstName": "...",
    "lastName": "...",
    "role": "CUSTOMER"
  }
}
```

### Refreshing tokens

`POST /api/v1/auth/refresh` (body: `{ "refreshToken": "..." }`) returns a new
`AuthResponse`. Access token TTL is 1h, refresh token 7d by default
(`JWT_EXPIRATION`, `JWT_REFRESH_EXPIRATION`).

### Current user

`GET /api/v1/auth/me` (Bearer token required) → `data: UserResponse`.

## Calling protected endpoints

```http
Authorization: Bearer <accessToken>
Content-Type: application/json
```

| Header | Where | Purpose |
|---|---|---|
| `Authorization: Bearer <jwt>` | all `/api/v1/**` (except auth/webhooks/health) | customer, admin, and JWT-based merchant auth |
| `X-API-Key` | alternative for merchants | merchant API key (`GET /api/v1/merchants/api-keys`) |

401 handling: `GET /api/v1/auth/refresh` with the refresh token, then retry the
original request once. On refresh failure, force re-login.

## CORS

Enabled on the gateway for all origins/methods/headers with credentials
(`spring.cloud.gateway.globalcors`, dev `allowedOriginPatterns: "*"`). Preflight
`OPTIONS` is handled before authentication, so browser calls work directly.
Tighten `allowedOriginPatterns` to the production origin before release.

## Endpoint reference

Full request/response examples: `eaas-postman-collection.json` (grouped by
feature). Health checks: `GET /api/v1/health`, `GET /api/v1/escrow/health`,
`GET /api/v1/payments/health`, `GET /api/v1/disputes/health`,
`GET /api/v1/notifications/health`, `GET /actuator/health`.

| Area | Base path | Notes |
|---|---|---|
| Auth | `/api/v1/auth/**` | public |
| Terms / privacy | `/api/v1/terms-of-service`, `/api/v1/privacy-policy` | public |
| GDPR | `/api/v1/users/me/*` | data-export, delete, marketing-consent |
| Escrow | `/api/v1/escrow/**` | create, list, get, ship, deliver, confirm, cancel |
| Payments | `/api/v1/payments/**` | initialize, verify, list, refund (admin) |
| Payouts | `/api/v1/payouts/**` | create (merchant withdraw), list, by-escrow |
| Disputes | `/api/v1/disputes/**` | create, list, messages, evidence, mark-read |
| Notifications | `/api/v1/notifications/**` | list, unread-count, mark read/read-all |
| Merchant | `/api/v1/merchants/**` | profile, KYC submit/upload, webhook config, api-keys |
| Admin | `/api/v1/admin/**` | kyc, escrows, payments, payouts, disputes, aml-alerts, fee-configurations |
| PSP webhooks | `/api/v1/webhooks/**` | public; dev simulation for Interswitch payments/payouts |

## Known behavior to design around

- **Notifications** are the source of truth in the UI (`GET /api/v1/notifications`).
  Email/SMS sending is not yet wired to a real provider (SendGrid key is a dev
  placeholder), so notification records may exist with status `FAILED` while the
  system continues to work.
- **No real-time events yet** — the webhook engine exists but delivery is not
  wired to the frontend. Poll notifications/escrow/dispute endpoints.
- **Idempotent operations** — creating a dispute on an already-disputed escrow
  and re-resolving an already-resolved dispute return the current state (200)
  instead of erroring.

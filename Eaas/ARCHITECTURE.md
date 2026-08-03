# EaaS — System Architecture Document
# Version 1.0.0 | June 2026

---

## 1. High-Level Architecture

```
┌────────────────────────────────────────────────────────────────────┐
│                          CLIENT LAYER                              │
│                                                                    │
│   [Merchant Dashboard]   [Customer Web App]   [Partner API Client] │
│       (React SPA)           (React SPA)         (3rd party apps)  │
└──────────────────────────────┬─────────────────────────────────────┘
                                │ HTTPS
                                ↓
┌────────────────────────────────────────────────────────────────────┐
│                         API GATEWAY                                │
│        Rate Limiting | JWT Auth | API Key Auth | SSL Term          │
│              Routing | Request Logging | CORS                      │
└───┬──────────────────┬──────────────────┬──────────────────────────┘
     │                  │                  │
     ↓                  ↓                  ↓
┌─────────────────┐ ┌──────────────────┐ ┌──────────────────────────┐
│    Identity     │ │     Escrow &     │ │    Payment Engine        │
│    & Merchant   │ │     Workflow     │ │    (Treasury)            │
│    Service      │ │    Service       │ │    Service               │
│    (Port 8081)  │ │    (Port 8082)   │ │    (Port 8083)           │
├─────────────────┤ ├──────────────────┤ ├──────────────────────────┤
│ • JWT Auth      │ │ • State Machine  │ │ • Interswitch Payment    │
│ • KYC           │ │ • Fee Calc       │ │ • Interswitch Webhook    │
│ • API Keys      │ │ • Scheduler      │ │ • Merchant Payout        │
│ • Merchant Mgmt │ │ • Idempotency    │ │ • Customer Refund        │
│ • Audit Logs    │ │ • Auto-Release   │ │ • Retry Logic            │
└─────────────────┘ └──────────────────┘ └──────────────────────────┘
       │                     │                     │
       └─────────────────────┴─────────────────────┘
                               │
                               ↓
               ┌────────────────────────┐
               │   RabbitMQ Event Bus   │
               │   (Topic Exchange:     │
               │    escrow.events)      │
               └───────────┬────────────┘
                           │
                           ↓
              ┌──────────────────────────┐
              │   Communication &        │
              │   Dispute Service        │
              │   (Port 8084)            │
              ├──────────────────────────┤
               │ • Email (SendGrid)       │
               │ • SMS (Termii)           │
               │ • Webhook Delivery       │
               │ • Dispute Management     │
               │ • Evidence Cloudinary    │
              └──────────────────────────┘

┌────────────────────────────────────────────────────────────────────┐
│                         DATA LAYER                                 │
│                                                                    │
│  ┌──────────────┐  ┌──────────────┐  ┌─────────────────────────┐ │
│  │   MSSQL      │  │    Redis     │  │   Cloudinary            │ │
│  │  (Per-service│  │  (Cache +    │  │   (KYC docs, dispute    │ │
│  │   databases) │  │  Sessions +  │  │    evidence, receipts)  │ │
│  └──────────────┘  └──────────────┘  └─────────────────────────┘ │
└────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────┐
│                      CROSS-CUTTING CONCERNS                        │
│                                                                    │
│  [Metrics / Prometheus]  [Grafana dashboards]                     │
└────────────────────────────────────────────────────────────────────┘
```

---

## 2. Microservices Breakdown (6 Services)

### 2.1 API Gateway Service
**Port:** 8080
**Responsibilities:**
- Rate limiting (100 req/min per API key)
- JWT and API key authentication
- SSL termination and CORS
- Request routing to downstream services
- IP-based brute force protection

---

### 2.2 Identity & Merchant Service
**Port:** 8081
**Database:** MSSQL (identity_db) — users, merchant_profiles, audit_logs, refresh_tokens
**Responsibilities:**
- User registration (customer + merchant)
- JWT issuance and refresh token rotation
- API key generation and validation (SHA-256 identifier + BCrypt hash)
- KYC document upload and review (stored in Cloudinary)
- Email verification
- GDPR/NDPR consent tracking and data export
- Immutable audit logging via RabbitMQ `audit.event`

**Key endpoints:**
- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/refresh`
- `POST /merchants/kyc`
- `POST /merchants/api-key/regenerate`
- `PUT /admin/kyc/{merchantId}/approve`
- `PUT /admin/kyc/{merchantId}/reject`

---

### 2.3 Escrow & Workflow Service
**Port:** 8082
**Database:** MSSQL (escrow_db) — escrow_transactions, escrow_state_history, idempotency_records
**Cache:** Redis (idempotency keys, hot escrow reads)
**Responsibilities:**
- Escrow creation and reference generation
- State machine management
- Fee lookup from `fee_configurations` table (per-merchant, not hardcoded)
- Idempotency enforcement
- Event publishing to RabbitMQ
- Auto-release scheduler (runs every 5 minutes)
- Confirmation window management (72 hours)

**Key endpoints:**
- `POST /escrow`
- `GET /escrow/{reference}`
- `POST /escrow/{reference}/ship`
- `POST /escrow/{reference}/deliver`
- `POST /escrow/{reference}/confirm`
- `POST /escrow/{reference}/cancel`

---

### 2.4 Payment Engine Service
**Port:** 8083
**Database:** MSSQL (payment_db) — payouts, payment_transactions, aml_alerts, aml_cases, aml_case_notes
**Responsibilities:**
- Generate Interswitch payment links
- Receive and verify Interswitch webhooks (HMAC-SHA512)
- Update escrow to FUNDED on successful payment
- Merchant payouts via Interswitch Transfer API
- Customer refunds via Interswitch Refund API
- Retry logic with exponential backoff
- Payout status tracking
- AML transaction monitoring and alert generation

**Key endpoints:**
- `POST /webhooks/interswitch/payments` (Interswitch webhook)
- `GET /payouts`
- `GET /payouts/{id}`

---

### 2.5 Notification Service
**Port:** 8084
**Database:** MSSQL (notification_db) — notifications
**Responsibilities:**
- Email delivery via SendGrid
- SMS delivery via Termii
- In-app notification queueing

**Consumes RabbitMQ events:**
- All `escrow.*` events

---

### 2.6 Communication & Dispute Service
**Port:** 8085
**Database:** MSSQL (communication_db) — disputes, dispute_evidence, webhook_events, webhook_delivery_attempts
**Storage:** Cloudinary (evidence files)
**Responsibilities:**
- Dispute creation and management
- Evidence upload (Cloudinary)
- Merchant response handling
- Admin dispute resolution
- Merchant webhook delivery with TTL+DLX retry and HMAC signing

**Key endpoints:**
- `GET /disputes/{id}`
- `POST /disputes/{id}/evidence`
- `POST /disputes/{id}/merchant-response`
- `PUT /admin/disputes/{id}/resolve`

**Consumes RabbitMQ events:**
- All `escrow.*` events

---

## 2.7 Service Summary

| Service | Port | Database | Key Technology |
|---|---|---|---|
| API Gateway | 8080 | None | Spring Cloud Gateway |
| Identity & Merchant | 8081 | identity_db | Spring Boot, Cloudinary |
| Escrow & Workflow | 8082 | escrow_db | Spring Boot, Redis |
| Payment Engine | 8083 | payment_db | Spring Boot, Interswitch |
| Notification | 8084 | notification_db | Spring Boot, SendGrid, Termii |
| Communication & Dispute | 8085 | communication_db | Spring Boot, Cloudinary, RabbitMQ DLX |

## 3. RabbitMQ Event Architecture

```
Exchange: escrow.events (type: topic)

Routing Key              | Published By              | Consumed By
────────────────────────────────────────────────────────────────────────────
escrow.initiated         | Escrow & Workflow         | Communication
escrow.funded            | Payment Engine            | Communication, Escrow
escrow.merchant.notified | Escrow & Workflow         | Communication
escrow.shipped           | Escrow & Workflow         | Communication
escrow.delivered         | Escrow & Workflow         | Communication
escrow.confirmed         | Escrow & Workflow         | Payment Engine, Communication
escrow.disputed          | Escrow & Workflow         | Communication
escrow.auto_released     | Escrow & Workflow         | Payment Engine, Communication
escrow.released          | Payment Engine            | Communication
escrow.resolved.merchant | Communication & Dispute   | Payment Engine, Communication
escrow.resolved.customer | Communication & Dispute   | Payment Engine, Communication
escrow.refunded          | Payment Engine            | Communication
escrow.cancelled         | Escrow & Workflow         | Communication
```

### Dead Letter Queue (DLQ) & Webhook Retry
Every queue has a corresponding DLQ: `escrow.events.dlq`. Messages failing after max retries go to DLQ and are monitored via Prometheus/Grafana.

Merchant webhook delivery uses RabbitMQ TTL+DLX retry with 5 intervals:
1. 1 minute
2. 5 minutes
3. 30 minutes
4. 2 hours
5. 24 hours → alert admin, manual review

---

## 4. State Machine

```
INITIATED ──────────────────────────────────────────────────────→ CANCELLED
    │
    ↓ (Interswitch webhook payment confirmed)
FUNDED ──────────────────────────────────────────────────────────→ CANCELLED
    │
    ↓ (system auto-notifies merchant)
MERCHANT_NOTIFIED
    │
    ↓ (merchant submits tracking)
SHIPPED
    │
    ↓ (merchant/logistics marks delivered)
DELIVERED
    │
    ├──→ CONFIRMED (customer confirms within 72hrs)
    │         │
    │         ↓
    │       RELEASED ──→ [Payout to Merchant]
    │
    ├──→ AUTO_RELEASED (72hr timer expires, no response)
    │         │
    │         ↓
    │      [Payout to Merchant]
    │
    └──→ DISPUTED (customer raises dispute)
              │
              ↓
          UNDER_REVIEW (admin takes over)
              │
              ├──→ RESOLVED_MERCHANT ──→ RELEASED ──→ [Payout to Merchant]
              │
              └──→ RESOLVED_CUSTOMER ──→ REFUNDED ──→ [Refund to Customer]
```

### Valid Transitions Map
```
From State           | Allowed Next States
─────────────────────────────────────────────────────────
INITIATED            | FUNDED, CANCELLED
FUNDED               | MERCHANT_NOTIFIED, CANCELLED
MERCHANT_NOTIFIED    | SHIPPED
SHIPPED              | DELIVERED
DELIVERED            | CONFIRMED, DISPUTED, AUTO_RELEASED
CONFIRMED            | RELEASED
DISPUTED             | UNDER_REVIEW
UNDER_REVIEW         | RESOLVED_MERCHANT, RESOLVED_CUSTOMER
AUTO_RELEASED        | (terminal → triggers payout)
RELEASED             | (terminal)
RESOLVED_MERCHANT    | RELEASED
RESOLVED_CUSTOMER    | REFUNDED
REFUNDED             | (terminal)
CANCELLED            | (terminal)
```

---

## 5. Security Architecture

```
Layer               | Control
────────────────────────────────────────────────────────────────
API Gateway         | Rate limit: 100 req/min per API key
                    | JWT validation (RS256)
                    | API key identifier hashing (SHA-256)
                    | IP-based brute force protection
                    | TLS 1.2+ only

Service Layer       | @PreAuthorize role-based access
                    | Ownership validation (your escrow only)
                    | Idempotency key enforcement
                    | Input validation (@Valid, bean validation)

Webhook Security    | Interswitch: HMAC-SHA512 signature check
                    | Merchant webhooks: signed with webhook_secret

Data Layer          | AES-256-GCM via AesGcmEncryptionService
                    | Encryption key: ENCRYPTION_MASTER_KEY env var
                    | PII fields (email, phone, bank_account, BVN) encrypted
                    | Audit log: append-only (no UPDATE/DELETE)
                    | Audit diffs only via DiffAuditHelper (no full snapshots)
                    | Soft deletes only (is_active = false)
                    | Database connection pooling (HikariCP)

Transport           | TLS 1.2+ everywhere
                    | Internal mTLS between services (future v2)
                    | Secrets via environment variables
```

---

## 6. Audit Logging

Audit events are published to RabbitMQ exchange `audit.event` rather than stored directly by a dedicated audit service.

- **Publisher:** Identity & Merchant Service (and others as needed)
- **Consumer:** Any service requiring audit trail aggregation
- **Format:** Diff-only using `DiffAuditHelper` (old_values vs new_values delta)
- **Immutability:** Once written, audit records cannot be updated or deleted

---

## 7. Compliance

### GDPR / NDPR
- Consent fields tracked on the `users` entity (marketing_consent, data_processing_consent)
- Right to data export: admin endpoint generates JSON dump of user data
- Right to erasure: user data anonymized (retaining transaction records for regulatory purposes)
- PII masking in logs via `PiiMaskingUtils`

### AML
- Payment Engine monitors transactions and generates `AML_ALERT` events
- `aml_alerts`, `aml_cases`, `aml_case_notes` tables track investigation workflow
- Suspicious Activity Report (SAR) filing supported via admin panel

---

## 8. Deployment Architecture

> **Note:** Kubernetes and Docker Swarm are not implemented in the current version.
> Services run as standalone JVM processes or within Docker containers managed by the host.

---

## 9. Caching Strategy

| Data | Cache Key | TTL | Invalidation |
|---|---|---|---|
| Escrow by reference | `escrow:{reference}` | 5 min | On state change |
| User by ID | `user:{id}` | 15 min | On profile update |
| Fee config | `fee:default` | 1 hour | On admin update |
| Escrow list (paged) | `escrows:merchant:{id}` | 2 min | On new escrow |
| Dispute summary | `dispute:{id}:summary` | 5 min | On status change |
| Merchant profile | `merchant:{id}` | 10 min | On KYC change |
| Idempotency key | `idem:{key}` | 24 hours | TTL-based expiry |
| JWT blacklist | `jwt:blacklist:{jti}` | Token TTL | TTL-based expiry |

---

## 10. Fee Calculation Logic

Fees are configured per-merchant in the `fee_configurations` database table, not hardcoded in `application.yml`.

```
Given:
  amount = ₦850,000
  fee_type = PERCENTAGE
  fee_value = 1.5%
  min_fee = ₦500
  max_fee = ₦50,000

Calculation:
  raw_fee = 850,000 × 1.5 / 100 = ₦12,750
  capped_fee = max(500, min(12,750, 50,000)) = ₦12,750

Result:
  escrow_fee = ₦12,750         (EaaS revenue, paid by merchant)
  merchant_amount = ₦837,250   (merchant receives)
  customer pays = ₦850,000     (the escrow amount — fee is on the merchant side)
```

---

## 11. Payout Flow (Payment Engine Service)

```
Trigger: escrow.confirmed event received

Payment Engine Service:
  1. Read escrow transaction (merchant_amount, bank_account, bank_code)
  2. Decrypt bank account number
  3. Create payout record (status=PENDING)
  4. Call Interswitch Transfer API:
       POST https://sandbox.interswitchng.com/api/v3/purchases
       { amount: merchant_amount,
         currency: "NGN",
         destination: bank_account,
         narration: reference }
  5. On success → payout status = SUCCESS, completed_at = now()
  6. On failure → payout status = FAILED, schedule retry
  7. Publish payout.completed or payout.failed event
  8. Communication Service sends email to merchant

Retry Schedule (on failure):
  Attempt 1: immediate
  Attempt 2: +1 minute
  Attempt 3: +5 minutes
  Attempt 4: +30 minutes
  Attempt 5: +2 hours
  Attempt 6: +24 hours → alert admin, manual review
```

---

## 12. Technology Stack

| Layer | Technology | Justification |
|---|---|---|
| Backend Services | Spring Boot 3.x (Java 21) | Production-grade, ecosystem maturity |
| API Gateway | Spring Cloud Gateway | Native Spring integration |
| Database | MSSQL 2019+ | ACID compliance, JSON support |
| Cache | Redis 7 | Fast idempotency + sessions |
| Message Broker | RabbitMQ 3.12 | Reliable async messaging, TTL+DLX retries |
| Object Storage | Cloudinary | Scalable file storage with transformation |
| Container Orchestration | Docker (host-managed) | Current deployment model |
| Email | SendGrid | Deliverability + templates |
| SMS | Termii | Nigeria-native SMS provider |
| PSP | Interswitch | Nigeria-first, CBN-licensed, enterprise-grade |
| Monitoring | Prometheus + Grafana | Metrics + dashboards |
| Logging | ELK Stack | Centralized log aggregation |
| Secrets | Environment variables | 12-factor app compliance |
| CI/CD | GitHub Actions | Automated build + deploy |
| TLS | Let's Encrypt / provider certs | Certificate management |

---

## Appendix: Key Utilities & Services

| Component | Purpose |
|---|---|
| `PiiMaskingUtils` | Masks PII in logs (e.g., `j***@example.com`) |
| `EncryptedStringConverter` | JPA attribute converter for transparent AES-256-GCM encryption |
| `RefreshToken` rotation | Short-lived refresh tokens rotated on every use |
| `WebhookSecretHashingService` | Hashes webhook secrets for secure storage and verification |

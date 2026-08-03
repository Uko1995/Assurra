# Product Requirements Document (PRD)
# Escrow as a Service (EaaS)

**Version:** 1.0.0
**Status:** Draft
**Date:** June 2026
**Author:** Uko Uwatt

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [Problem Statement](#problem-statement)
3. [Goals & Success Metrics](#goals--success-metrics)
4. [Target Users](#target-users)
5. [Product Scope](#product-scope)
6. [Feature Requirements](#feature-requirements)
7. [Non-Functional Requirements](#non-functional-requirements)
8. [Out of Scope (v1)](#out-of-scope-v1)
9. [Risks & Mitigations](#risks--mitigations)
10. [Timeline](#timeline)

---

## Executive Summary

EaaS (Escrow as a Service) is an API-first, Africa-native escrow platform that enables secure transactions between buyers (customers) and sellers (merchants). Funds are held in a neutral escrow account until the customer confirms receipt and satisfaction with the product, at which point funds are released to the merchant.

EaaS is designed as a white-label, embeddable service that any marketplace, e-commerce platform, or independent merchant can integrate with minimal effort.

---

## Problem Statement

E-commerce trust remains a critical barrier in African markets:

- **Customer risk:** Customers pay upfront but receive wrong, damaged, or no products.
- **Merchant risk:** Merchants ship products but customers refuse to pay or initiate fraudulent chargebacks.
- **Platform risk:** Marketplaces bear reputational and financial damage from buyer-seller disputes.

Existing solutions are either too expensive (enterprise escrow), not Africa-native (Escrow.com is USD-only), or closed ecosystems (Jumia/Konga hold funds only within their platforms).

**EaaS bridges this gap** by offering a developer-friendly, naira-native escrow engine accessible via API.

---

## Goals & Success Metrics

### Business Goals
| Goal | Metric | Target (6 months) |
|---|---|---|
| Merchant adoption | Registered verified merchants | 500 |
| Transaction volume | Monthly escrow transactions | 5,000 |
| GMV | Monthly Gross Merchandise Value | ₦500M |
| Dispute rate | % of transactions disputed | < 5% |
| Resolution time | Avg dispute resolution time | < 48 hours |
| Auto-release rate | % resolved without dispute | > 85% |

### Technical Goals
| Goal | Metric | Target |
|---|---|---|
| API availability | Uptime | 99.9% |
| API latency | p95 response time | < 300ms |
| Payment success rate | Interswitch success rate | > 98% |
| Payout SLA | Time from confirm to merchant payment | < 24 hours |

---

## Target Users

### Primary Users

**1. Merchants (Sellers)**
- SME business owners selling physical goods
- Freelancers delivering digital services
- Suppliers in B2B procurement chains
- Pain: customers distrust them; chargebacks hurt revenue

**2. Customers (Buyers)**
- Individual consumers buying online
- Business procurement teams
- Pain: merchants take money and disappear; product quality not guaranteed

### Secondary Users

**3. Platform Partners (API Integrators)**
- E-commerce marketplaces wanting escrow protection
- Logistics companies wanting payment-on-delivery integration
- Financial institutions wanting escrow as a product

**4. EaaS Admins**
- Internal team managing disputes
- Compliance officers reviewing KYC
- Finance team managing reconciliation

---

## Product Scope

### MVP Scope (v1)

```
✅ Merchant onboarding & KYC (lite)
✅ Customer registration
✅ Escrow transaction creation
✅ Payment via Interswitch (card + bank transfer)
✅ Interswitch webhook processing
✅ Merchant notification (email + SMS)
✅ Tracking number submission by merchant
✅ Customer delivery confirmation
✅ 72-hour auto-release timer
✅ Basic dispute filing
✅ Human-reviewed dispute resolution (admin)
✅ Merchant payout via bank transfer
✅ Customer refund processing
✅ Merchant dashboard (web)
✅ Customer transaction view (web)
✅ Admin panel (disputes + payouts)
✅ REST API + webhook for integrators
✅ Audit log (immutable)
```

---

## Feature Requirements

### FR-001: Merchant Onboarding
- Merchant must provide: business name, email, phone, bank account, BVN
- KYC documents uploaded to Cloudinary; URLs stored securely
- KYC review by admin within 24 hours
- Merchant receives API key and webhook URL configuration option upon approval
- KYC documents retained for 5 years per regulatory requirements

### FR-002: Escrow Creation
- Customer initiates escrow with: merchant ID, product description, amount, currency
- System calculates escrow fee and returns total charge to customer
- Idempotency key required to prevent duplicate transactions
- Escrow reference generated (format: TXN-YYYY-XXXXXXXX)

### FR-003: Payment Collection
- Payment link generated via Interswitch
- Supported channels: card, bank transfer, USSD
- Interswitch webhook confirms payment → escrow moves to FUNDED state
- Webhook signature must be verified (HMAC-SHA512)

### FR-004: Merchant Notification
- Upon FUNDED state: notify merchant via email + SMS + webhook (if configured)
- Notification includes: product details, amount held, customer info, shipping deadline

### FR-005: Shipment Tracking
- Merchant submits tracking number and logistics provider
- Customer notified with tracking details
- Escrow moves to SHIPPED state

### FR-006: Delivery Confirmation
- Merchant or logistics webhook marks as DELIVERED
- Customer receives notification to confirm or dispute within 72 hours
- Confirmation deadline stored on transaction

### FR-007: Customer Confirmation
- Customer confirms receipt → escrow moves to CONFIRMED → payout triggered
- Customer raises dispute → escrow moves to DISPUTED → dispute process opens

### FR-008: Auto-Release
- Scheduled job checks every 5 minutes for transactions past confirmation deadline
- If no response in 72 hours → AUTO_RELEASED → payout triggered to merchant
- Customer notified that funds were auto-released

### FR-009: Dispute Management
- Customer selects dispute reason: ITEM_NOT_RECEIVED | ITEM_NOT_AS_DESCRIBED | ITEM_DAMAGED | WRONG_ITEM | OTHER
- Both parties can upload evidence (images, videos, documents — max 10MB each)
- Admin reviews and resolves within 48 hours
- Resolution: funds to merchant OR refund to customer

### FR-010: Payout & Refund
- Payout to merchant: bank transfer via Interswitch Transfer API
- Refund to customer: reverse to original payment channel via Interswitch Refund API
- Payout SLA: within 24 hours of release trigger
- Payout status tracked and notified

### FR-011: Fee Management
- Default fee: 1.5% of transaction amount
- Minimum fee: ₦500
- Maximum fee: ₦50,000
- Per-merchant custom fee configuration by admin
- Fee always charged to customer on top of product amount

### FR-012: Audit Log
- Every state change recorded immutably
- Fields: escrow ID, from_status, to_status, triggered_by, timestamp, metadata
- Audit events published to RabbitMQ `audit.event` as diffs only (DiffAuditHelper)
- Accessible by admin only
- Cannot be deleted or modified

### FR-013: AML Monitoring
- Payment Engine monitors all transactions for suspicious patterns
- Automatic `AML_ALERT` generation for transactions > ₦5,000,000 or anomalous behavior
- Alerts escalated to `AML_CASES` with investigation workflow
- Compliance officers can add `AML_CASE_NOTES`
- Suspicious Activity Report (SAR) filing supported via admin panel

---

## Non-Functional Requirements

### Security
- All API endpoints protected by JWT or API key
- API key identifiers hashed with SHA-256; secrets stored with BCrypt (cost 10)
- Webhook signatures verified (HMAC-SHA512)
- Bank account numbers encrypted at rest (AES-256-GCM via AesGcmEncryptionService)
- BVN data encrypted at rest (AES-256-GCM)
- Refresh tokens rotated on every use and bound to device fingerprint
- TLS 1.2+ on all endpoints
- Rate limiting: 100 requests/minute per API key
- PCI-DSS compliance delegated to Interswitch

### Performance
- API p95 latency: < 300ms
- Interswitch webhook processing: < 2 seconds
- Payout processing: < 24 hours
- System uptime: 99.9% (< 8.7 hours downtime/year)

### Scalability
- Stateless services — horizontally scalable
- Redis for caching and idempotency
- RabbitMQ event bus for async processing

### Compliance
- KYC required for all merchants before going live
- AML screening on transactions > ₦5,000,000
- Transaction records retained for 7 years (CBN requirement)
- GDPR/NDPR: consent tracking, right to data export, right to erasure (anonymization)
- PII masked in logs via PiiMaskingUtils
- CBN reporting compliance via licensed Interswitch partner

---

## Out of Scope (v1)

| Feature | Reason | Version |
|---|---|---|
| Mobile app (iOS/Android) | API-first approach first | v2 |
| Multi-currency (USD, GBP) | Regulatory complexity | v2 |
| AI dispute resolution | Requires training data | v3 |
| Logistics integration | Partnership required | v2 |
| Crypto payments | Regulatory risk | v3 |
| Subscription escrow | New use case | v2 |
| In-app messaging | Scope creep | v2 |

---

## Risks & Mitigations

| Risk | Probability | Impact | Mitigation |
|---|---|---|---|
| CBN licensing delay | High | Critical | Partner with licensed Interswitch to hold funds |
| Merchant fraud (ship empty box) | Medium | High | KYC verification + dispute process + merchant rating |
| Customer abuse (never confirms) | Medium | High | 72-hour auto-release timer |
| Interswitch downtime | Low | High | Secondary payment fallback (Flutterwave) |
| Database corruption | Low | Critical | Daily backups + point-in-time recovery |
| Chargeback fraud | Medium | High | Hold funds 7 days post-release before settlement |
| Key person risk | Medium | Medium | Documentation + team cross-training |

---

## Timeline

### Phase 1 — Foundation (Weeks 1–4)
- Auth Service (JWT, API keys, KYC)
- Database schema + migrations
- Escrow Service (state machine, core CRUD)
- Payment Service (Interswitch integration)

### Phase 2 — Core Flow (Weeks 5–8)
- Notification Service (email + SMS)
- Payout Service (merchant payout + refund)
- Scheduler (auto-release timer)
- Audit Service

### Phase 3 — Dispute & Admin (Weeks 9–11)
- Dispute Service
- Admin Panel (disputes, payouts, KYC)
- Evidence upload (Cloudinary)

### Phase 4 — Dashboard & API Docs (Weeks 12–14)
- Merchant Dashboard
- Customer Transaction View
- Developer API Documentation
- Postman Collection

### Phase 5 — Hardening & Launch (Weeks 15–16)
- Load testing (k6)
- Security audit
- Beta merchant onboarding (10 merchants)
- Production deployment (Docker)

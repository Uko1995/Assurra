# EaaS — Product Requirements Document
# Index & Glossary

---

## 📁 Document Structure

```
eaas-prd/
│
├── README.md                          ← You are here
├── PRD.md                             ← Main Product Requirements Document
├── NON_FUNCTIONAL_REQUIREMENTS.md     ← Performance, security, SLA specs
│
├── user-stories/
│   └── USER_STORIES.md                ← All epics, user stories & acceptance criteria
│
├── diagrams/
│   ├── ERD.mermaid                    ← Entity Relationship Diagram (all tables)
│   ├── STATE_MACHINE.mermaid          ← Escrow status state machine
│   ├── SEQUENCE_HAPPY_PATH.mermaid    ← Happy path sequence diagram
│   ├── SEQUENCE_DISPUTE_FLOW.mermaid  ← Dispute resolution sequence diagram
│   └── schema.sql                     ← Full MSSQL database schema
│
├── architecture/
│   └── ARCHITECTURE.md               ← HLA, LLA, microservices, K8s, event map
│
└── api-specs/
    └── openapi.yaml                   ← Full OpenAPI 3.0 specification
```

---

## 🔍 How to View Diagrams

The `.mermaid` files can be rendered at:
- **https://mermaid.live** — paste file contents
- **VS Code** — install "Mermaid Preview" extension
- **GitHub** — renders Mermaid in markdown automatically

The `openapi.yaml` can be viewed at:
- **https://editor.swagger.io** — paste file contents

---

## 📖 Glossary

| Term | Definition |
|---|---|
| **EaaS** | Escrow as a Service — the product name |
| **Escrow** | A financial arrangement where a neutral third party holds funds until conditions are met |
| **Merchant** | The seller / business receiving payment through EaaS |
| **Customer** | The buyer making payment through EaaS |
| **Partner** | A third-party platform integrating EaaS via API |
| **PSP** | Payment Service Provider (Interswitch in v1) |
| **KYC** | Know Your Customer — identity verification process |
| **AML** | Anti-Money Laundering — regulatory compliance requirement |
| **BVN** | Bank Verification Number — Nigerian bank identity number |
| **CAC** | Corporate Affairs Commission — Nigerian business registration body |
| **CBN** | Central Bank of Nigeria — financial regulator |
| **Idempotency Key** | A unique key preventing duplicate processing of the same request |
| **State Machine** | A formal model of an escrow transaction's lifecycle states and transitions |
| **HPA** | Horizontal Pod Autoscaler — Kubernetes auto-scaling mechanism |
| **DLQ** | Dead Letter Queue — RabbitMQ queue for failed messages |
| **HMAC** | Hash-based Message Authentication Code — used to verify webhook authenticity |
| **Confirmation Window** | 72-hour period after delivery during which customer can confirm or dispute |
| **Auto-Release** | Automatic fund release to merchant after 72-hour confirmation window expires |
| **Payout** | Transfer of escrow funds to merchant's bank account |
| **Refund** | Return of escrow funds to customer's payment method |
| **Dispute** | A formal claim by the customer that the product was not received or not as described |
| **Arbitration** | Admin review and resolution of a dispute |
| **Escrow Fee** | EaaS platform fee charged per transaction (default 1.5%) |
| **Merchant Amount** | Amount merchant receives = escrow amount minus escrow fee |
| **Total Charge** | Amount customer pays = product amount plus escrow fee |
| **Reference** | Unique escrow transaction identifier (format: TXN-YYYY-XXXXXXXX) |
| **Webhook** | HTTP callback sent to merchant's URL on escrow state changes |
| **Sandbox** | Test environment for merchant integration (no real money) |

---

## 🔗 Key Business Rules Summary

1. **Merchant must be KYC-verified** before receiving any payouts.
2. **Idempotency key is required** on all escrow creation requests.
3. **Customer has 72 hours** after delivery to confirm or dispute.
4. **Funds auto-release to merchant** if customer does not respond in 72 hours.
5. **Disputes freeze payout** — no auto-release while a dispute is open.
6. **Escrow fee is non-refundable** unless EaaS is at fault.
7. **Minimum transaction amount** is ₦1,000.
8. **Only one active dispute** per escrow transaction at a time.
9. **Dispute must be raised before** the 72-hour window expires.
10. **Admin resolution is final** and binding for both parties.
11. **Cancelled escrows** (unfunded for 24 hours) incur no fee.
12. **Merchant receives net amount** (after fee deduction) — not the gross.

---

## 👥 Stakeholder Map

| Stakeholder | Role | Key Interest |
|---|---|---|
| Product Manager | Owns PRD | Feature scope, user value |
| Engineering Lead | Technical design | Feasibility, architecture |
| Backend Engineers | Implementation | API design, data model |
| Frontend Engineers | Dashboard & customer UI | API contracts |
| DevOps Engineer | Deployment | K8s, CI/CD, monitoring |
| Legal / Compliance | Regulatory | KYC, AML, CBN rules |
| Finance Team | Reconciliation | Payout accuracy, fees |
| Customer Support | Dispute handling | Admin tools, escalation |
| Merchants (external) | Revenue | Reliable payouts, low fees |
| Customers (external) | Purchase protection | Trust, refund speed |

---

## 📅 Document History

| Version | Date | Author | Changes |
|---|---|---|---|
| 1.0.0 | June 2026 | EaaS Product Team | Initial PRD |

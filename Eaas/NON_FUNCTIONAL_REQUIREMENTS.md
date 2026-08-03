# Non-Functional Requirements & SLAs
# EaaS — Escrow as a Service
# Version 1.0.0

---

## 1. Performance Requirements

| Metric | Requirement | Measurement Method |
|---|---|---|
| API response time (p50) | < 150ms | Prometheus histogram |
| API response time (p95) | < 300ms | Prometheus histogram |
| API response time (p99) | < 500ms | Prometheus histogram |
| Interswitch webhook processing | < 2 seconds | End-to-end timing |
| Notification delivery | < 2 minutes | Email/SMS delivery logs |
| Payout initiation | < 5 minutes after trigger | Audit log timestamp diff |
| Payout completion | < 24 hours | Interswitch SLA |
| Auto-release polling | Every 5 minutes | Scheduler logs |

---

## 2. Availability & Reliability

| Metric | Requirement | Notes |
|---|---|---|
| System uptime | 99.9% | < 8.7 hours downtime/year |
| Planned maintenance | < 1 hour/month | Off-peak hours only |
| Recovery Time Objective (RTO) | < 30 minutes | Time to restore after failure |
| Recovery Point Objective (RPO) | < 5 minutes | Max data loss acceptable |
| Database backup frequency | Every 6 hours | MSSQL Full + Transaction Log backups |
| Backup retention | 30 days | Encrypted backups in Cloudinary/Azure Blob |

### Redundancy Targets
- **API Services:** Minimum 2 instances per service
- **Database:** MSSQL with regular backups and point-in-time recovery
- **RabbitMQ:** 3-node cluster with mirrored queues
- **Redis:** Sentinel mode with 3 nodes
- **Load Balancer:** Managed by cloud provider (multi-AZ)

---

## 3. Scalability Requirements

| Scenario | Requirement |
|---|---|
| Baseline concurrent users | 500 |
| Peak concurrent users | 5,000 (10x baseline) |
| Baseline transactions/day | 500 |
| Peak transactions/day | 5,000 (10x baseline) |
| Concurrent transactions | 1,000+ on 3×16GB instances |
| Database connection pool size | 20 per service instance |
| MSSQL max connections | 500 per database |

---

## 4. Security Requirements

### Authentication & Authorisation
- JWT tokens expire after 1 hour (access token)
- Refresh tokens expire after 7 days and are rotated on every use
- API key identifiers are hashed with SHA-256; key secrets stored with BCrypt (cost factor 10)
- API key rotation supported on demand
- Role-based access control (CUSTOMER, MERCHANT, ADMIN)
- Resource-level ownership validation on every request

### Data Protection
- Bank account numbers: AES-256-GCM encrypted at rest via AesGcmEncryptionService (ENCRYPTION_MASTER_KEY env var)
- BVN: AES-256-GCM encrypted at rest
- Passwords: BCrypt hashed (never stored in plain text)
- Audit logs: append-only (no updates or deletes), diff-only via DiffAuditHelper
- PII in logs: masked via PiiMaskingUtils (e.g., email shown as j***@example.com)
- Database connections: TLS-encrypted

### Webhook Retry Specification
- Merchant webhook delivery uses RabbitMQ TTL+DLX retry with 5 intervals:
  1. 1 minute
  2. 5 minutes
  3. 30 minutes
  4. 2 hours
  5. 24 hours → alert admin, manual review

### Network Security
- TLS 1.2+ on all public endpoints
- Rate limiting: 100 requests/minute per API key
- IP-based rate limiting: 200 requests/minute per IP
- Webhook signatures: HMAC-SHA512 verified
- Internal services: accessible only within private network
- Secrets: stored in environment variables

### Compliance
- KYC required for all merchants before payout eligibility
- AML screening required for transactions > ₦5,000,000
- Transaction records retained for 7 years (CBN requirement)
- PCI-DSS: card data never touches EaaS servers (delegated to Interswitch)
- Data residency: all data stored in Nigeria/Africa regions

---

## 5. Observability Requirements

### Metrics (Prometheus + Grafana)
| Metric | Dashboard |
|---|---|
| API request rate | Operations |
| API error rate (4xx, 5xx) | Operations |
| API latency (p50, p95, p99) | Operations |
| Escrow creation rate | Business |
| Escrow success rate | Business |
| Dispute rate | Business |
| Payout success rate | Finance |
| RabbitMQ queue depth | Infrastructure |
| Instance CPU/Memory utilisation | Infrastructure |
| Database connection pool usage | Infrastructure |

### Alerts
| Alert | Threshold | Severity | Response |
|---|---|---|---|
| API error rate | > 5% for 5 min | Critical | On-call engineer |
| API latency p99 | > 1s for 5 min | High | Engineering team |
| Payout failure | Any failure | High | Finance + Engineering |
| DLQ messages | > 0 | High | Engineering team |
| Service crash | Any crash | Critical | On-call engineer |
| Database connections | > 80% pool used | High | Engineering team |
| Dispute unresolved | > 48 hours open | High | Operations team |
| Auto-release job | Last run > 10 min ago | High | Engineering team |

### Logging
- Structured JSON logs (no plaintext)
- Log levels: ERROR, WARN, INFO, DEBUG
- Correlation ID on every request (X-Correlation-ID header)
- Logs shipped to ELK Stack (Elasticsearch + Logstash + Kibana)
- Log retention: 90 days hot, 1 year cold storage

---

## 6. Data Integrity Requirements

| Requirement | Implementation |
|---|---|
| Atomic state transitions | @Transactional on all state changes |
| No duplicate escrow funding | Idempotency key on payment webhook |
| No double payouts | Idempotency check on payout initiation |
| Audit trail for every change | escrow_state_history + audit_logs (diff only) |
| Referential integrity | Foreign keys enforced within each service database |
| Amount precision | DECIMAL(15,2) — no floating point |
| Currency consistency | Currency code always stored with amount |
| Database constraints | CHECK constraints on status transitions |

---

## 7. Operational Requirements

| Requirement | Detail |
|---|---|
| Zero-downtime deployments | Blue-green or rolling deployment via Docker |
| Configuration management | Environment variables |
| Secret management | Environment variables (encrypted at rest on host) |
| Database migrations | Liquibase (version-controlled, idempotent, MSSQL-native) |
| Health checks | /actuator/health on all services |
| Readiness probes | Check DB + RabbitMQ + Redis connectivity |
| Liveness probes | Basic HTTP 200 check |
| Graceful shutdown | 30-second drain period on process termination |

---

## 8. Capacity Planning

### Storage Estimates (Year 1)

| Table | Est. Rows/Month | Row Size | Monthly Growth |
|---|---|---|---|
| escrow_transactions | 5,000 | ~2 KB | ~10 MB |
| escrow_state_history | 40,000 | ~500 B | ~20 MB |
| audit_logs | 100,000 | ~500 B | ~50 MB |
| notifications | 60,000 | ~1 KB | ~60 MB |
| webhook_deliveries | 50,000 | ~2 KB | ~100 MB |
| dispute_evidence (Cloudinary) | 500 files | ~2 MB avg | ~1 GB |

**Estimated database size at 12 months:** ~3–5 GB (well within MSSQL capacity)
**Estimated Cloudinary storage at 12 months:** ~15–20 GB
**MSSQL Edition:** Standard Edition (sufficient for projected growth)

### Compute Estimates (Baseline)

| Service | CPU | Memory | Instances (min) |
|---|---|---|---|
| api-gateway | 250m | 256Mi | 2 |
| identity-service | 250m | 256Mi | 2 |
| escrow-service | 500m | 512Mi | 2 |
| payment-service | 250m | 256Mi | 2 |
| notification-service | 250m | 256Mi | 2 |
| communication-service | 250m | 256Mi | 2 |

---

## 9. Disaster Recovery Runbook

### Scenario 1: Database Primary Failure
1. Promote most recent backup to primary (< 30 minutes)
2. Alert fires → on-call engineer acknowledges
3. Verify new primary is accepting connections
4. Spin up new replica from latest backup
5. Monitor for data loss — compare last LSN (Log Sequence Numbers)
6. Post-incident report within 24 hours

### Scenario 2: RabbitMQ Node Failure
1. 3-node cluster: 1 node failure is non-impactful
2. Alert fires if queue depth grows > 10,000
3. Restart failed node via Docker or host manager
4. Verify mirrored queues are intact
5. Process DLQ messages manually if needed

### Scenario 3: Mass Payout Failure
1. Alert fires on any payout failure
2. Finance team reviews payout queue in admin panel
3. Identify root cause (Interswitch outage vs bad bank data)
4. If Interswitch outage: wait and retry after resolution
5. If bad bank data: contact merchant for updated details
6. Manual payout initiation via admin panel

### Scenario 4: Suspected Fraud / Account Compromise
1. Disable merchant API key immediately: `PUT /admin/merchants/{id}/suspend`
2. Freeze all INITIATED and FUNDED escrows for that merchant
3. Notify affected customers
4. Investigate audit logs for anomalous patterns
5. Engage legal/compliance team
6. File SAR if AML threshold crossed

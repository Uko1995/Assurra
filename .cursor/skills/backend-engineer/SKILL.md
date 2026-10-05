---
name: backend-engineer
description: >-
  Acts as Assurra's senior backend engineer for the Java 21 / Spring Boot 3.2
  services and the Spring Cloud Gateway. Use when changing controllers, DTOs,
  security, JPA entities, Flyway migrations, RabbitMQ messaging, Redis caching,
  gateway routes, payments, escrow, identity, KYC, notifications, disputes, or
  Docker Compose.
---

# Senior backend engineer

Change the service that owns the behavior. The gateway is the only public edge. Controllers are the contract: `Eaas/openapi.yaml` has drifted, so never edit a controller to match it.

## Stack

Java 21, Spring Boot 3.2.0 (parent POM), Spring Cloud 2023.0.0, Maven multi-module (`com.uko:eaas-platform:1.0.0-SNAPSHOT`), Lombok 1.18.46, MapStruct 1.5.5.Final, jjwt 0.12.3, `mssql-jdbc` 12.4.2.jre11, Flyway (`flyway-core` + `flyway-sqlserver`), `spring-dotenv` 4.0.0, `cloudinary-http5` 2.0.0, Testcontainers (`junit-jupiter`, `mssqlserver`).

| Module | Port | Starters beyond web/jpa/validation | Owns |
|---|---|---|---|
| `eaas-api-gateway` | 8080 | `spring-cloud-starter-gateway`, `data-redis-reactive`, resilience4j circuitbreaker, jjwt (tomcat excluded) | Routing, JWT check, rate limit, `X-User-Id` / `X-User-Role`, HMAC to downstream |
| `eaas-identity-service` | 8081 | security, amqp, data-redis, mail, actuator, BCrypt, Cloudinary | Register, login, refresh, users, merchant KYC, API keys, GDPR |
| `eaas-escrow-service` | 8082 | amqp, security, actuator | Escrows, fee configuration, schedulers |
| `eaas-payment-service` | 8083 | amqp, **webflux** (WebClient to Interswitch), security | Payments, payouts, AML, provider webhooks |
| `eaas-communication-service` | 8084 | amqp, mail, Cloudinary, `spring-webflux` | Notifications, disputes |

Per-service MSSQL 2022 database (`identity_db`, etc.), one shared Redis, one RabbitMQ. Only 8080 is published in `docker-compose.yml`; the rest use `expose`.

## Controllers that exist

`/api/v1/auth`, `/api/v1/users`, `/api/v1/merchants`, `/api/v1/admin` (identity), `/api/v1/privacy-policy`, `/api/v1/terms-of-service`, `/api/v1/escrow`, `/api/v1/admin/escrows`, `/api/v1/admin/fee-configurations`, `/api/v1/payments`, `/api/v1/payouts`, `/api/v1/webhooks`, `/api/v1/admin/payments`, `/api/v1/admin/payouts`, `/api/v1/admin/aml-alerts`, `/api/v1/notifications`, `/api/v1/disputes`, `/api/v1/admin/disputes`.

There is no admin payment list, only `POST /api/v1/admin/payments/{reference}/refund?reason=`.

## Gateway

Spring Cloud Gateway takes the first matching route. Specific admin paths must precede the identity catch-all `/api/v1/admin/**`:

- `/api/v1/admin/escrows/**`, `/api/v1/admin/fee-configurations/**` → 8082
- `/api/v1/admin/payments/**`, `/api/v1/admin/payouts/**`, `/api/v1/admin/aml-alerts/**` → 8083
- `/api/v1/admin/disputes/**` → 8084
- remaining `/api/v1/admin/**` and `/api/v1/merchants/**` → 8081

Keep that order in both documents of `eaas-api-gateway/src/main/resources/application.yml`: `docker` (`http://eaas-*-service:808x`) and `local` (`http://localhost:8081`–`8084`). The `local` profile must not override the Redis host.

`UnifiedAuthFilter.PUBLIC_PATHS` is prefix-matched (`startsWith`): `/api/v1/auth/register`, `/login`, `/refresh`, `/forgot-password`, `/reset-password`. Everything else needs `Authorization: Bearer`. Rate limits: authenticated burst 150 / replenish 10, public IP burst 30 / replenish 2. Each service also runs a `GatewayRequestValidationFilter`, so downstream calls must carry the gateway HMAC and the injected headers. Never trust a client-supplied user id as the caller.

## Contracts and invariants

- Wrap successes in `ApiResponse<T>` (`success`, `message`, `data`, `timestamp`). Gateway auth errors are plain `{ error, status, path }`. Do not change the envelope for one endpoint.
- Money is `DECIMAL` naira on the wire. Kobo conversion is internal to `InterswitchClient` only.
- `CreateEscrowRequest`: amount ₦100 to ₦10,000,000, description ≤1000 chars, quantity ≥1, `agreedDeliveryDays` 1–30. Creating an escrow requires `X-Idempotency-Key`.
- Fee: `escrow.fee.percentage: 1.5`, `min: 500`, `max: 50000`. `FeeCalculationService` prefers an active config for the merchant, then the global default, then those properties. `feeValue` is a fraction (`0.015`), `feeType` is `PERCENTAGE`, `FLAT`, or `BLENDED`, and the result is clamped to min/max.
- Clocks are real: `escrow.confirmation.window-hours: 72`, `escrow.payment.expiry-hours: 24`. `AutoReleaseScheduler` releases past-deadline escrows every 5 minutes and expires unfunded escrows hourly. `EscrowResponse` exposes `confirmationDeadline`, `autoReleaseAt`, and `paymentExpiresAt`.
- Enums: `EscrowStatus` (14), `PaymentStatus` (PENDING/PROCESSING/SUCCESS/FAILED/REFUNDED/CANCELLED), `PayoutStatus` (PENDING/QUEUED/PROCESSING/COMPLETED/FAILED/REVERSED), `PaymentChannel` (CARD/BANK_TRANSFER/USSD/QR/MOBILE_MONEY), `PayoutMethod`, `DisputeStatus` (OPEN/UNDER_REVIEW/RESOLVED_MERCHANT/RESOLVED_CUSTOMER/CLOSED), `DocumentType` (CAC_CERT/UTILITY_BILL/ID_CARD/PASSPORT/DRIVERS_LICENSE/BANK_STATEMENT/OTHER). Do not add a shortcut transition.
- `GET /api/v1/escrow` is already scoped by the caller's role and id. Do not bolt on a client-supplied owner filter.
- Register does not issue tokens; login does, on role-specific paths. Access token 3600000 ms, refresh 604800000 ms, refresh rotates on use.
- Uploads stay server-mediated through Cloudinary (KYC documents, dispute evidence).

## Messaging, cache, schema

RabbitMQ: topic exchange `eaas.exchange`, direct DLX `eaas.dlx`. Queues each have a `.dlq`: `identity.audit.events`, `escrow.user.events`, `escrow.payment.events`, `payment.escrow.triggers`, `payment.user.events`, `comm.notifications`. Listeners retry 3 times with a 1s initial interval. Event classes are duplicated per service, so `__TypeId__` is not written; keep payloads structural.

Redis in identity caches users and users-by-email for 15 minutes, merchant profiles 10, KYC status 5, default 10. A cached `Optional<User>` must stay serializable.

JPA runs `ddl-auto: validate` with Flyway over `classpath:db/migration` and `classpath:db/repeatable`, `clean-disabled`, no out-of-order. Schema changes are a new versioned migration, never an entity-only edit.

## Security rules

Account lockout after 5 failed logins for 30 minutes. API keys are `sk_live_` + 32 chars, returned in full exactly once. `encryption.master-key` is 64 hex chars for AES-GCM over `bank_account_number`, `bvn`, and `phone`; changing it makes those columns unreadable. Identity keeps `app.frontend-url` (default `http://localhost:3000`) for email links.

Never commit secrets, widen `permitAll`, or log tokens, BVNs, or API-key secrets.

## Build

Run `bash mvnw` — the wrapper is not executable, so `./mvnw` exits 126. Always pass `--settings .github/maven-settings.xml`; the parent POM lists a private Archiva at `172.25.20.29` that is unreachable, and that settings file mirrors everything to Maven Central.

`bash mvnw -B verify --settings .github/maven-settings.xml` is the full check. Identity integration tests need a Docker socket for Testcontainers (SQL Server 2022, Redis 7, RabbitMQ 3.13); this WSL box usually has none, so say plainly when containers did not run.

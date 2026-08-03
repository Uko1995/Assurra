# User Stories & Acceptance Criteria
# EaaS — Escrow as a Service

**Version:** 1.0.0
**Date:** June 2026

---

## Epic 1: Merchant Onboarding

---

### US-001: Merchant Registration
**As a** merchant,
**I want to** register my business on EaaS,
**So that** I can receive escrow-protected payments from customers.

#### Acceptance Criteria:
```
AC-001-1: Given I submit valid registration details (business name, email,
          phone, bank account, bank code, BVN),
          When I POST /api/v1/auth/register with role=MERCHANT,
          Then I receive a 201 response with my user ID and a message
          that KYC review is pending.

AC-001-2: Given I submit an email that already exists in the system,
          When I attempt to register,
          Then I receive a 409 Conflict response with message
          "Email already registered".

AC-001-3: Given I submit a missing required field (e.g. no BVN),
          When I attempt to register,
          Then I receive a 400 Bad Request with field-level validation errors.

AC-001-4: Given my registration is submitted,
          When the system processes it,
          Then I receive a confirmation email within 5 minutes
          with my registration reference.
```

---

### US-002: Merchant KYC Submission
**As a** registered merchant,
**I want to** submit my KYC documents,
**So that** my account can be verified and I can go live.

#### Acceptance Criteria:
```
AC-002-1: Given I am a registered merchant with status PENDING,
          When I POST /api/v1/merchants/kyc with valid documents
          (CAC certificate, utility bill, ID),
          Then I receive a 200 response confirming documents received
          and my KYC status changes to UNDER_REVIEW.

AC-002-2: Given my KYC is UNDER_REVIEW,
          When an admin approves it,
          Then my KYC status changes to VERIFIED,
          I receive an approval email,
          And my API key is generated and included in the email.

AC-002-3: Given my KYC is UNDER_REVIEW,
          When an admin rejects it with a reason,
          Then my KYC status changes to REJECTED,
          And I receive a rejection email with the specific reason
          and instructions to resubmit.

AC-002-4: Given my KYC is not VERIFIED,
          When I attempt to receive a payout,
          Then the payout is blocked with error "Merchant KYC not verified".
```

---

### US-003: Merchant API Key Management
**As a** verified merchant,
**I want to** receive and manage my API key,
**So that** I can integrate EaaS into my platform.

#### Acceptance Criteria:
```
AC-003-1: Given my KYC is VERIFIED,
          When I access my merchant profile,
          Then I can see my API key (partially masked: sk_live_****XXXX).

AC-003-2: Given I want to regenerate my API key,
          When I POST /api/v1/merchants/api-key/regenerate,
          Then my old API key is immediately invalidated,
          A new API key is generated and returned once (not stored in full),
          And I receive an email alert about the key regeneration.

AC-003-3: Given I make an API request with an invalid or expired API key,
          When the API Gateway processes the request,
          Then I receive a 401 Unauthorized response.
```

---

## Epic 2: Customer Registration

---

### US-004: Customer Registration
**As a** customer,
**I want to** register on EaaS,
**So that** I can make escrow-protected purchases.

#### Acceptance Criteria:
```
AC-004-1: Given I submit valid details (full name, email, phone, password),
          When I POST /api/v1/auth/register with role=CUSTOMER,
          Then I receive a 201 response with my user ID,
          And a verification email is sent to my email address.

AC-004-2: Given I have not verified my email,
          When I attempt to create an escrow transaction,
          Then I receive a 403 response with message
          "Please verify your email to continue".

AC-004-3: Given I click the verification link in my email,
          When the system processes it,
          Then my email is marked as verified,
          And I am redirected to a success page.
```

---

## Epic 3: Escrow Transaction Lifecycle

---

### US-005: Create Escrow Transaction
**As a** customer,
**I want to** create an escrow transaction for a product I want to buy,
**So that** my payment is protected until I confirm I received the product.

#### Acceptance Criteria:
```
AC-005-1: Given I am a verified customer,
          When I POST /api/v1/escrow with valid payload
          (merchantId, productDescription, amount, currency, agreedDeliveryDays),
          Then I receive a 201 response containing:
          - Unique escrow reference (TXN-YYYY-XXXXXXXX)
          - Escrow fee breakdown (amount, fee, merchantAmount)
          - A payment link
          - Status: INITIATED
          - Expiry time (24 hours to complete payment).

AC-005-2: Given I submit the same request with the same idempotency key,
          When the system processes it,
          Then I receive the same escrow transaction (no duplicate created)
          with a 200 response.

AC-005-3: Given I submit a negative or zero amount,
          When the system validates the request,
          Then I receive a 400 Bad Request with message
          "Amount must be greater than zero".

AC-005-4: Given I submit an invalid merchantId that does not exist,
          When the system validates the request,
          Then I receive a 404 Not Found with message "Merchant not found".

AC-005-5: Given I submit a merchantId of an unverified merchant,
          When the system validates the request,
          Then I receive a 400 Bad Request with message
          "Merchant is not verified to receive escrow payments".

AC-005-6: Given a created escrow is not funded within 24 hours,
          When the expiry time passes,
          Then the escrow status changes to CANCELLED automatically,
          And the customer and merchant are notified.
```

---

### US-006: Fund Escrow via Payment
**As a** customer,
**I want to** pay into the escrow via card or bank transfer,
**So that** the merchant is notified and can ship my product.

#### Acceptance Criteria:
```
AC-006-1: Given I have an escrow in INITIATED status,
          When I complete payment via the Interswitch payment link,
          Then Interswitch sends a webhook to /api/v1/escrow/{ref}/fund,
          The escrow status changes to FUNDED,
          And the merchant is notified within 2 minutes.

AC-006-2: Given a Interswitch webhook is received,
          When the signature header does not match the expected HMAC,
          Then the webhook is rejected with 401,
          And the escrow status does not change.

AC-006-3: Given a payment webhook is received twice for the same transaction,
          When the system processes the second webhook,
          Then idempotency check prevents double-processing,
          And the system returns 200 without changing state again.

AC-006-4: Given payment is successful,
          When the escrow moves to FUNDED,
          Then I (customer) receive an email confirmation with:
          - Escrow reference
          - Amount paid
          - Product description
          - Merchant name
          - Expected delivery date.
```

---

### US-007: Merchant Ships Product
**As a** merchant,
**I want to** submit the tracking number after I ship the product,
**So that** the customer knows the product is on its way.

#### Acceptance Criteria:
```
AC-007-1: Given an escrow in MERCHANT_NOTIFIED status,
          When I POST /api/v1/escrow/{ref}/ship with valid trackingNumber
          and logisticsProvider,
          Then the escrow status changes to SHIPPED,
          And the customer is notified with tracking details within 2 minutes.

AC-007-2: Given an escrow in INITIATED or FUNDED status,
          When I attempt to submit a tracking number,
          Then I receive a 409 Conflict with message
          "Cannot ship before merchant notification is confirmed".

AC-007-3: Given I submit an empty tracking number,
          When the system validates the request,
          Then I receive a 400 Bad Request with message
          "Tracking number is required".

AC-007-4: Given the agreed delivery days have passed without shipping,
          When the system detects this,
          Then the customer is notified that the merchant has not shipped,
          And EaaS admin is alerted for possible intervention.
```

---

### US-008: Mark Product as Delivered
**As a** system or merchant,
**I want to** mark an escrow as delivered,
**So that** the customer's 72-hour confirmation window begins.

#### Acceptance Criteria:
```
AC-008-1: Given an escrow in SHIPPED status,
          When POST /api/v1/escrow/{ref}/deliver is called
          (by merchant or logistics webhook),
          Then the escrow status changes to DELIVERED,
          confirmation_deadline is set to now + 72 hours,
          auto_release_at is set to now + 72 hours,
          And the customer is notified to confirm or dispute within 72 hours.

AC-008-2: Given the delivery notification is sent,
          When the customer views it,
          Then the notification clearly shows the deadline time and date
          for confirming or raising a dispute.
```

---

### US-009: Customer Confirms Delivery
**As a** customer,
**I want to** confirm that I received my product in good condition,
**So that** the merchant receives their payment.

#### Acceptance Criteria:
```
AC-009-1: Given an escrow in DELIVERED status within the confirmation window,
          When I POST /api/v1/escrow/{ref}/confirm,
          Then the escrow status changes to CONFIRMED,
          A payout is triggered to the merchant,
          And both parties are notified.

AC-009-2: Given an escrow is already in CONFIRMED status,
          When I attempt to confirm again,
          Then I receive a 409 Conflict with message
          "Transaction already confirmed".

AC-009-3: Given the confirmation window has expired (past 72 hours),
          When I attempt to confirm,
          Then I receive a 409 with message
          "Confirmation window has expired — funds were auto-released".

AC-009-4: Given I confirm delivery,
          When the merchant payout is initiated,
          Then the merchant receives an email showing:
          - Amount to be received
          - Escrow fee deducted
          - Net payout amount
          - Expected bank credit time (within 24 hours).
```

---

### US-010: Auto-Release After 72 Hours
**As the** system,
**I want to** automatically release funds to the merchant after 72 hours of inactivity,
**So that** merchants are not indefinitely blocked from their payment.

#### Acceptance Criteria:
```
AC-010-1: Given an escrow has been in DELIVERED status for 72 hours,
          When the scheduler runs,
          Then the escrow status changes to AUTO_RELEASED,
          A payout is triggered to the merchant,
          And the customer receives a notification that funds were auto-released.

AC-010-2: Given the auto-release job runs,
          When it processes multiple expired transactions,
          Then each is processed atomically — one failure does not block others.

AC-010-3: Given an escrow is in DISPUTED status,
          When the auto-release job runs,
          Then the disputed escrow is NOT auto-released.
```

---

## Epic 4: Dispute Management

---

### US-011: Customer Raises Dispute
**As a** customer,
**I want to** raise a dispute if I receive a wrong, damaged, or missing product,
**So that** my payment is protected and the issue is investigated.

#### Acceptance Criteria:
```
AC-011-1: Given an escrow in DELIVERED status within the confirmation window,
          When I POST /api/v1/escrow/{ref}/dispute with a valid reason and description,
          Then the escrow status changes to DISPUTED,
          A dispute record is created,
          The merchant is notified of the dispute,
          And an EaaS admin is alerted.

AC-011-2: Given an escrow already in DISPUTED status,
          When I attempt to raise another dispute,
          Then I receive a 409 Conflict with message
          "A dispute already exists for this transaction".

AC-011-3: Given I raise a dispute with reason=OTHER,
          When the system validates it,
          Then the description field is required (minimum 20 characters).
          If empty, return 400 with "Please describe your dispute in detail".

AC-011-4: Given a dispute is raised,
          When the merchant receives notification,
          Then the merchant has 24 hours to respond with evidence
          before admin makes a decision.
```

---

### US-012: Upload Dispute Evidence
**As a** customer or merchant,
**I want to** upload evidence for a dispute,
**So that** the admin can make a fair and informed decision.

#### Acceptance Criteria:
```
AC-012-1: Given an open dispute,
          When I POST /api/v1/disputes/{id}/evidence with a file,
          Then the file is uploaded to secure storage (S3),
          A record is created linking the evidence to the dispute,
          And I receive a 201 response confirming the upload.

AC-012-2: Given I upload a file larger than 10MB,
          When the system validates it,
          Then I receive a 413 Payload Too Large with message
          "File must not exceed 10MB".

AC-012-3: Given I upload a file with unsupported type (e.g. .exe),
          When the system validates it,
          Then I receive a 400 with message
          "Only image, video, and PDF files are accepted".

AC-012-4: Given a dispute is RESOLVED,
          When I attempt to upload more evidence,
          Then I receive a 409 with message
          "Dispute is already resolved — no further evidence accepted".
```

---

### US-013: Admin Resolves Dispute
**As an** EaaS admin,
**I want to** review evidence and resolve disputes,
**So that** funds are fairly distributed and the transaction is closed.

#### Acceptance Criteria:
```
AC-013-1: Given a dispute in OPEN or UNDER_REVIEW status,
          When I PUT /api/v1/admin/disputes/{id} with resolution
          (RESOLVED_MERCHANT or RESOLVED_CUSTOMER) and resolution_note,
          Then the dispute status changes,
          The escrow status changes accordingly,
          A payout or refund is triggered,
          And both parties are notified of the decision with the resolution note.

AC-013-2: Given I resolve in favour of the merchant (RESOLVED_MERCHANT),
          When the system processes it,
          Then the escrow status → RESOLVED_MERCHANT → RELEASED,
          And merchant payout is initiated.

AC-013-3: Given I resolve in favour of the customer (RESOLVED_CUSTOMER),
          When the system processes it,
          Then the escrow status → RESOLVED_CUSTOMER → REFUNDED,
          And customer refund is initiated to the original payment channel.

AC-013-4: Given I attempt to resolve a dispute without a resolution_note,
          When the system validates it,
          Then I receive a 400 with message "Resolution note is required".
```

---

## Epic 5: Payout & Refund

---

### US-014: Merchant Receives Payout
**As a** merchant,
**I want to** receive payment to my bank account after escrow is released,
**So that** I can access my earnings.

#### Acceptance Criteria:
```
AC-014-1: Given an escrow is CONFIRMED, AUTO_RELEASED, or RESOLVED_MERCHANT,
          When the payout service processes the release,
          Then a payout record is created with status PENDING,
          The transfer is initiated via Interswitch Transfer API,
          And the merchant receives an email with payout details.

AC-014-2: Given a payout is initiated,
          When it completes successfully,
          Then the payout status changes to SUCCESS,
          paid_out_at is recorded,
          And the merchant receives a credit alert notification.

AC-014-3: Given a payout fails (e.g. invalid bank account),
           When Interswitch returns failure,
          Then the payout status changes to FAILED with failure_reason stored,
          An alert is sent to EaaS admin,
          And a retry is attempted within 1 hour.

AC-014-4: Given payout is triggered,
          When the merchant receives the funds,
          Then the amount = escrow amount minus escrow fee.
          The fee breakdown is shown in the notification.
```

---

### US-015: Customer Receives Refund
**As a** customer,
**I want to** receive a refund to my original payment method when a dispute is resolved in my favour,
**So that** my money is returned promptly.

#### Acceptance Criteria:
```
AC-015-1: Given an escrow is RESOLVED_CUSTOMER,
          When the payout service processes the refund,
          Then a refund is initiated to the customer's original payment channel,
          The customer receives an email confirming the refund,
          And the escrow status changes to REFUNDED.

AC-015-2: Given a refund is initiated via Interswitch,
          When it completes,
          Then the customer receives the full escrow amount
          (escrow fee is absorbed by EaaS in dispute-won scenarios).

AC-015-3: Given a refund fails,
           When Interswitch returns failure,
          Then an EaaS admin is alerted immediately,
          And manual intervention is initiated within 4 hours.
```

---

## Epic 6: Notifications

---

### US-016: Email Notifications
**As a** customer or merchant,
**I want to** receive timely email notifications at every step of the escrow lifecycle,
**So that** I am always informed about the status of my transaction.

#### Acceptance Criteria:
```
AC-016-1: Notifications are sent at these trigger points:
          - Customer: Escrow created (with payment link)
          - Customer: Payment confirmed
          - Merchant: Escrow funded (ship now)
          - Customer: Product shipped (with tracking)
          - Customer: Product delivered (confirm within 72hrs)
          - Merchant: Dispute raised
          - Both: Dispute resolved
          - Merchant: Payout initiated
          - Merchant: Payout successful
          - Customer: Refund initiated
          - Both: Auto-release triggered

AC-016-2: Given a notification trigger occurs,
          When the notification service processes it,
          Then the email is delivered within 2 minutes of the trigger.

AC-016-3: Given an email fails to deliver,
          When the system detects a bounce,
          Then it retries up to 3 times with exponential backoff.
```

---

### US-017: Merchant Webhook Notifications
**As a** merchant integrator,
**I want to** receive webhook events for every escrow state change,
**So that** I can update my platform in real time.

#### Acceptance Criteria:
```
AC-017-1: Given a merchant has configured a webhook URL,
          When an escrow state changes,
          Then a POST request is sent to the merchant's webhook URL
          within 30 seconds with the event payload and
          X-EaaS-Signature header for verification.

AC-017-2: Given the webhook endpoint returns a non-2xx response,
          When the system detects failure,
          Then it retries with exponential backoff:
          1 min → 5 min → 30 min → 2 hours → 24 hours.

AC-017-3: Given 5 consecutive webhook failures,
          When the system detects this,
          Then the webhook is disabled and the merchant is notified
          to check their endpoint configuration.
```

---

## Epic 7: Admin Operations

---

### US-018: Admin Views All Transactions
**As an** EaaS admin,
**I want to** view all escrow transactions with filtering and search,
**So that** I can monitor platform activity and intervene when needed.

#### Acceptance Criteria:
```
AC-018-1: Given I am an authenticated admin,
          When I GET /api/v1/admin/escrows,
          Then I receive a paginated list of all transactions
          with filters for: status, date range, merchant, amount range.

AC-018-2: Given I search by escrow reference,
          When the system processes it,
          Then I receive the exact matching transaction or 404.

AC-018-3: Given I export transactions,
          When I request a CSV export with date range filters,
          Then a CSV file is generated and available for download within 30 seconds.
```

---

### US-019: Admin Manages KYC
**As an** EaaS admin,
**I want to** review and approve or reject merchant KYC submissions,
**So that** only legitimate merchants can receive payouts.

#### Acceptance Criteria:
```
AC-019-1: Given a merchant submits KYC documents,
          When I access the admin KYC queue,
          Then I can see all pending KYC submissions
          with the merchant's submitted documents.

AC-019-2: Given I approve a KYC submission,
          When I PUT /api/v1/admin/kyc/{merchantId}/approve,
          Then the merchant's KYC status changes to VERIFIED,
          Their API key is generated,
          And they receive an approval email.

AC-019-3: Given I reject a KYC submission with a reason,
          When I PUT /api/v1/admin/kyc/{merchantId}/reject,
          Then the merchant's KYC status changes to REJECTED,
          And they receive a rejection email with the reason.
```

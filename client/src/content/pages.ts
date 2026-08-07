/** Stitch-accurate marketing copy pulled from canonical desktop screens. */

export const images = {
  homeHero: "/images/home-hero.webp",
  hiwDispute: "/images/hiw-dispute.webp",
  merchantsHero: "/images/merchants-hero.webp",
  customersHero: "/images/customers-hero.webp",
  securityHero: "/images/security-hero.webp",
  helpHero: "/images/help-hero.webp",
  dataSaverHero: "/images/data-saver-hero.webp",
  notFoundHero: "/images/not-found-hero.webp",
} as const;

export const homeContent = {
  brand: "Assurra",
  headline: "Escrow that works the way African commerce works.",
  support:
    "Secure your naira payments in a neutral vault. Buyers are protected until delivery. Sellers are guaranteed payment upon completion.",
  trustStrip: [
    "Bank-grade encryption",
    "48-hour dispute SLA",
    "Naira-native Nigerian bank payouts",
    "Built for slow networks",
  ],
  middleGround: {
    title: "The Middle Ground for Digital Commerce",
    description: "Bridging the trust gap between buyers and sellers in Nigeria.",
    buyerRisks: [
      "Seller ghosting after payment",
      "Receiving the wrong item",
      "Counterfeit goods",
    ],
    sellerRisks: [
      "Fraudulent chargebacks",
      "Buyers with no intent to pay",
      "Lack of trust limits sales",
    ],
    center: "The neutral vault securing funds until both parties are satisfied.",
  },
  lifecycleEyebrow: "How It Works",
  lifecycleTitle: "From agreement to payout in 7 clear steps.",
  audiences: {
    title: "Who is Assurra for?",
    merchants: [
      "Simple KYC onboarding",
      "Instant funded alerts",
      "Transparent 1.5% fee",
    ],
    customers: [
      "Safe online payments",
      "Objective dispute resolution",
      "Guaranteed refunds for non-delivery",
    ],
    developers: [
      "Clean /api/v1 architecture",
      "JWT auth & webhooks",
      "Fully functional sandbox",
    ],
  },
  proof: {
    title: "The Proof is in the Protocol",
    stats: [
      { value: "1.5%", label: "Flat transaction fee" },
      { value: "₦1,000", label: "Minimum transaction size" },
      { value: "24h", label: "Auto-cancel for unfunded escrows" },
      { value: "99.9%", label: "Platform uptime target" },
    ],
    notes: [
      "Disputes freeze payouts immediately",
      "NDPR compliant data handling",
    ],
  },
  feeSnapshot: {
    title: "Simple, Transparent Pricing",
    product: "₦100,000",
    fee: "₦1,500",
    payout: "₦98,500",
  },
  finalCta: {
    title: "Let Assurra hold the money while you focus on the deal.",
  },
} as const;

export const howItWorksSteps = [
  {
    title: "Create escrow",
    status: "INITIATED",
    body: "Initiate via dashboard or API. Every escrow gets a unique reference (e.g. TXN-2024-82930411). API partners must send an X-Idempotency-Key.",
  },
  {
    title: "Pay via Interswitch",
    status: "FUNDED",
    body: "The customer opens a secure Interswitch payment link. Funds are held in a CBN-regulated account until the transaction completes.",
  },
  {
    title: "Merchant notified",
    status: "MERCHANT_NOTIFIED",
    body: "Once payment is verified, the merchant is notified via email, SMS, and webhook to begin fulfillment.",
  },
  {
    title: "Ship with tracking",
    status: "SHIPPED",
    body: "The merchant ships and uploads a tracking number so the buyer can follow delivery.",
  },
  {
    title: "Delivery & inspection",
    status: "DELIVERED",
    body: "Merchant marks delivered. A 72-hour inspection window starts for the customer to verify the product.",
  },
  {
    title: "Confirm or auto-release",
    status: "CONFIRMED / AUTO_RELEASED",
    body: "Customer confirms receipt, or funds auto-release after the 72-hour window if there is no response.",
  },
  {
    title: "Payout",
    status: "RELEASED",
    body: "Funds settle to the merchant’s verified Nigerian bank account — target under 24 hours.",
  },
] as const;

export const lifecycleNotes = [
  {
    title: "Cancellation",
    body: "Unfunded escrows cancel after 24 hours with no fees incurred by either party.",
  },
  {
    title: "Dispute window",
    body: "Raise a dispute within the 72-hour inspection window. Evidence-based mediation targets resolution under 48 hours.",
  },
  {
    title: "Transparent fees",
    body: "A flat 1.5% fee applies. Customers pay product price + fee; merchants receive the net amount upon release.",
  },
] as const;

export const resolutionCenter = {
  title: "Fair & Transparent Dispute Handling",
  description:
    "Our Resolution Center handles disagreements swiftly and fairly, rooted in Nigerian regulatory frameworks.",
  points: [
    "Neutral, CBN-compliant mediation process",
    "Comprehensive audit trail of communications and agreements",
    "Moderated channels to resolve issues amiably",
  ],
} as const;

export const pricingContent = {
  title: "Transparent Pricing, Zero Surprises.",
  description:
    "Secure high-stakes transactions with a simple fee structure. Default escrow fee is 1.5% (configurable for enterprise partners), with a minimum transaction of ₦1,000. No hidden FX — Naira-native security.",
  rules: [
    "Default escrow fee ~1.5% (configurable per platform fee settings).",
    "Customer total charge = product amount + fee.",
    "Merchant payout = product amount − fee (net).",
    "Fee is non-refundable unless Assurra is at fault.",
    "Unfunded escrows cancel after 24 hours with no fee.",
    "Minimum transaction amount is ₦1,000.",
    "Naira-native — no hidden FX markup on the escrow fee model.",
    "Configurable fee structures for high-volume merchants.",
  ],
} as const;

export const services = [
  {
    title: "Personal Escrow",
    body: "P2P safety and buyer protection for one-off sales. Funds stay held until both parties are satisfied.",
    href: "/for-customers",
    cta: "Explore Personal",
  },
  {
    title: "Business Escrow",
    body: "B2B services with merchant payouts after KYC and support for higher-volume trading.",
    href: "/for-merchants",
    cta: "Explore Business",
  },
  {
    title: "Milestone Payments",
    body: "Release funds as project phases are completed and approved — ideal for long-term work.",
    href: "/how-it-works",
    cta: "Explore Milestones",
  },
  {
    title: "Platform API",
    body: "Embed Assurra into checkout flows via REST, HMAC webhooks, and a sandbox for testing.",
    href: "/developers",
    cta: "View Integrations",
  },
] as const;

export const serviceTruths = [
  {
    title: "Interswitch Naira Payments",
    body: "Secure deposits and Nigerian bank payouts through Interswitch.",
  },
  {
    title: "Automated Notifications",
    body: "Email, SMS, and in-app alerts keep every party updated in real time.",
  },
  {
    title: "HMAC Webhooks",
    body: "Signed callbacks so platforms can trust every escrow state change.",
  },
] as const;

export const merchantHighlights = [
  {
    title: "Eliminate payment-on-delivery risk",
    body: "Buyers commit funds upfront into escrow, so you ship against verified intent — not fake orders or rejected deliveries.",
  },
  {
    title: "Instant funded alerts",
    body: "Email, SMS, and webhooks tell you the moment a buyer pays — before you move inventory.",
  },
  {
    title: "Fulfill & track",
    body: "Ship with tracking, mark delivered, and start the buyer’s 72-hour inspection window.",
  },
  {
    title: "24-hour net payouts",
    body: "After release, settle to your Nigerian bank under a 24-hour target. Transparent ~1.5% fee. KYC required first.",
  },
] as const;

export const merchantGrowth = [
  {
    title: "Close more sales",
    body: "When buyers know funds are held until delivery, hesitation drops and conversion rises.",
  },
  {
    title: "Flexible integration",
    body: "Shareable escrow links for SMEs, or REST + webhooks for platforms after KYC unlocks an API key.",
  },
] as const;

export const customerHighlights = [
  {
    title: "Secure payment",
    body: "Pay via Interswitch. Funds stay held by Assurra and are never released until you are satisfied.",
  },
  {
    title: "72-hour window",
    body: "After delivery you have 72 hours to inspect, confirm, or raise a dispute with evidence.",
  },
  {
    title: "Dispute & resolve",
    body: "An active dispute freezes payout immediately. Target binding resolution under 48 hours.",
  },
  {
    title: "Auto-release & refunds",
    body: "Silence after 72 hours auto-releases to the merchant. A buyer-favour ruling triggers a refund path.",
  },
] as const;

export const developerEndpoints = [
  {
    method: "POST",
    path: "/api/v1/auth/register/merchant",
    note: "Merchant signup; KYC required before payouts",
  },
  {
    method: "POST",
    path: "/api/v1/auth/login/merchant",
    note: "JWT access + rotating refresh token",
  },
  {
    method: "POST",
    path: "/api/v1/escrow",
    note: "Create escrow — X-Idempotency-Key required",
  },
  {
    method: "POST",
    path: "/api/v1/escrow/{reference}/ship",
    note: "Attach trackingNumber; update status to shipped",
  },
  {
    method: "POST",
    path: "/api/v1/escrow/{reference}/confirm",
    note: "Buyer confirmation to release funds",
  },
  {
    method: "POST",
    path: "/api/v1/disputes",
    note: "Open dispute within the confirmation window",
  },
] as const;

export const securityPoints = [
  {
    title: "Fund custody",
    body: "Money is held until confirm, auto-release, or dispute resolution — never silent release.",
  },
  {
    title: "Modern auth",
    body: "JWT access tokens (1 hour) with rotating refresh tokens. Merchants can use X-API-Key after KYC.",
  },
  {
    title: "Encrypted sensitive data",
    body: "Bank accounts and BVN encrypted at rest with AES-256. Passwords hashed with BCrypt.",
  },
  {
    title: "Webhook integrity",
    body: "HMAC-signed callbacks with retry ladder: 1m → 5m → 30m → 2h → 24h.",
  },
  {
    title: "Compliance posture",
    body: "KYC before payouts, AML monitoring, NDPR-aligned privacy, append-only audit logs.",
  },
  {
    title: "Reliability target",
    body: "99.9% uptime goal with monitored services and resilient messaging.",
  },
] as const;

export const helpCategories = [
  {
    title: "Account & Security",
    items: [
      "How to create an escrow (initiate terms, lock funds)",
      "KYC for merchants (mandatory for payouts)",
    ],
  },
  {
    title: "Payments & Fees",
    items: [
      "How to pay (Interswitch integration)",
      "Fees (transparent 1.5% fee)",
    ],
  },
  {
    title: "Dispute Resolution",
    items: [
      "Shipping & confirmation (72h inspection window)",
      "Disputes (freeze payout, evidence submission)",
    ],
  },
] as const;

export const helpFaqs = [
  {
    q: "How long do I have to confirm delivery?",
    a: "72 hours after the merchant marks the escrow as delivered. If you do nothing, funds auto-release to the merchant.",
  },
  {
    q: "When can I raise a dispute?",
    a: "Only while the confirmation window is open. Disputes freeze payout until Assurra resolves the case.",
  },
  {
    q: "What does the fee cover?",
    a: "A default 1.5% escrow fee. The customer pays product + fee; the merchant receives net after fee. Unfunded cancels incur no fee.",
  },
  {
    q: "When do merchants get paid?",
    a: "After customer confirmation or auto-release (or a merchant-favour dispute). Target payout to a Nigerian bank is under 24 hours. KYC is required first.",
  },
  {
    q: "Is there a sandbox for developers?",
    a: "Yes. Integrate against /api/v1 with JWT or X-API-Key, idempotent escrow create, and HMAC webhooks without moving real money.",
  },
] as const;

export const dataSaverFacts = [
  {
    title: "Low-data mode",
    body: "Strips rich media, prioritizing core transaction and marketing content.",
  },
  {
    title: "~80% less decorative data",
    body: "Optimized payloads keep essential copy flowing when bandwidth is scarce.",
  },
  {
    title: "Built for 2G/3G",
    body: "Disable animations and decorative imagery so pages stay usable on weak signals.",
  },
] as const;

export const privacySections = [
  {
    title: "1. Introduction",
    body: "Assurra Escrow (“Assurra”, “we”, “us”, or “our”) protects personal information in line with the Nigerian Data Protection Regulation (NDPR). Contact privacy@assurra.com with questions. We only collect data necessary to facilitate escrow and comply with financial regulations — we do not sell your data.",
  },
  {
    title: "2. Information we collect",
    body: "Identity data (name, date of birth, NIN/BVN/passport for KYC), contact data, bank details for disbursements (card data handled by licensed PSPs), transaction data, and automatic device/usage logs such as IP and browser characteristics.",
  },
  {
    title: "3. How we use your data",
    body: "Service provision and payments, KYC/AML compliance required by the CBN, customer support, and fraud prevention monitoring.",
  },
  {
    title: "4. Data sharing & transfers",
    body: "We may share data with licensed PSPs (e.g. Interswitch), regulatory authorities when required, and authorized KYC partners. KYC and transaction records are retained for a minimum of five years after account closure or the last transaction for compliance.",
  },
  {
    title: "5. Your NDPR rights",
    body: "You may request access, rectification, erasure (where it does not conflict with financial retention rules), and portability. Contact dpo@assurra.com — we aim to respond within 30 days.",
  },
  {
    title: "6. Cookies",
    body: "Essential cookies keep the marketing site working. Analytics cookies load only after you accept them on the consent banner.",
  },
] as const;

export const termsSections = [
  {
    title: "01. Introduction",
    body: "These Terms govern access to Assurra’s escrow platform. By using Assurra you agree to be bound by them.",
  },
  {
    title: "02. Escrow agreement",
    body: "Assurra acts as a neutral escrow agent, holding funds until predefined conditions are met. We do not take ownership of goods. Funds release on confirmation, auto-release, cancellation, or dispute outcome. Failure to respond within the 72-hour confirmation window may result in automatic disbursement barring an active dispute.",
  },
  {
    title: "03. Platform fees",
    body: "Standard ~1.5% fee on transaction volume unless otherwise configured. Customers pay product amount plus fee; merchants receive net. Fees are non-refundable unless Assurra is at fault. Unfunded cancellations after 24 hours incur no fee.",
  },
  {
    title: "04. Dispute resolution",
    body: "On disagreement, funds freeze. Both parties submit evidence to the Resolution Center. Target resolution is 48 hours once evidence is received.",
  },
  {
    title: "05. KYC/AML requirements",
    body: "Users are subject to KYC and AML checks. Transactions may be delayed or cancelled if verification documents are not provided. Merchants must complete KYC before payout eligibility.",
  },
  {
    title: "06. Limitation of liability",
    body: "Assurra is not liable for indirect or consequential damages. Maximum liability is limited to the fees paid for the specific transaction in dispute.",
  },
  {
    title: "07. Acceptable use",
    body: "You may not use Assurra for illegal goods, sanctioned activity, or abuse of the dispute process. We may freeze or cancel escrows to comply with law or protect users.",
  },
] as const;

export const notFoundContent = {
  title: "Lost in transit?",
  description:
    "It seems the page you are looking for has been misdirected or is securely locked in our vault. Let’s get you back on a safe path.",
} as const;

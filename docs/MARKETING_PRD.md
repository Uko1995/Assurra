# Assurra Marketing Frontend — Product Requirements Document

**Version:** 1.0.0  
**Status:** Draft  
**Date:** August 2026  
**Author:** Frontend Team  
**Audience:** Product, Design, Frontend Engineering, Marketing  

---

## 1. Executive Summary

Assurra is the public marketing face of the EaaS escrow platform. This PRD defines the requirements for the **marketing frontend only** — the public, SEO-optimized, pre-authentication website that explains what Assurra does, why it is the right escrow choice for African commerce, and how merchants, customers, and platform partners can get started.

The marketing site is a **Next.js App Router `(marketing)` route group**, statically generated at build time with on-demand revalidation, hand-authored content, and strict performance budgets. It must load fast on low-bandwidth devices, rank well for relevant search terms, and convert visitors into registered merchants and confident customers.

### Primary Goal

Turn anonymous visitors into registered merchants and informed customers by clearly answering three questions within 10 seconds:

1. What is Assurra? *(Escrow for African commerce)*
2. Why should I trust it? *(Money is held safely, status is visible, disputes resolve fast)*
3. What do I do next? *(Create an escrow, become a merchant, or integrate the API)*

---

## 2. Scope

### In Scope

- Public marketing pages listed in the sitemap below.
- SEO metadata, Open Graph, Twitter Cards, JSON-LD structured data, sitemap, robots.
- NDPR-compliant cookie/analytics consent banner.
- Content management via hand-authored `content/*.mdx` files.
- Performance, accessibility, and Core Web Vital targets.
- Data-saver mode as a visible African-first feature.
- Responsive design for mobile, tablet, and desktop.

### Out of Scope

- Authentication flows (login, register, forgot password).
- Dashboard experiences (merchant, customer, admin).
- Real-time API integration for dynamic marketing content.
- E-commerce checkout, payment processing, or wallet UI.
- Multi-language support (English only for v1).

---

## 3. Product Positioning

### Core Promise

**Assurra holds money safely between buyers and sellers until everyone is satisfied.**

Built specifically for African commerce, Assurra makes escrow feel local, fast, and fair — not like a foreign financial product adapted as an afterthought.

### Key Value Propositions

| For Merchants | For Customers | For Platform Partners |
|---|---|---|
| Close more sales by removing buyer doubt. | Pay with confidence — money is released only when you confirm delivery. | Add escrow protection without building it from scratch. |
| Clear, capped fees with no hidden FX costs. | Track every naira from payment to release. | REST API + webhooks for fast integration. |
| Get notified the moment a buyer funds an escrow. | Raise a dispute with evidence and a 48-hour resolution SLA. | White-label ready, data-residency friendly. |
| Payouts to your Nigerian bank account within 24 hours. | Automatic refund path if a dispute resolves in your favor. | Reduce marketplace fraud and chargeback risk. |

### Brand Voice

- **Trust-first and calm.** Money is emotional. Assurra speaks slowly, clearly, and never uses hype.
- **African, not apologetic.** We say "pay with your Nigerian bank account" plainly. We reference local realities — slow networks, small data bundles, familiar payment habits — as strengths, not caveats.
- **Conversational where appropriate.** Headlines can be warm: "Let Assurra hold the money while you focus on the deal." Body copy stays precise and scannable.

---

## 4. Target Personas

### Primary Persona — The Independent Merchant

**Chinwe**, 34, runs a WhatsApp-based fashion store in Lagos. Customers ask "How do I know you will send what I paid for?" She loses sales because buyers do not trust prepayment. She wants a simple way to prove she will deliver.

**Needs:**
- Fast merchant KYC and onboarding.
- A shareable payment link she can send to buyers.
- Clear fee structure.
- Fast payout to her Nigerian bank account.

**Marketing message:** *"Show buyers you can be trusted — let Assurra hold the payment until delivery is confirmed."*

### Secondary Persona — The Cautious Customer

**Emeka**, 28, buys electronics from Instagram sellers across Nigeria. He has been ghosted once after payment. He wants a way to pay that protects him if the item is wrong, damaged, or never arrives.

**Needs:**
- Simple explanation of how escrow protects him.
- Visible transaction status and countdown.
- Easy dispute filing with evidence upload.
- Refund guarantee if the seller does not deliver.

**Marketing message:** *"Pay with confidence. Assurra releases your money only when you confirm you got what you paid for."*

### Tertiary Persona — The Platform Integrator

**Ade**, CTO of a B2B marketplace in Kenya/Nigeria. His platform needs escrow but building it in-house would take months and require compliance expertise.

**Needs:**
- Clear API documentation and use-case pages.
- Webhook reliability and retry guarantees.
- Security and compliance trust signals.
- Sandbox/testing path.

**Marketing message:** *"Add escrow to your platform in days, not months."*

---

## 5. Sitemap

| Path | Page Name | Primary Goal | Primary CTA |
|---|---|---|---|
| `/` | Home | Explain Assurra in 10 seconds and drive action. | "Create a Free Escrow" / "Become a Merchant" |
| `/services` | Services | Map escrow use cases to audience segments. | "Get Started" |
| `/how-it-works` | How It Works | Walk through the 5-step escrow lifecycle visually. | "Start an Escrow" |
| `/pricing` | Pricing | Completely transparent fee calculator and table. | "Create Account" |
| `/for-merchants` | For Merchants | Deep merchant value proposition and onboarding promise. | "Become a Merchant" |
| `/for-customers` | For Customers | Customer protection, dispute process, and trust. | "Learn How You Are Protected" |
| `/developers` | Developers / API | API-first positioning, quick-start, endpoint overview. | "Read API Docs" |
| `/security` | Security & Compliance | Trust signals: encryption, NDPR, audit logs, fund safety. | "View Our Security Practices" |
| `/data-saver` | Data Saver | Explain and demonstrate Assurra's low-bandwidth mode. | "Try Data Saver" |
| `/faq` | FAQ | Answer merchant, customer, and technical questions. | "Create an Escrow" |
| `/contact` | Contact | Sales, support, and partnership inquiries. | "Send Message" |
| `/legal/privacy` | Privacy Policy | NDPR/GDPR-compliant privacy notice. | — |
| `/legal/terms` | Terms of Service | User agreement, fees, dispute terms. | — |

### Optional Post-Launch Pages

- `/blog` — case studies, trust education, fraud prevention tips.
- `/about` — company story, team, mission.
- `/case-studies` — merchant success stories once testimonials exist.

---

## 6. Messaging Matrix

### Home Page Messaging

| Element | Copy Direction |
|---|---|
| Headline | "Escrow that works the way African commerce works." |
| Subheadline | "Assurra holds payments safely between buyers and sellers — in naira, on Nigerian bank accounts, with clear status every step of the way." |
| Merchant CTA | "Sell with trust — become a merchant" |
| Customer CTA | "Pay safely — create an escrow" |
| Trust bar | "Bank-grade encryption | 48-hour dispute SLA | Built for slow networks" |

### For Merchants Page Messaging

| Element | Copy Direction |
|---|---|
| Headline | "Turn hesitation into sales." |
| Subheadline | "When buyers trust the payment process, they buy faster. Assurra gives your customers confidence without slowing you down." |
| Proof points | 1. Shareable escrow links for WhatsApp, Instagram, email. 2. Pay-out to your Nigerian bank within 24 hours. 3. Clear fees: no surprises. |
| CTA | "Start selling with Assurra" |

### For Customers Page Messaging

| Element | Copy Direction |
|---|---|
| Headline | "Pay online without the worry." |
| Subheadline | "Assurra releases your money only when you confirm delivery. If something goes wrong, you have a clear path to dispute and refund." |
| Proof points | 1. Your money is held safely during the transaction. 2. Track every step from payment to release. 3. Disputes reviewed within 48 hours. |
| CTA | "Find a seller using Assurra" |

### Developers Page Messaging

| Element | Copy Direction |
|---|---|
| Headline | "Add escrow to your platform in days." |
| Subheadline | "One REST API. Webhooks for every state change. Sandbox ready. Built for African marketplaces, lenders, and procurement tools." |
| Proof points | 1. OpenAPI spec and generated SDKs. 2. HMAC-signed webhooks with automatic retries. 3. Data-residency friendly deployment. |
| CTA | "Explore the API" |

---

## 7. Page Requirements

### 7.1 Home (`/`)

**Purpose:** Convert. Explain the product and drive the user toward the right entry point.

**Required sections:**
1. **Hero** — headline, subheadline, two primary CTAs (merchant-first), hero image/illustration of a phone showing an escrow timeline.
2. **Trust bar** — 3 to 4 trust signals with icons (secure funds, 48h dispute SLA, bank transfer payouts, data-saver).
3. **How it works teaser** — 4-step visual summary with a "See full process" CTA.
4. **Audience cards** — "I'm a merchant", "I'm a customer", "I'm building a platform" — each links to its page.
5. **Feature highlight** — escrow timeline/status visualization.
6. **Fee transparency block** — "1.5%, minimum ₦500, maximum ₦50,000. No hidden FX fees."
7. **Data-saver badge** — "Assurra works on slow networks. Tap to try data-saver mode."
8. **Final CTA** — merchant-focused primary, customer-focused secondary.

**Metadata:**
- Title: `Assurra — Escrow for African Commerce`
- Description: `Hold payments safely between buyers and sellers in Nigeria. Assurra releases funds only when delivery is confirmed. Fast, transparent, and built for African commerce.`
- JSON-LD: Organization + WebSite + SoftwareApplication

### 7.2 Services (`/services`)

**Purpose:** Map Assurra to real use cases.

**Required sections:**
1. **Page hero** — "Use cases built for Nigerian commerce."
2. **Use-case grid** — E-commerce, freelance services, B2B procurement, vehicle/ asset sales, marketplace transactions.
3. **Per-use-case cards** — problem, Assurra solution, outcome.
4. **Audience section** — merchant, customer, integrator links.
5. **CTA** — "Get started with Assurra."

**Metadata:**
- Title: `Services — Assurra`
- Description: `Escrow protection for e-commerce, freelancing, B2B procurement, marketplaces, and more.`

### 7.3 How It Works (`/how-it-works`)

**Purpose:** Reduce uncertainty by showing the exact escrow lifecycle.

**Required sections:**
1. **Page hero** — "Five simple steps. Full transparency."
2. **Step timeline** — vertical or horizontal timeline:
   1. Buyer and seller agree on terms.
   2. Buyer creates an escrow and funds it.
   3. Seller ships or delivers the item.
   4. Buyer confirms delivery — or raises a dispute.
   5. Funds are released to the seller or refunded to the buyer.
3. **Status glossary** — show the core statuses: INITIATED, FUNDED, SHIPPED, DELIVERED, CONFIRMED, RELEASED, DISPUTED, REFUNDED.
4. **72-hour auto-release explanation** — "If the buyer does not respond within 72 hours, funds auto-release to the seller."
5. **Dispute path explanation** — "If something is wrong, either party can raise a dispute. Admins review within 48 hours."
6. **CTA** — "Try it with your next transaction."

**Metadata:**
- Title: `How It Works — Assurra`
- Description: `See exactly how Assurra holds, tracks, and releases payments between buyers and sellers.`

### 7.4 Pricing (`/pricing`)

**Purpose:** Remove fee uncertainty entirely.

**Required sections:**
1. **Page hero** — "Simple, capped fees."
2. **Fee formula** — 1.5% of transaction amount, ₦500 minimum, ₦50,000 maximum. Fee paid by buyer at funding.
3. **Interactive calculator** — input amount, show fee + total in real time.
4. **Fee table** — example amounts and corresponding fees.
5. **Merchant note** — custom enterprise fees for high-volume merchants (admin-configured).
6. **CTA** — "Create a free account."

**Metadata:**
- Title: `Pricing — Assurra`
- Description: `Transparent escrow fees: 1.5%, minimum ₦500, maximum ₦50,000. No hidden charges.`
- JSON-LD: Product/Offer

### 7.5 For Merchants (`/for-merchants`)

**Purpose:** Convince sellers that Assurra increases conversion and protects their cash flow.

**Required sections:**
1. **Hero** — merchant-focused headline and CTA.
2. **Problem/solution blocks** — "Buyers hesitate", "Assurra removes the doubt", "You ship with confidence".
3. **Feature list** — payment links, instant funding notifications, payout tracking, API keys, webhook notifications.
4. **Onboarding promise** — "KYC review within 24 hours; start accepting escrow same day."
5. **Fee reminder** — transparent pricing link.
6. **Testimonial placeholder** — "Merchant story coming soon" with a CTA to share yours.
7. **CTA** — "Become a merchant today."

**Metadata:**
- Title: `For Merchants — Assurra`
- Description: `Increase buyer trust and close more sales with Assurra escrow for Nigerian merchants.`

### 7.6 For Customers (`/for-customers`)

**Purpose:** Convince buyers that escrow is the safest way to pay online strangers.

**Required sections:**
1. **Hero** — customer-focused headline and CTA.
2. **Protection promise** — "Your money is held until you confirm."
3. **What happens if something goes wrong** — dispute flow, 48h SLA, refund path.
4. **What you need to do** — confirm within 72 hours to avoid auto-release.
5. **Common scenarios** — electronics, fashion, services, B2B deposits.
6. **CTA** — "Learn how to request Assurra."

**Metadata:**
- Title: `For Customers — Assurra`
- Description: `Pay online with confidence. Assurra holds your money until you confirm delivery, with a clear dispute and refund process.`

### 7.7 Developers (`/developers`)

**Purpose:** Position Assurra as the embeddable escrow engine.

**Required sections:**
1. **Hero** — API-first headline.
2. **Quick-start code snippet** — show a minimal escrow creation example.
3. **Endpoint overview** — authentication, escrow lifecycle, payments, disputes, webhooks.
4. **Webhook reliability** — retry policy, HMAC signature verification, event types.
5. **Security and compliance** — API key management, encryption, audit logs.
6. **Sandbox CTA** — "Get API keys."
7. **Link to full API docs** — hosted separately, not marketing scope.

**Metadata:**
- Title: `Developers — Assurra API`
- Description: `Add escrow to your platform with the Assurra REST API and webhooks. Built for African marketplaces and fintechs.`

### 7.8 Security (`/security`)

**Purpose:** Build institutional trust.

**Required sections:**
1. **Hero** — "Your money and data are protected."
2. **Fund safety** — held in neutral escrow account via licensed Interswitch partner.
3. **Encryption** — AES-256-GCM at rest, TLS 1.2+ in transit, hashed API keys.
4. **Identity** — KYC verification, BVN encryption, role-based access.
5. **Compliance** — NDPR/GDPR consent, data export and erasure rights, audit logs.
6. **Dispute fairness** — admin-reviewed, evidence-based, 48-hour SLA.
7. **CTA** — "Read our privacy policy" / "Contact compliance."

**Metadata:**
- Title: `Security & Compliance — Assurra`
- Description: `Bank-grade encryption, NDPR compliance, immutable audit logs, and a 48-hour dispute resolution SLA.`

### 7.9 Data Saver (`/data-saver`)

**Purpose:** Turn a technical accessibility feature into a marketing differentiator.

**Required sections:**
1. **Hero** — "Assurra works even when your network does not."
2. **Explanation** — what data-saver mode does (reduces images, disables non-essential motion, compresses assets).
3. **Live toggle demo** — user can toggle the page into data-saver mode and see the difference.
4. **Statistics** — average page weight with/without data-saver.
5. **Promise** — "Every Assurra page works in data-saver mode, including the dashboard."
6. **CTA** — "Try Assurra in data-saver mode."

**Metadata:**
- Title: `Data Saver — Assurra`
- Description: `Assurra is built for slow networks and small data bundles. Toggle data-saver mode and keep using escrow.`

### 7.10 FAQ (`/faq`)

**Purpose:** Answer objections and support queries before signup.

**Required sections:**
1. **Hero** — "Questions? Answered."
2. **Grouped accordion** by topic:
   - General escrow questions
   - Merchant questions
   - Customer questions
   - Fees and payouts
   - Disputes and refunds
   - API and integrations
3. **Search/filter** — optional, post-launch.
4. **CTA** — "Still have questions? Contact support."

**Metadata:**
- Title: `FAQ — Assurra`
- Description: `Frequently asked questions about Assurra escrow fees, payouts, disputes, refunds, and integration.`
- JSON-LD: FAQPage

### 7.11 Contact (`/contact`)

**Purpose:** Capture sales and support inquiries.

**Required sections:**
1. **Hero** — "Talk to us."
2. **Contact form** — name, email, phone, inquiry type (sales/support/partnership), message.
3. **Contact options** — email, phone, business hours (Lagos/WAT).
4. **Office/region note** — Nigeria-first operations.
5. **CTA** — "Send message."

**Metadata:**
- Title: `Contact — Assurra`
- Description: `Get in touch with Assurra sales, support, or partnerships.`

### 7.12 Legal Pages

**Privacy Policy (`/legal/privacy`)**
- NDPR/GDPR-compliant privacy notice.
- Data collection, use, retention, rights, cookies, third parties.
- Last updated date.

**Terms of Service (`/legal/terms`)**
- User agreement, eligibility, escrow terms, fee schedule, dispute terms, liability limits, termination.
- Last updated date.

---

## 8. Content Management

All marketing copy is **hand-authored**, not AI-generated. Content lives in:

```
frontend/content/
├── pages/
│   ├── home.mdx
│   ├── services.mdx
│   ├── how-it-works.mdx
│   ├── pricing.mdx
│   ├── for-merchants.mdx
│   ├── for-customers.mdx
│   ├── developers.mdx
│   ├── security.mdx
│   ├── data-saver.mdx
│   ├── faq.mdx
│   ├── contact.mdx
│   └── legal/
│       ├── privacy.mdx
│       └── terms.mdx
├── shared/
│   ├── trust-bar.json
│   ├── footer.json
│   └── navigation.json
```

### Content Rules

1. **Plain English first.** Avoid jargon until necessary; explain escrow to first-time users.
2. **Money is always in `₦` major units.** Use `Intl.NumberFormat('en-NG', { style: 'currency', currency: 'NGN' })`.
3. **Time is Africa/Lagos.** All SLA references use WAT or clear working-day language.
4. **References are trust anchors.** Mention `ESC-` reference prefixes where relevant.
5. **No fake testimonials.** Use placeholder blocks with a clear collection timeline.
6. **No competitor comparisons by name.** Positioning is expressed through value, not contrast.

### Testimonial Placeholder Strategy

Until real testimonials are collected, every page that would feature a quote uses a **"Merchant story coming soon"** card with:
- A brief prompt: *"Are you selling with Assurra? Share your story."*
- A link to `/contact` with subject pre-filled.
- A target date for first stories to be published (post-launch, Week 8).

---

## 9. Design & UX Requirements

### 9.1 Visual Personality — "Calm Ledger"

Assurra should feel like a bank you can trust and a neighbor you can talk to.

- **Trust.** Large whitespace, clear hierarchy, no visual noise.
- **Clarity.** Every screen answers "Where is my money?"
- **African-first.** Local payment cues, naira formatting, Lagos time, data-saver pride.
- **Professional warmth.** Green means progress; red is reserved for loss/error states.

### 9.2 Colors

Use the approved design-system palette exactly. The palette is derived from the Assurra logo greens (`#0B4011`, `#083B0E`, `#06350B`, `#7ECE6C`, `#76C766`) plus warm near-white backgrounds:

**Light mode:**
- Background: `#F6F8F6`
- Surface: `#FFFFFF`
- Primary: `#11832B`
- Primary strong: `#0D6B21`
- Primary soft: `#E8F3EA`
- Primary deep (brand sections): `#0A3D16`
- Success: `#15803D`
- Danger: `#B91C1C`
- Warning: `#B45309`
- Info: `#1D4ED8`
- Text: `#0F2E1A`
- Text muted: `#4A5D4E`
- Text faint: `#6B7D6F`
- Border: `#DEE6DE`

**Dark mode:**
- Background: `#0A0F0B`
- Surface: `#111812`
- Border: `#1F2920`
- Foreground: `#E6F0E8`
- Muted: `#9DB3A1`
- Faint: `#6F8573`
- Primary: `#3CC95A`
- Primary strong: `#54D86F`
- Primary soft: `#0F2918`
- Primary deep: `#062B10`
- Success: `#4ADE80`, Danger: `#F87171`, Warning: `#FBBF24`, Info: `#60A5FA`

### 9.3 Typography

- **Body font:** Inter Variable, subsetted for Latin.
- **Display font:** Fraunces (serif) for `h1`–`h2` headings and hero numerals — gives Assurra an editorial, hand-set feel distinct from typical SaaS templates.
- **Fallbacks:** `system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Arial, sans-serif`; display falls back to Georgia.
- **Scale:** 12/14/16/20/24/32/48/60/72px.
- **Money figures:** `font-variant-numeric: tabular-nums`, weight 700–800, largest figure on the page on pricing.
- **Line height:** 1.5 body, 1.25 headings.

### 9.4 Spacing & Layout

- 4px base grid.
- Touch targets minimum 44 × 44 px.
- Container max-widths: 480 mobile, 720 tablet, 1200 desktop.
- Breakpoints: 480, 768, 1024, 1280 px.

### 9.5 Animation

- **Default:** CSS transitions only.
- **Motion library:** Lazy-loaded Motion (`m`) for trust-critical moments only: escrow timeline state transitions, pricing calculator reveal, data-saver toggle.
- **No decorative scroll-linked or parallax motion.**
- **Respect `prefers-reduced-motion`.**

### 9.6 Icons

- **Phosphor icon family only.**
- **Weights:** regular for body, bold for navigation/active states, duotone for money/status icons.
- **Whitelist duotone icons** to ≤ 8 total.
- Sizes: 16, 20, 24 px.

### 9.7 Data-Saver Mode — Marketing Behavior

Data-saver is a **visible feature**, not just a performance implementation.

**Marketing surfaces:**
- Badge in hero/trust bar: "Works on slow networks — tap for data-saver."
- Dedicated `/data-saver` page explaining the feature.
- Footer toggle to enable/disable data-saver globally.

**Behavior when enabled:**
- Images load at lower resolution via Cloudinary `q_auto:low,w_640`.
- All non-essential animations disabled.
- Decorative illustrations replaced with CSS iconography where possible.
- Video backgrounds removed.
- Heavy components deferred or hidden.
- Preference persisted in `localStorage` and respected across marketing and dashboard.

**Implementation note:** Data-saver state is read by the layout and passed down via CSS class or context. No user authentication is required.

---

## 10. Technical Requirements

### 10.1 Framework

- **Next.js App Router** with route group `app/(marketing)/`.
- All marketing pages are **statically generated at build time** (`export const dynamic = 'force-static'` where appropriate).
- **On-demand revalidation** via route handler at `/api/revalidate` triggered by content publishes.
- `output: 'standalone'` for docker deployment.
- `typedRoutes` enabled.

### 10.2 Rendering Strategy

| Page Type | Strategy |
|---|---|
| Marketing pages | SSG at build time + on-demand ISR |
| `/pricing` calculator | Client-side state only; no server data needed |
| `/data-saver` toggle | Client-side state persisted to `localStorage` |
| `/contact` form | Client-side form with validation; submission to backend via API route or gateway |
| Legal pages | SSG |

### 10.3 Metadata

Every page must define:
- `title`
- `description`
- `canonical` URL
- Open Graph: `og:title`, `og:description`, `og:type`, `og:url`, `og:image`
- Twitter Card: `twitter:title`, `twitter:description`, `twitter:image`, `twitter:card`

### 10.4 Structured Data (JSON-LD)

- Home: `WebSite`, `Organization`, `SoftwareApplication`
- Pricing: `Product` or `Offer`
- FAQ: `FAQPage`
- How It Works: `HowTo`
- Contact: `Organization` / `ContactPoint`

### 10.5 Sitemap & Robots

- `app/sitemap.ts` generating `/sitemap.xml` from the page list.
- `app/robots.ts` allowing all pages except legal variants if needed.

### 10.6 Analytics & Consent

- **PostHog** and **Sentry** are loaded only after NDPR cookie consent.
- Consent banner appears on first visit.
- Essential cookies only until consent given.
- Link to `/legal/privacy` from banner.

### 10.7 Images

- Source from Cloudinary when brand assets are ready.
- Use `next/image` with explicit `width`/`height` and `aspect-ratio`.
- Lazy load below-the-fold images.
- Cloudinary params: `f_auto,q_auto,w_`, `dpr_1`.

---

## 11. SEO & Performance Requirements

### 11.1 Performance Budgets

| Metric | Target | Measurement |
|---|---|---|
| Largest Contentful Paint (LCP) | < 1.5s on mid-range Android | Lighthouse / Playwright |
| First Contentful Paint (FCP) | < 1.0s | Lighthouse |
| Cumulative Layout Shift (CLS) | < 0.1 | Lighthouse |
| Total Blocking Time (TBT) | < 200ms | Lighthouse |
| Initial JS size | ≤ 170 KB gzipped | Bundle analyzer |
| Marketing page total | ≤ 350 KB gzipped | Bundle analyzer |

### 11.2 SEO Targets

- Canonical URLs on every page.
- No duplicate content between `/` and `/home`.
- Descriptive alt text on all images.
- Internal linking between related pages.
- Semantic HTML (`main`, `article`, `section`, `nav`, `footer`).
- H1 per page, logical heading hierarchy.

### 11.3 Search Keywords (content guidance, not guarantee)

Primary: `escrow Nigeria`, `escrow service Nigeria`, `secure payment Nigeria`, `escrow for merchants Nigeria`, `pay safely online Nigeria`.

Secondary: `escrow API Nigeria`, `marketplace escrow Africa`, `B2B escrow Nigeria`, `freelance escrow Nigeria`, `dispute resolution payments`.

---

## 12. Accessibility & Compliance

### 12.1 Accessibility

- WCAG 2.1 AA compliance.
- Skip-to-content link.
- Focus-visible rings on interactive elements.
- Keyboard-navigable menus, accordions, and modals.
- ARIA labels on icon-only buttons.
- Color is never the sole indicator of status.
- Reduced-motion support.

### 12.2 NDPR / GDPR Compliance

- Cookie consent banner on first visit.
- Consent-gated analytics and error tracking.
- Privacy policy linked from footer and consent banner.
- No marketing trackers before consent.

### 12.3 Trust Signals on Every Page

- Footer must include: Privacy, Terms, Security, Contact.
- Footer must show: "Funds held via licensed Interswitch partner" or equivalent regulator-friendly statement.
- Fraud prevention cue: "Assurra will never ask for your PIN or BVN by phone or email."

---

## 13. Component / Section Inventory

| Component | Purpose | Pages Used |
|---|---|---|
| `MarketingShell` | Layout: nav, footer, consent banner | All marketing |
| `MarketingNav` | Sticky header with audience links | All marketing |
| `MarketingFooter` | Legal links, contact, data-saver toggle | All marketing |
| `HeroSection` | Headline, subheadline, CTAs, hero visual | Home, For Merchants, For Customers, Developers, Data Saver |
| `TrustBar` | 3–4 icon + text trust signals | Home, For Merchants, For Customers |
| `AudienceCardGroup` | Merchant / Customer / Developer links | Home, Services |
| `EscrowTimeline` | Visual 5-step process | How It Works, Home teaser |
| `StatusGlossary` | Core escrow status chips | How It Works |
| `PricingCalculator` | Interactive fee calculator | Pricing |
| `FeatureGrid` | Icon + title + description blocks | For Merchants, For Customers, Developers |
| `UseCaseGrid` | Problem/solution/outcome cards | Services |
| `FaqAccordion` | Grouped FAQ with headings | FAQ |
| `ContactForm` | Validated contact form | Contact |
| `SecurityFeatureList` | Encryption/KYC/compliance blocks | Security |
| `DataSaverDemo` | Toggle + before/after stats | Data Saver |
| `CtaSection` | Final call-to-action block | Most pages |
| `TestimonialPlaceholder` | "Story coming soon" card | For Merchants, For Customers |
| `CookieConsentBanner` | NDPR consent | All marketing |
| `JsonLd` | Structured data injection | Per page |

---

## 14. Acceptance Criteria

### Build & Deployment

- [ ] All pages in sitemap are created and routable.
- [ ] `next build` passes without errors or warnings.
- [ ] `output: 'standalone'` docker image builds successfully.
- [ ] Sitemap and robots files are generated.

### Content

- [ ] All copy is hand-authored and reviewed.
- [ ] No AI-generated placeholder text remains.
- [ ] No fake testimonials; placeholders use approved "coming soon" pattern.
- [ ] All money values use `Intl.NumberFormat` in NGN.
- [ ] All SLA/time references use Lagos/WAT context.

### SEO

- [ ] Every page has unique title, description, canonical, OG, and Twitter metadata.
- [ ] JSON-LD structured data passes Google's Rich Results Test.
- [ ] Sitemap includes all public pages and excludes noindex pages.

### Design & UX

- [ ] Design matches "Calm Ledger" system and approved color palette.
- [ ] All pages are responsive at 480/768/1024/1280 px breakpoints.
- [ ] Touch targets are ≥ 44 × 44 px.
- [ ] Data-saver toggle works and persists across pages.

### Performance

- [ ] LCP < 1.5s on simulated mid-range Android.
- [ ] CLS < 0.1.
- [ ] Initial JS ≤ 170 KB gzipped.
- [ ] Images use Cloudinary optimization params.

### Accessibility

- [ ] WCAG 2.1 AA audit passes (axe / Storybook a11y addon).
- [ ] Focus order is logical.
- [ ] Reduced-motion preference is respected.

### Compliance

- [ ] Cookie consent banner shown on first visit.
- [ ] Analytics only load after consent.
- [ ] Privacy policy and terms pages exist and are linked.

---

## 15. Dependencies & Risks

### Dependencies

| Dependency | Owner | Due Date | Impact if Missing |
|---|---|---|---|
| Final brand assets (logo, illustrations) | Design/Marketing | Week 2 | Blocks hero and OG images |
| Cloudinary account and image pipeline | DevOps | Week 2 | Blocks optimized image delivery |
| Approved legal copy | Legal/Compliance | Week 4 | Blocks Privacy/Terms pages |
| PostHog/Sentry keys | DevOps | Week 3 | Blocks consent-gated analytics |
| First merchant testimonials | Marketing | Week 8 | Blocks testimonial replacement |
| API documentation host | Backend/DevRel | Week 5 | Blocks Developers page deep links |

### Risks

| Risk | Probability | Impact | Mitigation |
|---|---|---|---|
| Brand assets delayed | Medium | High | Use Phosphor iconography and CSS-based visuals as fallback |
| Legal review delays | Medium | High | Mark pages as draft and gate them with `noindex` |
| SEO targets not met | Medium | Medium | Build keyword-driven content plan; iterate after launch |
| Testimonials not collected | High | Medium | Keep placeholder cards; do not publish fake quotes |
| Data-saver UX feels cheap | Low | Medium | Design review; ensure toggle is elegant, not crude |

---

## 16. Appendix A — Data-Saver Specification

### User Story

> As a user on a slow or expensive network, I want Assurra to use less data so that I can browse and use escrow without draining my bundle.

### Marketing Copy

**Badge/Label:**
- Default: "Built for slow networks"
- Expanded: "Assurra uses less data when data-saver is on — images are smaller, motion is off, and pages stay fast."
- Toggle label: "Data Saver"

### Functional Behavior

When data-saver is enabled:

1. **Images:** Load via Cloudinary `q_auto:low,w_640,dpr_1`. Decorative images may be hidden.
2. **Animation:** Motion library imports are skipped; CSS transitions reduced to 0ms or minimal fades.
3. **Illustrations:** Complex SVG/Canvas illustrations replaced with lightweight Phosphor icon compositions.
4. **Components:** Optional non-critical sections (testimonial placeholders, decorative grids) collapse or hide.
5. **Dashboard continuity:** The data-saver flag is read by the dashboard shell when the user logs in.

### Persistence

- Stored in `localStorage` under key `assurra-data-saver`.
- Read at layout level; avoids flash of rich content by setting class on `html` before first paint.
- Synced across tabs via `storage` event.

### Analytics

- Track toggles in PostHog (consent-gated) to understand adoption and network context.

---

## 17. Appendix B — Page Metadata Template

Every marketing page must export a metadata object following this shape:

```ts
export const metadata: Metadata = {
  title: 'Page Title — Assurra',
  description: 'Concise, keyword-rich description under 160 characters.',
  alternates: { canonical: 'https://assurra.com/page-path' },
  openGraph: {
    title: 'Page Title — Assurra',
    description: 'Concise description.',
    url: 'https://assurra.com/page-path',
    siteName: 'Assurra',
    type: 'website',
    images: [{ url: 'https://assurra.com/og/default.png', width: 1200, height: 630 }],
  },
  twitter: {
    card: 'summary_large_image',
    title: 'Page Title — Assurra',
    description: 'Concise description.',
    images: ['https://assurra.com/og/default.png'],
  },
};
```

---

## 18. Revision History

| Version | Date | Author | Changes |
|---|---|---|---|
| 1.0.0 | August 2026 | Frontend Team | Initial marketing frontend PRD |

---

*End of document.*

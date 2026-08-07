# Assurra marketing site (`client`)

Public Next.js marketing frontend for Assurra (EaaS). Visual system follows the approved **Stitch** Assurra design project; product copy follows `Eaas/` business rules.

## Stack

- Next.js 15 (App Router) + TypeScript + Tailwind CSS v4
- Phosphor icons, Zustand (data-saver + consent)
- SSG-friendly marketing routes under `src/app/(marketing)`

## Setup

```bash
cd client
cp .env.example .env.local
npm install
npm run dev
```

Open [http://localhost:3000](http://localhost:3000).

## Environment

| Variable | Purpose |
|---|---|
| `NEXT_PUBLIC_SITE_URL` | Canonical site URL (SEO, sitemap) |
| `NEXT_PUBLIC_APP_URL` | Future dashboard/auth base for CTA deep-links |

Example CTAs resolve to `{NEXT_PUBLIC_APP_URL}/register/customer` and `.../register/merchant`.

## Scripts

- `npm run dev` — local development (Turbopack)
- `npm run build` — production build (`output: 'standalone'`)
- `npm run start` — serve production build
- `npm run lint` — ESLint

## Routes

`/`, `/services`, `/how-it-works`, `/pricing`, `/for-merchants`, `/for-customers`, `/developers`, `/security`, `/data-saver`, `/help`, `/legal/privacy`, `/legal/terms`

## Notes

- Marketing pages do **not** call live escrow/payment APIs.
- Data Saver toggle persists in local storage and strips decorative media/motion.
- Cookie banner stores analytics consent locally (NDPR-oriented shell).

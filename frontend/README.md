# Assurra Marketing Frontend

Public, SEO-optimized marketing website for Assurra — escrow for African commerce.

## Stack

- Next.js 14 (App Router, `output: 'standalone'`)
- TypeScript (strict)
- Tailwind CSS v3 with Calm Ledger design tokens (v4-ready CSS variables)
- Radix UI primitives + Phosphor icons
- Motion (lazy, trust-critical moments only)
- Storybook 8

## Getting started

```bash
npm install
cp .env.example .env.local
npm run dev
```

## Scripts

| Command | Purpose |
|---|---|
| `npm run dev` | Start dev server |
| `npm run build` | Production build |
| `npm run start` | Start production server |
| `npm run lint` | ESLint |
| `npm run typecheck` | TypeScript check |
| `npm run format` | Prettier write |
| `npm run storybook` | Storybook dev (port 6006) |
| `npm run build:storybook` | Build Storybook static site |

## Structure

```
app/(marketing)/   # All 13 public pages
content/           # Hand-authored copy (JSON data + MDX placeholders)
src/features/      # Marketing section components
src/shared/        # UI primitives, styles, lib, hooks, messages
src/widgets/       # Nav, footer, cookie consent, data-saver toggle
```

## Marketing pages

`/` · `/services` · `/how-it-works` · `/pricing` · `/for-merchants` · `/for-customers` · `/developers` · `/security` · `/data-saver` · `/faq` · `/contact` · `/legal/privacy` · `/legal/terms`

## Data-saver mode

A visible African-first feature. Toggle in the footer or on `/data-saver`; persisted in `localStorage` (`assurra-data-saver`) and applied to the `<html>` element before first paint.

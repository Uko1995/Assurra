---
name: Assurra
colors:
  background: '#f3fcf0'
  on-background: '#151d16'
  surface: '#f3fcf0'
  surface-dim: '#d3ddd1'
  surface-bright: '#f3fcf0'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#edf6ea'
  surface-container: '#e7f0e4'
  surface-container-high: '#e2ebdf'
  surface-container-highest: '#dce5d9'
  surface-variant: '#dce5d9'
  on-surface: '#151d16'
  on-surface-variant: '#3f4a3d'
  inverse-surface: '#2a322b'
  inverse-on-surface: '#eaf3e7'
  outline: '#6f7a6b'
  outline-variant: '#becab9'
  surface-tint: '#006e20'
  primary: '#00681d'
  on-primary: '#ffffff'
  primary-container: '#11832b'
  on-primary-container: '#e2ffdb'
  inverse-primary: '#75dd78'
  secondary: '#38693b'
  on-secondary: '#ffffff'
  secondary-container: '#b9f1b6'
  on-secondary-container: '#3e6f40'
  tertiary: '#585955'
  on-tertiary: '#ffffff'
  tertiary-container: '#71716d'
  on-tertiary-container: '#f8f7f2'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#91fa91'
  primary-fixed-dim: '#75dd78'
  on-primary-fixed: '#002205'
  on-primary-fixed-variant: '#005316'
  secondary-fixed: '#b9f1b6'
  secondary-fixed-dim: '#9dd49b'
  on-secondary-fixed: '#002106'
  on-secondary-fixed-variant: '#1f5125'
  tertiary-fixed: '#e3e3de'
  tertiary-fixed-dim: '#c7c7c2'
  on-tertiary-fixed: '#1b1c19'
  on-tertiary-fixed-variant: '#464744'
typography:
  display-lg:
    fontFamily: Playfair Display
    fontSize: 56px
    fontWeight: '700'
    lineHeight: 64px
    letterSpacing: -0.02em
  display-lg-mobile:
    fontFamily: Playfair Display
    fontSize: 36px
    fontWeight: '700'
    lineHeight: 44px
  headline-md:
    fontFamily: Playfair Display
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
  headline-sm:
    fontFamily: Playfair Display
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
  body-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 28px
  body-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  label-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: 0.05em
  label-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
rounded:
  DEFAULT: 0.25rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  container-max: 1280px
  gutter: 24px
  margin-mobile: 16px
  stack-sm: 8px
  stack-md: 16px
  stack-lg: 32px
  section-padding: 80px
---

## Brand & Style
The design system for this product is built on the pillars of **security, transparency, and cultural resonance**. It targets Nigerian entrepreneurs, merchants, and consumers who require a dependable bridge for high-stakes transactions.

The visual style is **Warm Minimalism**. It rejects the cold, sterile nature of global fintech in favor of a palette and texture set that feels grounded and premium. The aesthetic combines traditional trust markers (authoritative serifs, deep forest tones) with modern digital efficiency (spacious layouts, soft elevation). To reflect the Nigerian context, the design incorporates subtle, geometric pattern overlays inspired by traditional textiles, used at very low opacities (2-4%) to add rhythmic depth to surfaces without distracting from the functional UI.

## Colors
Structured tokens (source of truth) live in `theme.json` / HTML Tailwind config.

### Light & dark
Stitch design system reports `colorMode: LIGHT` with a dual-tone Material palette (`inverse_*`, `primary_fixed`). Screen HTML enables Tailwind `darkMode: "class"` and applies `dark:` utilities on chrome. The Next.js client:
- keeps light tokens on `:root`
- remaps semantic CSS variables under `html.dark` (see `theme.json` → `dark`)
- exposes Light / Dark / System via the header theme toggle

Narrative accents:
- **Primary container / CTA fill**: Deep Emerald (#11832B)
- **Primary text / borders**: #00681D
- **Override secondary (deep headers)**: Forest Night (#083D14)
- **Funded badge**: muted gold

## Typography
- **Headings / display**: Playfair Display
- **UI & body / labels**: Plus Jakarta Sans
- Pair role classes: `font-display-lg text-display-lg`, `font-headline-md text-headline-md`, etc.

## Layout & Spacing
12-column fixed grid for desktop (max 1280px). 8px base unit. Section padding 80px. `stack-sm` 8px, `stack-md` 16px, `stack-lg` 32px, `gutter` 24px.

## Elevation & Depth
- Level 1 cards: `0px 4px 20px rgba(0, 33, 6, 0.04)` + 1px border
- Level 2 / hover: `0px 10px 30px rgba(0, 33, 6, 0.08)`

## Components

### Buttons
- **Primary**: `bg-primary-container text-on-primary-container`, 8px radius (`rounded-lg` in Stitch HTML config), `font-label-md text-label-md`, px-8 py-3/4
- **Secondary**: transparent + border primary
- **Nav CTA (Get Started)**: primary style with px-8

### Navigation Bar
Sticky top bar, `bg-surface/90 backdrop-blur-md`, glassmorphism. Brand wordmark Playfair `headline-sm` in primary. Links `body-md`. Active link bold + bottom border primary.

### Status Badges
- Funded: muted gold
- Released: light green + deep emerald text
- Disputed: soft red + maroon

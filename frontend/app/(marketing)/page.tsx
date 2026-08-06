import type { Metadata } from "next";
import { Container, Text } from "@/shared/ui";
import {
  HeroSection,
  TrustBar,
  AudienceCardGroup,
  EscrowTimeline,
  CtaSection,
  FeatureGrid,
} from "@/features/marketing/sections";
import { createMetadata, createJsonLd } from "@/shared/lib/seo";

export const metadata: Metadata = createMetadata({
  title: "Escrow for African Commerce",
  description:
    "Hold payments safely between buyers and sellers in Nigeria. Assurra releases funds only when delivery is confirmed. Fast, transparent, and built for African commerce.",
  path: "/",
});

const jsonLd = {
  "@context": "https://schema.org",
  "@type": "Organization",
  name: "Assurra",
  url: "https://assurra.com",
  description:
    "Escrow for African commerce. Hold payments safely between buyers and sellers until everyone is satisfied.",
  sameAs: [],
};

function EscrowReceiptCard() {
  const items = [
    { label: "Escrow ref", value: "ESC-2026-00814", tone: "default" },
    { label: "Amount held", value: "₦540,000", tone: "strong" },
    { label: "Status", value: "FUNDED", tone: "success" },
  ];

  return (
    <div className="relative mx-auto mt-48 w-full max-w-sm lg:absolute lg:right-8 lg:top-1/2 lg:mt-0 lg:-translate-y-1/2">
      <div className="absolute -inset-4 rounded-3xl bg-white/5" aria-hidden />
      <div className="relative rounded-2xl bg-surface p-24 shadow-lg">
        <div className="flex items-center justify-between border-b border-dashed border-border pb-16">
          <p className="overline-label text-primary">Assurra escrow</p>
          <span className="inline-block h-24 w-24 rounded-full bg-primary-soft text-center text-xs font-bold leading-24 text-primary">
            ₦
          </span>
        </div>
        <div className="space-y-16 py-16">
          {items.map((item) => (
            <div key={item.label} className="flex items-baseline justify-between gap-16">
              <Text size="sm" variant="muted">
                {item.label}
              </Text>
              <span
                className={
                  item.tone === "success"
                    ? "text-sm font-bold text-success"
                    : item.tone === "strong"
                      ? "text-lg font-extrabold tabular-nums text-primary"
                      : "text-sm font-semibold tabular-nums text-foreground"
                }
              >
                {item.value}
              </span>
            </div>
          ))}
        </div>
        <div className="border-t border-dashed border-border pt-16">
          <div className="flex items-center justify-between">
            <Text size="sm" variant="muted">
              Buyer confirmation window
            </Text>
            <span className="text-sm font-bold text-warning tabular-nums">64h 12m</span>
          </div>
        </div>
      </div>
      <p className="mt-16 text-center text-xs text-primary-soft">
        Every naira tracked from payment to release.
      </p>
    </div>
  );
}

export default function HomePage() {
  return (
    <>
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: createJsonLd(jsonLd) }}
      />
      <HeroSection
        variant="brand"
        overline="Escrow · Naira · Africa"
        title="Money, held safely. Deals, closed with trust."
        subtitle="Assurra holds payments between buyers and sellers — in naira, on Nigerian bank accounts, with clear status every step of the way."
        primaryCta={{ label: "Become a merchant", href: "/for-merchants" }}
        secondaryCta={{ label: "Create an escrow", href: "/for-customers" }}
      >
        <EscrowReceiptCard />
      </HeroSection>

      <TrustBar />

      <section className="bg-surface py-48 lg:py-64">
        <Container size="lg">
          <div className="mb-32 flex flex-col gap-16 md:flex-row md:items-end md:justify-between">
            <div>
              <p className="overline-label text-primary">How it works</p>
              <HeadingSection />
            </div>
          </div>
          <EscrowTimeline compact />
        </Container>
      </section>

      <AudienceCardGroup />
      <FeatureGrid
        title="Why merchants and buyers choose Assurra"
        features={[
          {
            title: "Funds held, not released",
            description:
              "Your money sits in a neutral escrow account until delivery is confirmed by the buyer.",
          },
          {
            title: "Visible at every step",
            description:
              "Clear statuses — funded, shipped, delivered, released — with copyable references.",
          },
          {
            title: "72-hour confirmation window",
            description:
              "Buyers get a full 72 hours to confirm receipt or raise a dispute. No snap decisions.",
          },
          {
            title: "48-hour dispute SLA",
            description:
              "Evidence-based human review, with a clear refund or release outcome within 48 hours.",
          },
          {
            title: "Naira-native payouts",
            description:
              "Payouts land in Nigerian bank accounts within 24 hours of release. No hidden FX fees.",
          },
          {
            title: "Light on data",
            description:
              "Data-saver mode keeps Assurra fast on slow networks and small data bundles.",
          },
        ]}
      />
      <CtaSection
        title="Ready to move money with confidence?"
        subtitle="Join merchants and buyers who trust Assurra to hold their payments safely."
        primaryCta={{ label: "Become a merchant", href: "/for-merchants" }}
        secondaryCta={{ label: "Learn how it works", href: "/how-it-works" }}
      />
    </>
  );
}

function HeadingSection() {
  return (
    <h2 className="font-display text-2xl font-semibold leading-snug tracking-[-0.01em] text-foreground">
      Five steps. Full transparency.
    </h2>
  );
}

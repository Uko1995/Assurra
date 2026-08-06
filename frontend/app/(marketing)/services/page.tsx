import type { Metadata } from "next";
import { HeroSection, UseCaseGrid, AudienceCardGroup, CtaSection } from "@/features/marketing/sections";
import { createMetadata } from "@/shared/lib/seo";
import useCases from "@content/pages/services.json";

export const metadata: Metadata = createMetadata({
  title: "Services",
  description:
    "Escrow protection for e-commerce, freelancing, B2B procurement, marketplaces, and more. Built for Nigerian commerce.",
  path: "/services",
});

export default function ServicesPage() {
  return (
    <>
      <HeroSection
        title="Use cases built for Nigerian commerce."
        subtitle="Wherever money meets trust, Assurra holds the middle — safely, transparently, and fast."
        primaryCta={{ label: "Get started", href: "/for-merchants" }}
        secondaryCta={{ label: "See pricing", href: "/pricing" }}
      />
      <UseCaseGrid
        title="Escrow protection, everywhere it matters"
        subtitle="From a single WhatsApp sale to a full marketplace, Assurra adapts to how you trade."
        cases={useCases.useCases}
      />
      <AudienceCardGroup />
      <CtaSection
        title="Find your use case on Assurra"
        subtitle="Tell us what you trade, and we'll show you how escrow keeps both sides safe."
        primaryCta={{ label: "Become a merchant", href: "/for-merchants" }}
        secondaryCta={{ label: "Contact sales", href: "/contact" }}
      />
    </>
  );
}

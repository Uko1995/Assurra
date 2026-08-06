import type { Metadata } from "next";
import {
  HeroSection,
  FeatureGrid,
  TrustBar,
  TestimonialPlaceholder,
  CtaSection,
} from "@/features/marketing/sections";
import { createMetadata } from "@/shared/lib/seo";
import merchantFeatures from "@content/pages/for-merchants.json";

export const metadata: Metadata = createMetadata({
  title: "For Merchants",
  description:
    "Increase buyer trust and close more sales with Assurra escrow for Nigerian merchants. Payouts to your bank within 24 hours.",
  path: "/for-merchants",
});

export default function ForMerchantsPage() {
  return (
    <>
      <HeroSection
        title="Turn hesitation into sales."
        subtitle="When buyers trust the payment process, they buy faster. Assurra gives your customers confidence without slowing you down."
        primaryCta={{ label: "Become a merchant today", href: "/for-merchants" }}
        secondaryCta={{ label: "See pricing", href: "/pricing" }}
      />
      <TrustBar />
      <FeatureGrid
        title="Everything you need to sell with trust"
        subtitle="Shareable links, instant alerts, and fast payouts — built for how Nigerian merchants actually sell."
        features={merchantFeatures.features}
      />
      <section className="bg-primary-soft py-32">
        <div className="container-lg text-center">
          <p className="text-lg font-medium text-primary">
            KYC review within 24 hours. Start accepting escrow-protected payments the same day.
          </p>
        </div>
      </section>
      <TestimonialPlaceholder />
      <CtaSection
        title="Sell with Assurra"
        subtitle="Join merchants who close more sales because buyers trust the payment process."
        primaryCta={{ label: "Become a merchant", href: "/for-merchants" }}
        secondaryCta={{ label: "Talk to sales", href: "/contact" }}
      />
    </>
  );
}

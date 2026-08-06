import type { Metadata } from "next";
import {
  HeroSection,
  FeatureGrid,
  EscrowTimeline,
  TestimonialPlaceholder,
  CtaSection,
} from "@/features/marketing/sections";
import { createMetadata } from "@/shared/lib/seo";
import customerFeatures from "@content/pages/for-customers.json";

export const metadata: Metadata = createMetadata({
  title: "For Customers",
  description:
    "Pay online with confidence. Assurra holds your money until you confirm delivery, with a clear dispute and refund process.",
  path: "/for-customers",
});

export default function ForCustomersPage() {
  return (
    <>
      <HeroSection
        title="Pay online without the worry."
        subtitle="Assurra releases your money only when you confirm delivery. If something goes wrong, you have a clear path to dispute and refund."
        primaryCta={{ label: "Learn how you're protected", href: "/how-it-works" }}
        secondaryCta={{ label: "Read the FAQ", href: "/faq" }}
      />
      <FeatureGrid
        title="Your money, protected from payment to delivery"
        subtitle="Every step of your purchase is visible, trackable, and reversible when things go wrong."
        features={customerFeatures.features}
      />
      <EscrowTimeline />
      <section className="bg-danger/5 py-32">
        <div className="container-lg text-center">
          <p className="text-lg font-medium text-foreground">
            Remember: <span className="font-bold text-danger">Assurra will never ask for your PIN or BVN</span>{" "}
            by phone or email. Report anyone who does.
          </p>
        </div>
      </section>
      <TestimonialPlaceholder />
      <CtaSection
        title="Ask for Assurra on your next online purchase"
        subtitle="When a seller uses Assurra, your money is never at risk until you say it is."
        primaryCta={{ label: "How it works", href: "/how-it-works" }}
        secondaryCta={{ label: "Contact support", href: "/contact" }}
      />
    </>
  );
}

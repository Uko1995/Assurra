import type { Metadata } from "next";
import { HeroSection, EscrowTimeline, StatusGlossary, CtaSection } from "@/features/marketing/sections";
import { Container, Heading, Text } from "@/shared/ui";
import { createMetadata } from "@/shared/lib/seo";

export const metadata: Metadata = createMetadata({
  title: "How It Works",
  description:
    "See exactly how Assurra holds, tracks, and releases payments between buyers and sellers in five transparent steps.",
  path: "/how-it-works",
});

export default function HowItWorksPage() {
  return (
    <>
      <HeroSection
        title="Five simple steps. Full transparency."
        subtitle="From agreement to payout, every stage of your escrow is visible and protected."
        primaryCta={{ label: "Start an escrow", href: "/for-customers" }}
        secondaryCta={{ label: "Become a merchant", href: "/for-merchants" }}
      />
      <EscrowTimeline />
      <StatusGlossary />
      <section className="bg-primary-soft py-48">
        <Container size="md" className="text-center">
          <Heading as="h2" size="xl" className="mb-16">
            What if the buyer doesn&apos;t respond?
          </Heading>
          <Text variant="muted">
            If a buyer does not confirm or dispute within 72 hours of delivery, the escrow
            auto-releases and the seller is paid. This protects merchants from buyers who
            disappear after receiving their goods.
          </Text>
        </Container>
      </section>
      <CtaSection
        title="Try it with your next transaction"
        subtitle="Set up your first escrow in minutes and see the status timeline yourself."
        primaryCta={{ label: "Create an escrow", href: "/for-customers" }}
        secondaryCta={{ label: "Read the FAQ", href: "/faq" }}
      />
    </>
  );
}

import type { Metadata } from "next";
import { HeroSection, SecurityFeatureList, CtaSection } from "@/features/marketing/sections";
import { Container, Heading, Text } from "@/shared/ui";
import { createMetadata } from "@/shared/lib/seo";

export const metadata: Metadata = createMetadata({
  title: "Security & Compliance",
  description:
    "Bank-grade encryption, NDPR compliance, immutable audit logs, and a 48-hour dispute resolution SLA.",
  path: "/security",
});

export default function SecurityPage() {
  return (
    <>
      <HeroSection
        title="Your money and data are protected."
        subtitle="Security is the foundation of escrow. Here's exactly how Assurra keeps funds safe and records honest."
        primaryCta={{ label: "Read our privacy policy", href: "/legal/privacy" }}
        secondaryCta={{ label: "Contact compliance", href: "/contact" }}
      />
      <SecurityFeatureList />
      <section className="bg-primary-soft py-48">
        <Container size="md" className="text-center">
          <Heading as="h2" size="xl" className="mb-16">
            Compliance built in from day one
          </Heading>
          <Text variant="muted">
            Assurra is designed around Nigerian regulation: KYC for every merchant, AML monitoring
            on large transactions, NDPR data rights, and records retained for the periods regulators
            require. PCI-DSS compliance is delegated to our licensed payment partner.
          </Text>
        </Container>
      </section>
      <CtaSection
        title="Trust the process"
        subtitle="Every escrow, every dispute, every payout — recorded, protected, and fair."
        primaryCta={{ label: "Become a merchant", href: "/for-merchants" }}
        secondaryCta={{ label: "Contact compliance", href: "/contact" }}
      />
    </>
  );
}

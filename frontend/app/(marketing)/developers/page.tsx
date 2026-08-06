import type { Metadata } from "next";
import { HeroSection, FeatureGrid, CtaSection } from "@/features/marketing/sections";
import { Container, Card, CardContent, Heading, Text } from "@/shared/ui";
import { createMetadata } from "@/shared/lib/seo";
import developerFeatures from "@content/pages/developers.json";

export const metadata: Metadata = createMetadata({
  title: "Developers",
  description:
    "Add escrow to your platform with the Assurra REST API and webhooks. Built for African marketplaces, lenders, and fintechs.",
  path: "/developers",
});

export default function DevelopersPage() {
  return (
    <>
      <HeroSection
        title="Add escrow to your platform in days."
        subtitle="One REST API. Webhooks for every state change. Sandbox ready. Built for African marketplaces, lenders, and procurement tools."
        primaryCta={{ label: "Read the API docs", href: "/developers" }}
        secondaryCta={{ label: "Get API keys", href: "/contact" }}
      />
      <FeatureGrid
        title="An escrow engine, not a bolt-on"
        subtitle="Contract-first, webhook-native, and built to keep your integration honest."
        features={developerFeatures.features}
      />
      <section className="bg-background py-48">
        <Container size="md">
          <Heading as="h2" size="xl" className="mb-24 text-center">
            Quick start
          </Heading>
          <Card>
            <CardContent>
              <pre className="overflow-x-auto rounded-md bg-[#0F172A] p-24 text-sm leading-relaxed text-[#E6EAF2]">
                <code>{`# 1. Create an escrow
POST /api/v1/escrows
{
  "merchantId": "mch_123",
  "amount": 50000,
  "currency": "NGN",
  "description": "Laptop — i5, 8GB RAM",
  "idempotencyKey": "your-uuid-here"
}

# 2. Listen for webhooks
escrow.funded    → ship the item
escrow.confirmed → release funds
escrow.disputed  → open evidence thread`}</code>
              </pre>
            </CardContent>
          </Card>
        </Container>
      </section>
      <CtaSection
        title="Build on Assurra"
        subtitle="Get sandbox credentials and start integrating today. Our team reviews integrations within one business day."
        primaryCta={{ label: "Request API keys", href: "/contact" }}
        secondaryCta={{ label: "Read the FAQ", href: "/faq" }}
      />
    </>
  );
}

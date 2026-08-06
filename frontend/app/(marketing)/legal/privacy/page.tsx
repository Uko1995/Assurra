import type { Metadata } from "next";
import { Container, Heading, Text } from "@/shared/ui";
import { createMetadata } from "@/shared/lib/seo";

export const metadata: Metadata = createMetadata({
  title: "Privacy Policy",
  description:
    "How Assurra collects, uses, and protects your personal data, in line with NDPR and GDPR requirements.",
  path: "/legal/privacy",
});

export default function PrivacyPage() {
  return (
    <div className="bg-background py-48">
      <Container size="md">
        <Heading as="h1" size="2xl" className="mb-8">
          Privacy Policy
        </Heading>
        <Text size="sm" variant="faint" className="mb-32">
          Last updated: August 2026
        </Text>

        <div className="space-y-32">
          <section>
            <Heading as="h2" size="lg" className="mb-16">
              1. Who we are
            </Heading>
            <Text variant="muted">
              Assurra (operated by EaaS) provides escrow services for African commerce. This policy
              explains what personal data we collect, why we collect it, and the rights you have
              over it.
            </Text>
          </section>

          <section>
            <Heading as="h2" size="lg" className="mb-16">
              2. Data we collect
            </Heading>
            <Text variant="muted">
              We collect account details (name, email, phone), KYC documents for merchants (ID,
              business registration, bank details, BVN), transaction records, and usage analytics
              when you consent.
            </Text>
          </section>

          <section>
            <Heading as="h2" size="lg" className="mb-16">
              3. How we use your data
            </Heading>
            <Text variant="muted">
              Your data is used to provide escrow services, verify identity, process payments and
              payouts, resolve disputes, meet legal obligations, and — with your consent — improve
              the product through analytics.
            </Text>
          </section>

          <section>
            <Heading as="h2" size="lg" className="mb-16">
              4. Data protection
            </Heading>
            <Text variant="muted">
              Bank details and BVN are encrypted at rest (AES-256-GCM). Data in transit is protected
              with TLS 1.2+. Access to sensitive data is role-limited and audited.
            </Text>
          </section>

          <section>
            <Heading as="h2" size="lg" className="mb-16">
              5. Your rights
            </Heading>
            <Text variant="muted">
              Under NDPR and GDPR you may request a copy of your data (data export), correction of
              inaccuracies, or erasure. Contact privacy@assurra.com to exercise these rights.
            </Text>
          </section>

          <section>
            <Heading as="h2" size="lg" className="mb-16">
              6. Cookies &amp; analytics
            </Heading>
            <Text variant="muted">
              We use consent-gated analytics. No non-essential cookies or trackers are loaded until
              you accept. You can change your choice at any time via the consent banner.
            </Text>
          </section>

          <section>
            <Heading as="h2" size="lg" className="mb-16">
              7. Contact
            </Heading>
            <Text variant="muted">
              Questions about this policy: privacy@assurra.com.
            </Text>
          </section>
        </div>
      </Container>
    </div>
  );
}

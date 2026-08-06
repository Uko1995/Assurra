import type { Metadata } from "next";
import { Container, Heading, Text } from "@/shared/ui";
import { createMetadata } from "@/shared/lib/seo";

export const metadata: Metadata = createMetadata({
  title: "Terms of Service",
  description:
    "The terms governing your use of Assurra, including escrow rules, fees, disputes, and liability.",
  path: "/legal/terms",
});

export default function TermsPage() {
  return (
    <div className="bg-background py-48">
      <Container size="md">
        <Heading as="h1" size="2xl" className="mb-8">
          Terms of Service
        </Heading>
        <Text size="sm" variant="faint" className="mb-32">
          Last updated: August 2026
        </Text>

        <div className="space-y-32">
          <section>
            <Heading as="h2" size="lg" className="mb-16">
              1. Agreement
            </Heading>
            <Text variant="muted">
              By creating an account or using Assurra, you agree to these terms. If you do not
              agree, do not use the service.
            </Text>
          </section>

          <section>
            <Heading as="h2" size="lg" className="mb-16">
              2. Eligibility
            </Heading>
            <Text variant="muted">
              You must be at least 18 years old and legally able to enter contracts. Merchants must
              complete KYC verification before accepting escrow-protected payments.
            </Text>
          </section>

          <section>
            <Heading as="h2" size="lg" className="mb-16">
              3. Escrow rules
            </Heading>
            <Text variant="muted">
              Funds are held in escrow until the buyer confirms delivery or a dispute is resolved.
              If the buyer takes no action within 72 hours of delivery, funds auto-release to the
              merchant. Disputes are reviewed by our team within 48 hours.
            </Text>
          </section>

          <section>
            <Heading as="h2" size="lg" className="mb-16">
              4. Fees
            </Heading>
            <Text variant="muted">
              The escrow fee is 1.5% of the transaction amount, minimum ₦500, maximum ₦50,000, paid
              by the buyer at funding. Custom fee configurations may apply to enterprise merchants.
            </Text>
          </section>

          <section>
            <Heading as="h2" size="lg" className="mb-16">
              5. Prohibited use
            </Heading>
            <Text variant="muted">
              You may not use Assurra for illegal transactions, money laundering, or fraud. We
              monitor transactions for AML patterns and report suspicious activity as required by
              law.
            </Text>
          </section>

          <section>
            <Heading as="h2" size="lg" className="mb-16">
              6. Liability
            </Heading>
            <Text variant="muted">
              Assurra acts as a neutral escrow agent and is not a party to your underlying
              transactions. Our liability is limited to the escrow funds held in relation to the
              disputed transaction, to the extent permitted by law.
            </Text>
          </section>

          <section>
            <Heading as="h2" size="lg" className="mb-16">
              7. Termination
            </Heading>
            <Text variant="muted">
              You may close your account at any time. Assurra may suspend or terminate accounts that
              violate these terms. Outstanding escrows are settled before account closure.
            </Text>
          </section>

          <section>
            <Heading as="h2" size="lg" className="mb-16">
              8. Changes
            </Heading>
            <Text variant="muted">
              We may update these terms. Material changes will be communicated by email and posted
              on this page with a new effective date.
            </Text>
          </section>

          <section>
            <Heading as="h2" size="lg" className="mb-16">
              9. Contact
            </Heading>
            <Text variant="muted">
              Questions about these terms: legal@assurra.com.
            </Text>
          </section>
        </div>
      </Container>
    </div>
  );
}

import type { Metadata } from "next";
import { HeroSection, PricingCalculator, CtaSection } from "@/features/marketing/sections";
import { Container, Card, CardContent, Heading, Text } from "@/shared/ui";
import { createMetadata } from "@/shared/lib/seo";

export const metadata: Metadata = createMetadata({
  title: "Pricing",
  description:
    "Transparent escrow fees: 1.5% per transaction, minimum ₦500, maximum ₦50,000. No hidden charges, no FX surprises.",
  path: "/pricing",
});

const feeExamples = [
  { amount: "₦10,000", fee: "₦500 (minimum)", total: "₦10,500" },
  { amount: "₦50,000", fee: "₦750", total: "₦50,750" },
  { amount: "₦500,000", fee: "₦7,500", total: "₦507,500" },
  { amount: "₦5,000,000", fee: "₦50,000 (maximum)", total: "₦5,050,000" },
];

export default function PricingPage() {
  return (
    <>
      <HeroSection
        title="Simple, capped fees."
        subtitle="One transparent fee. No hidden charges, no surprise deductions, no FX markups."
        primaryCta={{ label: "Create a free account", href: "/for-merchants" }}
        secondaryCta={{ label: "See how it works", href: "/how-it-works" }}
        centered
      />
      <PricingCalculator />
      <section className="bg-background py-48">
        <Container size="md">
          <Heading as="h2" size="xl" className="mb-24 text-center">
            Fee examples
          </Heading>
          <div className="grid gap-16 sm:grid-cols-2">
            {feeExamples.map((example) => (
              <Card key={example.amount}>
                <CardContent>
                  <div className="flex items-center justify-between">
                    <div>
                      <Text size="sm" variant="muted">
                        Transaction
                      </Text>
                      <p className="text-lg font-bold tabular-nums">{example.amount}</p>
                    </div>
                    <div className="text-right">
                      <Text size="sm" variant="muted">
                        Fee
                      </Text>
                      <p className="text-lg font-bold tabular-nums text-primary">{example.fee}</p>
                    </div>
                  </div>
                  <div className="mt-16 border-t border-border pt-16 text-center">
                    <Text size="sm" variant="muted">
                      Buyer pays
                    </Text>
                    <p className="text-xl font-extrabold tabular-nums">{example.total}</p>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
          <Text size="sm" variant="muted" className="mt-24 text-center">
            High-volume merchants can request custom fee configurations. Contact sales for
            enterprise pricing.
          </Text>
        </Container>
      </section>
      <CtaSection
        title="Know your costs before you start"
        subtitle="The fee is paid by the buyer at funding. Merchants receive their full payout."
        primaryCta={{ label: "Create a free account", href: "/for-merchants" }}
        secondaryCta={{ label: "Contact sales", href: "/contact" }}
      />
    </>
  );
}

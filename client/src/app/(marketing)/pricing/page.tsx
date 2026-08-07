import type { Metadata } from "next";
import { FeeCalculator } from "@/components/FeeCalculator";
import { PageHero } from "@/components/PageHero";
import { Button } from "@/components/ui/Button";
import { Section, SectionHeading } from "@/components/ui/Section";
import { pricingContent } from "@/content/pages";
import { appPath } from "@/lib/site";

export const metadata: Metadata = {
  title: "Pricing",
  description:
    "Transparent Assurra escrow fees: default 1.5%, customer pays product plus fee, merchant receives net.",
};

export default function PricingPage() {
  return (
    <>
      <PageHero
        eyebrow="Pricing"
        title={pricingContent.title}
        description={pricingContent.description}
        actions={<Button href={appPath("/register/merchant")}>Create account</Button>}
      />
      <Section>
        <div className="grid items-start gap-10 lg:grid-cols-[1fr_0.9fr]">
          <div>
            <SectionHeading
              title="Fee policy"
              description="Not a SaaS seat tax — a transparent per-escrow fee aligned with African commerce."
            />
            <ul className="space-y-3">
              {pricingContent.rules.map((rule) => (
                <li
                  key={rule}
                  className="border-b border-border-subtle pb-3 text-sm leading-6 text-on-surface"
                >
                  {rule}
                </li>
              ))}
            </ul>
          </div>
          <FeeCalculator />
        </div>
      </Section>
      <Section tone="deep">
        <SectionHeading
          invert
          title="Ready to secure your funds?"
          description="Join Nigerians trading safely with Assurra Escrow."
        />
        <Button href={appPath("/register/customer")} className="!bg-white !text-secondary">
          Start a transaction
        </Button>
      </Section>
    </>
  );
}

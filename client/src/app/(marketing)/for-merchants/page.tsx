import type { Metadata } from "next";
import { PageHero } from "@/components/PageHero";
import { Button } from "@/components/ui/Button";
import { Section, SectionHeading } from "@/components/ui/Section";
import { images, merchantGrowth, merchantHighlights } from "@/content/pages";
import { appPath } from "@/lib/site";

export const metadata: Metadata = {
  title: "For merchants",
  description:
    "Close more sales with Assurra escrow: KYC once, get funded alerts, ship with tracking, and receive Nigerian bank payouts.",
};

export default function ForMerchantsPage() {
  return (
    <>
      <PageHero
        eyebrow="For Nigerian Merchants"
        title="Scale your business with absolute trust"
        description="Eliminate payment-on-delivery risk and build credibility. Assurra protects your revenue and reassures customers — turning trust into a competitive advantage."
        image={images.merchantsHero}
        actions={
          <>
            <Button href={appPath("/register/merchant")}>Create merchant account</Button>
            <Button href="/pricing" variant="secondary">
              View pricing
            </Button>
          </>
        }
      />
      <Section>
        <SectionHeading
          title="Redefining Nigerian commerce"
          description="Move beyond risky payment methods. Secure transactions and streamline operations with modern escrow."
        />
        <div className="grid gap-8 md:grid-cols-2">
          {merchantHighlights.map((item) => (
            <article key={item.title} className="border-t border-border-subtle pt-6">
              <h2 className="font-serif text-xl font-semibold text-on-surface">{item.title}</h2>
              <p className="mt-3 text-sm leading-7 text-muted">{item.body}</p>
            </article>
          ))}
        </div>
        <p className="mt-8 text-sm text-muted">
          *KYC verification is required before any payouts can be processed.
        </p>
      </Section>
      <Section tone="muted">
        <SectionHeading
          title="Empower your growth"
          description="Trust is the currency of modern commerce. Assurra equips you to build it while you scale."
        />
        <div className="grid gap-8 md:grid-cols-2">
          {merchantGrowth.map((item) => (
            <article key={item.title}>
              <h2 className="font-serif text-xl font-semibold text-on-surface">{item.title}</h2>
              <p className="mt-3 text-sm leading-7 text-muted">{item.body}</p>
            </article>
          ))}
        </div>
        <div className="mt-10">
          <Button href="/developers" variant="secondary">
            Explore the API
          </Button>
        </div>
      </Section>
    </>
  );
}

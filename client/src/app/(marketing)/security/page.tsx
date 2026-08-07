import type { Metadata } from "next";
import { PageHero } from "@/components/PageHero";
import { Button } from "@/components/ui/Button";
import { Section, SectionHeading } from "@/components/ui/Section";
import { images, securityPoints } from "@/content/pages";

export const metadata: Metadata = {
  title: "Security",
  description:
    "Assurra security: fund custody, JWT auth, AES encryption, HMAC webhooks, KYC/AML, NDPR-aligned practices.",
};

export default function SecurityPage() {
  return (
    <>
      <PageHero
        eyebrow="Security"
        title="Bank-grade security, Nigerian regulation"
        description="Fort Knox for high-stakes transactions. Funds are held until confirmation, auto-release, or dispute resolution — with NDPR-aligned data handling."
        image={images.securityHero}
        actions={<Button href="/legal/privacy" variant="secondary">View privacy practices</Button>}
      />
      <Section>
        <div className="mb-10 flex flex-wrap gap-6 text-sm font-semibold uppercase tracking-[0.08em] text-primary">
          <span>CBN Regulated</span>
          <span>NDPR Compliant</span>
          <span>AES-256 at rest</span>
        </div>
        <SectionHeading
          title="Controls that match the stakes"
          description="From token lifetimes to payout eligibility, security is part of the escrow lifecycle — not a footnote."
        />
        <div className="grid gap-8 md:grid-cols-2 lg:grid-cols-3">
          {securityPoints.map((item) => (
            <article key={item.title} className="border-t border-border-subtle pt-6">
              <h2 className="font-serif text-xl font-semibold text-on-surface">{item.title}</h2>
              <p className="mt-3 text-sm leading-7 text-muted">{item.body}</p>
            </article>
          ))}
        </div>
      </Section>
    </>
  );
}

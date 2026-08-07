import type { Metadata } from "next";
import Link from "next/link";
import { ArrowRight } from "@phosphor-icons/react/dist/ssr";
import { PageHero } from "@/components/PageHero";
import { Button } from "@/components/ui/Button";
import { Section, SectionHeading } from "@/components/ui/Section";
import { serviceTruths, services } from "@/content/pages";
import { appPath } from "@/lib/site";

export const metadata: Metadata = {
  title: "Services",
  description:
    "Assurra escrow services for consumers, merchants, and platform partners across African commerce.",
};

export default function ServicesPage() {
  return (
    <>
      <PageHero
        eyebrow="Services"
        title="Secure transactions at every scale"
        description="From independent freelancers to enterprise e-commerce platforms, Assurra provides a trust layer for business dealings in Nigeria and beyond."
        actions={<Button href={appPath("/register/merchant")}>Get started</Button>}
      />
      <Section>
        <SectionHeading
          title="Built for every side of the transaction"
          description="Pick the path that matches your role. The same escrow lifecycle powers all of them."
        />
        <div className="grid gap-8 md:grid-cols-2">
          {services.map((item) => (
            <article key={item.title} className="border-t border-border-subtle pt-6">
              <h2 className="font-serif text-2xl font-semibold text-on-surface">{item.title}</h2>
              <p className="mt-3 text-base leading-7 text-muted">{item.body}</p>
              <Link
                href={item.href}
                className="mt-4 inline-flex items-center gap-1.5 text-sm font-semibold text-primary hover:underline"
              >
                {item.cta} <ArrowRight size={14} weight="bold" />
              </Link>
            </article>
          ))}
        </div>
      </Section>
      <Section tone="muted">
        <SectionHeading
          title="Technical truths"
          description="Built on infrastructure designed for reliable Naira escrow."
        />
        <div className="grid gap-8 md:grid-cols-3">
          {serviceTruths.map((item) => (
            <article key={item.title}>
              <h3 className="font-serif text-xl font-semibold text-on-surface">{item.title}</h3>
              <p className="mt-3 text-sm leading-7 text-muted">{item.body}</p>
            </article>
          ))}
        </div>
      </Section>
    </>
  );
}

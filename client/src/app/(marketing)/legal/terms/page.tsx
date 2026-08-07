import type { Metadata } from "next";
import { PageHero } from "@/components/PageHero";
import { Section } from "@/components/ui/Section";
import { termsSections } from "@/content/pages";

export const metadata: Metadata = {
  title: "Terms of Service",
  description: "Assurra terms covering escrow use, fees, confirmation windows, disputes, and merchant KYC.",
};

export default function TermsPage() {
  return (
    <>
      <PageHero
        eyebrow="Legal"
        title="Terms of Service"
        description="The rules of using Assurra escrow — fees, timers, KYC, and dispute outcomes."
      />
      <Section>
        <p className="mb-10 text-sm text-muted">Last updated: October 24, 2024</p>
        <div className="mx-auto max-w-3xl space-y-10">
          {termsSections.map((section) => (
            <article key={section.title}>
              <h2 className="font-serif text-2xl font-semibold text-on-surface">{section.title}</h2>
              <p className="mt-3 text-base leading-8 text-muted">{section.body}</p>
            </article>
          ))}
          <p className="text-sm text-muted">
            Draft for product alignment; final legal review required before launch.
          </p>
        </div>
      </Section>
    </>
  );
}

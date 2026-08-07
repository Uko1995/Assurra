import type { Metadata } from "next";
import { PageHero } from "@/components/PageHero";
import { Section } from "@/components/ui/Section";
import { privacySections } from "@/content/pages";

export const metadata: Metadata = {
  title: "Privacy Policy",
  description: "Assurra privacy practices aligned with NDPR principles for escrow and identity data.",
};

export default function PrivacyPage() {
  return (
    <>
      <PageHero
        eyebrow="Legal Documentation"
        title="Privacy Policy"
        description="How we collect, use, and protect personal data in accordance with the Nigerian Data Protection Regulation (NDPR) and other applicable laws."
      />
      <Section>
        <p className="mb-10 text-sm text-muted">
          Last updated: October 24, 2024 · Effective: November 1, 2024
        </p>
        <div className="mx-auto max-w-3xl space-y-10">
          {privacySections.map((section) => (
            <article key={section.title}>
              <h2 className="font-serif text-2xl font-semibold text-on-surface">{section.title}</h2>
              <p className="mt-3 text-base leading-8 text-muted">{section.body}</p>
            </article>
          ))}
          <p className="text-sm text-muted">
            Final counsel-approved language will replace this product-aligned draft before production launch.
          </p>
        </div>
      </Section>
    </>
  );
}

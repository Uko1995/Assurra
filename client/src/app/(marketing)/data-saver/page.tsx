import type { Metadata } from "next";
import { DataSaverToggle } from "@/components/DataSaverToggle";
import { PageHero } from "@/components/PageHero";
import { Section, SectionHeading } from "@/components/ui/Section";
import { dataSaverFacts, images } from "@/content/pages";

export const metadata: Metadata = {
  title: "Data Saver",
  description:
    "Assurra Data Saver reduces decorative media and motion for low-bandwidth browsing across Africa.",
};

export default function DataSaverPage() {
  return (
    <>
      <PageHero
        eyebrow="Data Saver"
        title="Fast and lightweight by design"
        description="Built for Nigerian connectivity. Assurra keeps high-stakes transactions usable on 2G/3G — optimizing every byte for reliability."
        image={images.dataSaverHero}
        actions={<DataSaverToggle />}
      />
      <Section>
        <SectionHeading
          title="What Data Saver changes"
          description="Essential content, navigation, and CTAs stay. Decorative imagery and entrance animations step aside."
        />
        <ul className="grid gap-8 md:grid-cols-3">
          {dataSaverFacts.map((item) => (
            <li key={item.title} className="border-t border-border-subtle pt-6">
              <h3 className="font-serif text-xl font-semibold text-on-surface">{item.title}</h3>
              <p className="mt-3 text-sm leading-7 text-muted">{item.body}</p>
            </li>
          ))}
        </ul>
        <p className="mt-10 text-sm text-muted">
          Preference persists in your browser via local settings. Toggle anytime from the header or this page.
        </p>
      </Section>
    </>
  );
}

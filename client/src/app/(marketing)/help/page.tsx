import type { Metadata } from "next";
import { PageHero } from "@/components/PageHero";
import { Button } from "@/components/ui/Button";
import { Section, SectionHeading } from "@/components/ui/Section";
import { helpCategories, helpFaqs, images } from "@/content/pages";
import { appPath, siteConfig } from "@/lib/site";

export const metadata: Metadata = {
  title: "Help Center",
  description: "Answers about Assurra escrow fees, confirmation windows, disputes, payouts, and APIs.",
};

export default function HelpPage() {
  return (
    <>
      <PageHero
        eyebrow="Help Center"
        title="How can we help you?"
        description="Browse categories or FAQs grounded in Assurra’s real product rules — fees, 72-hour windows, disputes, and payouts."
        image={images.helpHero}
        actions={<Button href={appPath("/register/customer")}>Create an escrow</Button>}
      />
      <Section>
        <SectionHeading title="Browse by topic" />
        <div className="grid gap-8 md:grid-cols-3">
          {helpCategories.map((cat) => (
            <article key={cat.title} className="border-t border-border-subtle pt-6">
              <h2 className="font-serif text-xl font-semibold text-on-surface">{cat.title}</h2>
              <ul className="mt-4 space-y-2 text-sm leading-7 text-muted">
                {cat.items.map((item) => (
                  <li key={item}>{item}</li>
                ))}
              </ul>
            </article>
          ))}
        </div>
      </Section>
      <Section tone="muted">
        <SectionHeading title="FAQ" description="Still stuck? Email us — no ticket maze for v1." />
        <div className="space-y-4">
          {helpFaqs.map((item) => (
            <details
              key={item.q}
              className="group border-b border-border-subtle pb-4"
            >
              <summary className="cursor-pointer list-none font-serif text-lg font-semibold text-on-surface marker:content-none">
                {item.q}
              </summary>
              <p className="mt-3 text-sm leading-7 text-muted">{item.a}</p>
            </details>
          ))}
        </div>
        <div className="mt-10 space-y-2 text-sm text-muted">
          <p>
            Support hours: Monday to Friday, 9am – 6pm WAT
          </p>
          <p>
            Contact:{" "}
            <a
              className="font-semibold text-primary hover:underline"
              href={`mailto:${siteConfig.contactEmail}`}
            >
              {siteConfig.contactEmail}
            </a>
          </p>
        </div>
      </Section>
    </>
  );
}

import type { Metadata } from "next";
import { PageHero } from "@/components/PageHero";
import { Button } from "@/components/ui/Button";
import { Section, SectionHeading } from "@/components/ui/Section";
import { customerHighlights, images } from "@/content/pages";
import { appPath } from "@/lib/site";

export const metadata: Metadata = {
  title: "For customers",
  description:
    "Pay with confidence through Assurra. Funds release only after you confirm delivery — or after a fair dispute.",
};

export default function ForCustomersPage() {
  return (
    <>
      <PageHero
        eyebrow="For customers"
        title="Shop online with zero risk"
        description="Assurra protects your money until you receive exactly what you ordered. If it’s not right, your money stays safe."
        image={images.customersHero}
        actions={
          <Button href={appPath("/register/customer")}>Create free account</Button>
        }
      />
      <Section>
        <SectionHeading
          title="How our protection works"
          description="A transparent process designed for peace of mind. Track every step of your transaction in real time."
        />
        <div className="grid gap-8 md:grid-cols-2">
          {customerHighlights.map((item) => (
            <article key={item.title} className="border-t border-border-subtle pt-6">
              <h2 className="font-serif text-xl font-semibold text-on-surface">{item.title}</h2>
              <p className="mt-3 text-sm leading-7 text-muted">{item.body}</p>
            </article>
          ))}
        </div>
      </Section>
      <Section tone="deep">
        <SectionHeading
          invert
          title="100% buyer protection mindset"
          description="Funds release only after you confirm — or after a dispute outcome in your favour."
        />
        <Button href="/how-it-works" className="!bg-white !text-secondary">
          See the full lifecycle
        </Button>
      </Section>
    </>
  );
}

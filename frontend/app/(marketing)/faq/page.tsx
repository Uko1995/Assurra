import type { Metadata } from "next";
import { HeroSection, FaqAccordion, CtaSection } from "@/features/marketing/sections";
import { createMetadata, createJsonLd } from "@/shared/lib/seo";
import faqContent from "@content/pages/faq.json";

export const metadata: Metadata = createMetadata({
  title: "FAQ",
  description:
    "Frequently asked questions about Assurra escrow fees, payouts, disputes, refunds, and integration.",
  path: "/faq",
});

const allItems = faqContent.groups.flatMap((group) => group.items);

export default function FaqPage() {
  const faqJsonLd = {
    "@context": "https://schema.org",
    "@type": "FAQPage",
    mainEntity: allItems.map((item) => ({
      "@type": "Question",
      name: item.question,
      acceptedAnswer: {
        "@type": "Answer",
        text: item.answer,
      },
    })),
  };

  return (
    <>
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: createJsonLd(faqJsonLd) }}
      />
      <HeroSection
        title="Questions? Answered."
        subtitle="Everything merchants, customers, and developers ask before they start using Assurra."
        primaryCta={{ label: "Create an escrow", href: "/for-customers" }}
        secondaryCta={{ label: "Contact support", href: "/contact" }}
        centered
      />
      <FaqAccordion
        title="Frequently asked questions"
        groups={faqContent.groups}
      />
      <CtaSection
        title="Still have questions?"
        subtitle="Our team replies within one business day."
        primaryCta={{ label: "Contact us", href: "/contact" }}
        secondaryCta={{ label: "Become a merchant", href: "/for-merchants" }}
      />
    </>
  );
}

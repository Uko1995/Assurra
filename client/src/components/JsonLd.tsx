import { siteConfig } from "@/lib/site";

export function JsonLd() {
  const data = {
    "@context": "https://schema.org",
    "@graph": [
      {
        "@type": "Organization",
        name: siteConfig.name,
        url: siteConfig.siteUrl,
        description: siteConfig.description,
        logo: `${siteConfig.siteUrl}/assurra-logo.svg`,
        email: siteConfig.contactEmail,
      },
      {
        "@type": "WebSite",
        name: siteConfig.name,
        url: siteConfig.siteUrl,
        description: siteConfig.description,
      },
    ],
  };

  return (
    <script
      type="application/ld+json"
      dangerouslySetInnerHTML={{ __html: JSON.stringify(data) }}
    />
  );
}

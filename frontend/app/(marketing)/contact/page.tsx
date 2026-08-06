import type { Metadata } from "next";
import { HeroSection, ContactForm } from "@/features/marketing/sections";
import { Container, Card, CardContent, Heading, Text } from "@/shared/ui";
import { createMetadata } from "@/shared/lib/seo";

export const metadata: Metadata = createMetadata({
  title: "Contact",
  description:
    "Get in touch with Assurra sales, support, or partnerships. We reply within one business day.",
  path: "/contact",
});

export default function ContactPage() {
  return (
    <>
      <HeroSection
        title="Talk to us."
        subtitle="Sales, support, partnerships — whatever you need, we're one message away."
        centered
      />
      <ContactForm />
      <section className="bg-background py-48">
        <Container size="md">
          <Heading as="h2" size="xl" className="mb-24 text-center">
            Other ways to reach us
          </Heading>
          <div className="grid gap-16 sm:grid-cols-3">
            <Card>
              <CardContent className="text-center">
                <Heading as="h3" size="base" className="mb-8">
                  Email
                </Heading>
                <Text size="sm" variant="muted">
                  hello@assurra.com
                </Text>
              </CardContent>
            </Card>
            <Card>
              <CardContent className="text-center">
                <Heading as="h3" size="base" className="mb-8">
                  Support
                </Heading>
                <Text size="sm" variant="muted">
                  support@assurra.com
                </Text>
              </CardContent>
            </Card>
            <Card>
              <CardContent className="text-center">
                <Heading as="h3" size="base" className="mb-8">
                  Hours
                </Heading>
                <Text size="sm" variant="muted">
                  Mon–Fri, 9:00–18:00 WAT (Lagos)
                </Text>
              </CardContent>
            </Card>
          </div>
        </Container>
      </section>
    </>
  );
}

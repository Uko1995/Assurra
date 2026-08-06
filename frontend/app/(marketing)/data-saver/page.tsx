import type { Metadata } from "next";
import { HeroSection, DataSaverDemo, CtaSection } from "@/features/marketing/sections";
import { Container, Heading, Text, Card, CardContent } from "@/shared/ui";
import { createMetadata } from "@/shared/lib/seo";

export const metadata: Metadata = createMetadata({
  title: "Data Saver",
  description:
    "Assurra is built for slow networks and small data bundles. Toggle data-saver mode and keep using escrow without burning through data.",
  path: "/data-saver",
});

export default function DataSaverPage() {
  return (
    <>
      <HeroSection
        title="Assurra works even when your network does not."
        subtitle="Small data bundles, patchy 3G, crowded commutes — data-saver mode keeps Assurra fast, light, and usable anywhere in Nigeria."
        primaryCta={{ label: "Try data-saver", href: "/data-saver" }}
        secondaryCta={{ label: "Create an escrow", href: "/for-customers" }}
        centered
      />
      <DataSaverDemo />
      <section className="bg-background py-48">
        <Container size="lg">
          <Heading as="h2" size="xl" className="mb-24 text-center">
            What data-saver mode does
          </Heading>
          <div className="grid gap-24 md:grid-cols-3">
            <Card>
              <CardContent>
                <Heading as="h3" size="base" className="mb-8">
                  Lighter images
                </Heading>
                <Text size="sm" variant="muted">
                  Images load at lower resolution and decorative illustrations are replaced with
                  lightweight icons.
                </Text>
              </CardContent>
            </Card>
            <Card>
              <CardContent>
                <Heading as="h3" size="base" className="mb-8">
                  Motion off
                </Heading>
                <Text size="sm" variant="muted">
                  Non-essential animation is disabled, reducing processor work on budget Android
                  devices.
                </Text>
              </CardContent>
            </Card>
            <Card>
              <CardContent>
                <Heading as="h3" size="base" className="mb-8">
                  Less data, same trust
                </Heading>
                <Text size="sm" variant="muted">
                  Every page still works fully — statuses, references, and payouts are never
                  degraded, only the page weight is.
                </Text>
              </CardContent>
            </Card>
          </div>
          <Text variant="muted" className="mt-32 text-center">
            Your preference is saved and carried into the Assurra dashboard when you log in.
          </Text>
        </Container>
      </section>
      <CtaSection
        title="Built for the way Africa goes online"
        subtitle="Fast on fibre. Fast on 3G. Fast on whatever you've got."
        primaryCta={{ label: "Create an escrow", href: "/for-customers" }}
        secondaryCta={{ label: "Learn how it works", href: "/how-it-works" }}
      />
    </>
  );
}

import Link from "next/link";
import { Button, Container, Heading, Text } from "@/shared/ui";

export default function NotFoundPage() {
  return (
    <div className="flex min-h-[60vh] items-center justify-center">
      <Container size="sm" className="py-64 text-center">
        <Heading as="h1" size="3xl" className="mb-8">
          404 — Page not found
        </Heading>
        <Text variant="muted" size="lg" className="mb-32">
          We couldn&apos;t find the page you were looking for. Let&apos;s get you back to safety.
        </Text>
        <Button asChild>
          <Link href="/">Go back home</Link>
        </Button>
      </Container>
    </div>
  );
}

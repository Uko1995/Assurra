import type { ReactNode } from "react";
import Image from "next/image";
import { Container } from "@/components/ui/Container";

export function PageHero({
  eyebrow,
  title,
  description,
  actions,
  image,
  imageAlt = "",
}: {
  eyebrow?: string;
  title: string;
  description: string;
  actions?: ReactNode;
  image?: string;
  imageAlt?: string;
}) {
  if (image) {
    return (
      <div className="relative isolate min-h-[52vh] overflow-hidden border-b border-border-subtle sm:min-h-[58vh]">
        <div data-decorative className="absolute inset-0">
          <Image
            src={image}
            alt={imageAlt}
            fill
            priority
            sizes="100vw"
            className="object-cover object-center"
          />
          <div className="absolute inset-0 bg-[linear-gradient(105deg,rgba(243,252,240,0.96)_0%,rgba(243,252,240,0.88)_42%,rgba(8,61,20,0.55)_100%)]" />
        </div>
        <Container className="relative flex min-h-[52vh] flex-col justify-end py-16 sm:min-h-[58vh] sm:py-20 lg:py-24">
          {eyebrow ? (
            <p className="animate-fade-up mb-3 text-xs font-semibold uppercase tracking-[0.08em] text-primary">
              {eyebrow}
            </p>
          ) : null}
          <h1 className="animate-fade-up max-w-3xl font-serif text-4xl font-bold tracking-tight text-on-surface sm:text-5xl lg:text-[3.5rem] lg:leading-[1.1]">
            {title}
          </h1>
          <p className="animate-fade-up-delay mt-5 max-w-2xl text-lg leading-8 text-muted">
            {description}
          </p>
          {actions ? (
            <div className="animate-fade-up-delay mt-8 flex flex-wrap gap-3">{actions}</div>
          ) : null}
        </Container>
      </div>
    );
  }

  return (
    <div className="relative overflow-hidden border-b border-border-subtle bg-background">
      <div
        data-decorative
        className="pointer-events-none absolute inset-0 opacity-[0.035]"
        style={{
          backgroundImage:
            "radial-gradient(circle at 20% 20%, #11832b 0.8px, transparent 0.9px), radial-gradient(circle at 80% 0%, #083d14 1px, transparent 1.1px)",
          backgroundSize: "28px 28px, 40px 40px",
        }}
      />
      <Container className="relative py-16 sm:py-20 lg:py-24">
        {eyebrow ? (
          <p className="animate-fade-up mb-3 text-xs font-semibold uppercase tracking-[0.08em] text-primary">
            {eyebrow}
          </p>
        ) : null}
        <h1 className="animate-fade-up max-w-4xl font-serif text-4xl font-bold tracking-tight text-on-surface sm:text-5xl lg:text-[3.5rem] lg:leading-[1.1]">
          {title}
        </h1>
        <p className="animate-fade-up-delay mt-5 max-w-2xl text-lg leading-8 text-muted">
          {description}
        </p>
        {actions ? <div className="animate-fade-up-delay mt-8 flex flex-wrap gap-3">{actions}</div> : null}
      </Container>
    </div>
  );
}

import type { ReactNode } from "react";
import { Container } from "./Container";

export function Section({
  children,
  className = "",
  id,
  tone = "default",
}: {
  children: ReactNode;
  className?: string;
  id?: string;
  tone?: "default" | "muted" | "deep";
}) {
  const toneClass =
    tone === "muted"
      ? "bg-surface-low"
      : tone === "deep"
        ? "bg-secondary text-on-primary"
        : "bg-transparent";

  return (
    <section id={id} className={`py-16 sm:py-20 lg:py-24 ${toneClass} ${className}`}>
      <Container>{children}</Container>
    </section>
  );
}

export function SectionHeading({
  eyebrow,
  title,
  description,
  align = "left",
  invert = false,
}: {
  eyebrow?: string;
  title: string;
  description?: string;
  align?: "left" | "center";
  invert?: boolean;
}) {
  return (
    <div className={`mb-12 max-w-3xl ${align === "center" ? "mx-auto text-center" : ""}`}>
      {eyebrow ? (
        <p
          className={`mb-3 text-xs font-semibold uppercase tracking-[0.08em] ${
            invert ? "text-white/70" : "text-primary"
          }`}
        >
          {eyebrow}
        </p>
      ) : null}
      <h2
        className={`font-serif text-3xl font-semibold tracking-tight sm:text-4xl ${
          invert ? "text-white" : "text-on-surface"
        }`}
      >
        {title}
      </h2>
      {description ? (
        <p
          className={`mt-4 text-base leading-7 sm:text-lg ${
            invert ? "text-white/80" : "text-muted"
          }`}
        >
          {description}
        </p>
      ) : null}
    </div>
  );
}

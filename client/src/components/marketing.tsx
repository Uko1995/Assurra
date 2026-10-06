import type { ReactNode } from "react";
import Link from "next/link";
import { Mark, type MarkName } from "@/components/illustrations";

export function Band({
  children,
  className = "",
  tight = false,
}: {
  children: ReactNode;
  className?: string;
  tight?: boolean;
}) {
  return (
    <section className={className}>
      <div className={`mx-auto max-w-6xl px-4 ${tight ? "py-10 md:py-14" : "py-16 md:py-24"}`}>{children}</div>
    </section>
  );
}

export function Intro({
  eyebrow,
  title,
  children,
  action,
}: {
  eyebrow?: string;
  title: string;
  children?: ReactNode;
  action?: ReactNode;
}) {
  return (
    <div className="flex flex-wrap items-end justify-between gap-6">
      <div className="max-w-2xl">
        {eyebrow && <p className="meta text-primary">{eyebrow}</p>}
        <h2 className={`section-title text-ink ${eyebrow ? "mt-3" : ""}`}>{title}</h2>
        {children && <p className="lede mt-3">{children}</p>}
      </div>
      {action}
    </div>
  );
}

export function Story({
  eyebrow,
  title,
  copy,
  mark,
  reverse = false,
  children,
}: {
  eyebrow: string;
  title: string;
  copy: string;
  mark: MarkName;
  reverse?: boolean;
  children?: ReactNode;
}) {
  return (
    <div className={`grid items-center gap-10 md:grid-cols-2 ${reverse ? "md:[&>div:first-child]:order-2" : ""}`}>
      <div>
        <p className="meta text-primary">{eyebrow}</p>
        <h2 className="section-title mt-3">{title}</h2>
        <p className="lede mt-3">{copy}</p>
        {children}
      </div>
      <div
        className="grid min-h-72 place-items-center rounded-panel border border-line bg-subtle"
        role="img"
        aria-label={title}
      >
        <Mark name={mark} className="size-44" trigger="in" />
      </div>
    </div>
  );
}

export function CtaBand({
  title,
  copy,
  primary,
  secondary,
}: {
  title: string;
  copy: string;
  primary: { href: string; label: string };
  secondary?: { href: string; label: string };
}) {
  return (
    <Band className="px-4 pb-16 md:pb-24">
      <div className="relative overflow-hidden rounded-panel bg-panel px-6 py-12 text-white md:px-12 md:py-16">
        <div className="grain pointer-events-none absolute inset-0 opacity-80" />
        <div className="relative flex flex-col items-start justify-between gap-8 md:flex-row md:items-center">
          <div className="max-w-xl">
            <h2 className="section-title">{title}</h2>
            <p className="mt-3 text-[1.0625rem] text-white/70">{copy}</p>
          </div>
          <div className="flex flex-wrap gap-3">
            <Link
              href={primary.href}
              className="inline-flex min-h-11 items-center rounded-field bg-white px-5 text-sm font-semibold text-panel transition hover:bg-white/90"
            >
              {primary.label}
            </Link>
            {secondary && (
              <Link
                href={secondary.href}
                className="inline-flex min-h-11 items-center rounded-field border border-white/20 px-5 text-sm font-semibold text-white transition hover:bg-white/10"
              >
                {secondary.label}
              </Link>
            )}
          </div>
        </div>
      </div>
    </Band>
  );
}

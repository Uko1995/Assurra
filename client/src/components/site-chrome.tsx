"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useEffect, useState } from "react";
import { Icon } from "@/components/icons";
import { Logo, btnPrimary } from "@/components/ui";

const nav = [
  { href: "/how-it-works", label: "How it works" },
  { href: "/pricing", label: "Pricing" },
  { href: "/for-merchants", label: "Merchants" },
  { href: "/for-customers", label: "Customers" },
];

export function SiteHeader() {
  const [open, setOpen] = useState(false);
  const [scrolled, setScrolled] = useState(false);
  const pathname = usePathname();

  useEffect(() => setOpen(false), [pathname]);

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 6);
    onScroll();
    window.addEventListener("scroll", onScroll, { passive: true });
    return () => window.removeEventListener("scroll", onScroll);
  }, []);

  return (
    <header
      className={`sticky top-0 z-40 transition duration-200 ${
        scrolled ? "border-b border-line bg-canvas/85 backdrop-blur-md" : "border-b border-transparent bg-canvas/70 backdrop-blur-sm"
      }`}
    >
      <div className="mx-auto flex h-14 max-w-6xl items-center justify-between gap-4 px-4">
        <Logo />
        <nav className="hidden items-center gap-0.5 text-sm font-medium md:flex">
          {nav.map((item) => (
            <Link
              key={item.href}
              href={item.href}
              className={`rounded-field px-3 py-2 transition duration-200 ${
                pathname === item.href ? "bg-subtle text-ink" : "text-muted hover:text-ink"
              }`}
            >
              {item.label}
            </Link>
          ))}
        </nav>
        <div className="hidden items-center gap-2 md:flex">
          <Link href="/login" className="rounded-field px-3 py-2 text-sm font-semibold text-ink transition hover:bg-subtle">
            Sign in
          </Link>
          <Link href="/register" className={btnPrimary}>
            Get started
          </Link>
        </div>
        <button
          type="button"
          className="grid size-11 place-items-center rounded-field border border-line-strong text-ink md:hidden"
          aria-label={open ? "Close menu" : "Open menu"}
          aria-expanded={open}
          onClick={() => setOpen((value) => !value)}
        >
          <Icon name={open ? "close" : "menu"} className="size-5" />
        </button>
      </div>
      <nav className={`border-t border-line bg-canvas px-4 py-3 md:hidden ${open ? "" : "hidden"}`}>
        {nav.map((item) => (
          <Link
            key={item.href}
            href={item.href}
            className="block rounded-field px-2 py-3 text-[0.9375rem] font-medium text-ink"
            onClick={() => setOpen(false)}
          >
            {item.label}
          </Link>
        ))}
        <div className="mt-2 grid gap-2 border-t border-line pt-3">
          <Link href="/login" className="rounded-field px-2 py-3 text-[0.9375rem] font-semibold text-ink" onClick={() => setOpen(false)}>
            Sign in
          </Link>
          <Link href="/register" className={`${btnPrimary} w-full`} onClick={() => setOpen(false)}>
            Get started
          </Link>
        </div>
      </nav>
    </header>
  );
}

const footerGroups = [
  {
    title: "Product",
    links: [
      { href: "/how-it-works", label: "How it works" },
      { href: "/pricing", label: "Pricing" },
    ],
  },
  {
    title: "Accounts",
    links: [
      { href: "/for-customers", label: "For payers" },
      { href: "/for-merchants", label: "For merchants" },
    ],
  },
  {
    title: "Legal",
    links: [
      { href: "/legal/privacy", label: "Privacy" },
      { href: "/legal/terms", label: "Terms" },
    ],
  },
];

export function SiteFooter() {
  return (
    <footer className="border-t border-line bg-surface">
      <div className="mx-auto grid max-w-6xl gap-10 px-4 py-14 md:grid-cols-[1.5fr_2fr]">
        <div>
          <Logo />
          <p className="mt-4 max-w-xs text-[0.9375rem] leading-6 text-muted">
            Naira stays on a shared record until the payer confirms the stage. Goods, services, and contract work use
            the same path.
          </p>
        </div>
        <div className="grid gap-8 sm:grid-cols-3">
          {footerGroups.map((group) => (
            <div key={group.title}>
              <p className="meta text-faint">{group.title}</p>
              <ul className="mt-3 space-y-2.5 text-[0.9375rem]">
                {group.links.map((link) => (
                  <li key={link.href}>
                    <Link href={link.href} className="text-muted transition hover:text-ink">
                      {link.label}
                    </Link>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>
      </div>
      <div className="border-t border-line">
        <p className="mx-auto flex max-w-6xl flex-wrap items-center justify-between gap-3 px-4 py-5 text-[0.8125rem] text-faint">
          <span>
            © {new Date().getFullYear()} Assurra. Escrow for naira transactions. Amounts shown on a record are the
            amounts charged.
          </span>
          <a href="https://lordicon.com/" className="hover:text-ink">
            Animated icons by Lordicon.com
          </a>
        </p>
      </div>
    </footer>
  );
}

export function AuthFrame({
  title,
  lede,
  aside,
  children,
}: {
  title: string;
  lede: string;
  aside?: React.ReactNode;
  children: React.ReactNode;
}) {
  return (
    <div className="min-h-screen">
      <SiteHeader />
      <main className="mx-auto grid max-w-6xl gap-10 px-4 py-12 md:py-20 lg:grid-cols-[1fr_27rem] lg:items-start">
        <div className="max-w-lg lg:sticky lg:top-24">
          <h1 className="page-title rise">{title}</h1>
          <p className="lede mt-3 rise rise-delay-1">{lede}</p>
          {aside}
        </div>
        <div className="rise rise-delay-2 rounded-card border border-line bg-surface p-5 shadow-card md:p-7">{children}</div>
      </main>
    </div>
  );
}

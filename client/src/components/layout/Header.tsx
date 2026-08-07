"use client";

import Image from "next/image";
import Link from "next/link";
import { List, X } from "@phosphor-icons/react";
import { useState } from "react";
import { Button } from "@/components/ui/Button";
import { Container } from "@/components/ui/Container";
import { DataSaverToggle } from "@/components/DataSaverToggle";
import { appPath, navLinks } from "@/lib/site";

export function Header() {
  const [open, setOpen] = useState(false);

  return (
    <header className="sticky top-0 z-40 border-b border-border-subtle/80 bg-[color-mix(in_srgb,var(--background)_90%,transparent)] backdrop-blur-[10px]">
      <Container className="flex h-16 items-center justify-between gap-4">
        <Link href="/" className="flex items-center gap-2.5" onClick={() => setOpen(false)}>
          <Image
            src="/assurra-logo.svg"
            alt="Assurra"
            width={28}
            height={28}
            className="h-7 w-7"
            priority
          />
          <span className="font-serif text-xl font-semibold tracking-tight text-on-surface">
            Assurra
          </span>
        </Link>

        <nav className="hidden items-center gap-6 lg:flex" aria-label="Primary">
          {navLinks.map((link) => (
            <Link
              key={link.href}
              href={link.href}
              className="text-sm font-medium text-muted transition-colors hover:text-primary"
            >
              {link.label}
            </Link>
          ))}
        </nav>

        <div className="hidden items-center gap-3 lg:flex">
          <DataSaverToggle />
          <Button href={appPath("/register/customer")} variant="secondary" className="!px-4 !py-2">
            Create escrow
          </Button>
          <Button href={appPath("/register/merchant")} className="!px-5 !py-2">
            Become a merchant
          </Button>
        </div>

        <button
          type="button"
          className="rounded-lg p-2 text-on-surface lg:hidden"
          aria-label={open ? "Close menu" : "Open menu"}
          onClick={() => setOpen((v) => !v)}
        >
          {open ? <X size={24} weight="bold" /> : <List size={24} weight="bold" />}
        </button>
      </Container>

      {open ? (
        <div className="border-t border-border-subtle bg-surface-lowest lg:hidden">
          <Container className="flex flex-col gap-1 py-4">
            {navLinks.map((link) => (
              <Link
                key={link.href}
                href={link.href}
                className="rounded-lg px-3 py-2.5 text-sm font-medium text-on-surface hover:bg-surface-low"
                onClick={() => setOpen(false)}
              >
                {link.label}
              </Link>
            ))}
            <div className="mt-3 flex flex-col gap-2 border-t border-border-subtle pt-3">
              <DataSaverToggle className="self-start" />
              <Button href={appPath("/register/customer")} variant="secondary">
                Create escrow
              </Button>
              <Button href={appPath("/register/merchant")}>Become a merchant</Button>
            </div>
          </Container>
        </div>
      ) : null}
    </header>
  );
}

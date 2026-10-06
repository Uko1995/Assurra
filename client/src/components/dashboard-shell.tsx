"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { Icon, type IconName } from "@/components/icons";
import { Logo, Skeleton } from "@/components/ui";
import { useAuth } from "@/lib/auth-store";
import { homeForRole } from "@/lib/format";
import type { Role } from "@/lib/types";

type NavLink = { href: string; label: string; icon: IconName };
type NavGroup = { title: string; links: NavLink[] };

const groups: Record<Role, NavGroup[]> = {
  CUSTOMER: [
    {
      title: "Escrows",
      links: [
        { href: "/customer", label: "Your escrows", icon: "record" },
        { href: "/customer/escrow/new", label: "New escrow", icon: "plus" },
      ],
    },
    {
      title: "Account",
      links: [{ href: "/customer/notifications", label: "Notifications", icon: "bell" }],
    },
  ],
  MERCHANT: [
    {
      title: "Work",
      links: [{ href: "/merchant", label: "Escrows", icon: "record" }],
    },
    {
      title: "Account",
      links: [
        { href: "/merchant/kyc", label: "KYC", icon: "shield" },
        { href: "/merchant/payouts", label: "Payouts", icon: "bank" },
        { href: "/merchant/settings", label: "Settings", icon: "sliders" },
      ],
    },
  ],
  ADMIN: [
    {
      title: "Queues",
      links: [
        { href: "/admin", label: "Overview", icon: "grid" },
        { href: "/admin/kyc", label: "KYC", icon: "shield" },
        { href: "/admin/disputes", label: "Disputes", icon: "flag" },
        { href: "/admin/payouts", label: "Payouts", icon: "bank" },
        { href: "/admin/aml", label: "AML alerts", icon: "alert" },
      ],
    },
    {
      title: "Records",
      links: [{ href: "/admin/escrows", label: "All escrows", icon: "layers" }],
    },
    {
      title: "Settings",
      links: [
        { href: "/admin/fees", label: "Fees", icon: "percent" },
        { href: "/admin/refunds", label: "Refunds", icon: "undo" },
      ],
    },
  ],
};

const mobilePrimary: Record<Role, NavLink[]> = {
  CUSTOMER: [
    { href: "/customer", label: "Escrows", icon: "record" },
    { href: "/customer/escrow/new", label: "New", icon: "plus" },
    { href: "/customer/notifications", label: "Inbox", icon: "bell" },
  ],
  MERCHANT: [
    { href: "/merchant", label: "Escrows", icon: "record" },
    { href: "/merchant/kyc", label: "KYC", icon: "shield" },
    { href: "/merchant/payouts", label: "Payouts", icon: "bank" },
  ],
  ADMIN: [
    { href: "/admin", label: "Home", icon: "grid" },
    { href: "/admin/kyc", label: "KYC", icon: "shield" },
    { href: "/admin/disputes", label: "Disputes", icon: "flag" },
  ],
};

function isActive(role: Role, href: string, pathname: string) {
  if (pathname === href) return true;
  const all = groups[role].flatMap((group) => group.links.map((link) => link.href));
  const nestedElsewhere = all.some(
    (other) => other !== href && other.startsWith(`${href}/`) && pathname.startsWith(other),
  );
  return !nestedElsewhere && pathname.startsWith(`${href}/`);
}

function Nav({ role, onNavigate }: { role: Role; onNavigate?: () => void }) {
  const pathname = usePathname();
  return (
    <nav className="space-y-7">
      {groups[role].map((group) => (
        <div key={group.title}>
          <p className="meta px-3 text-faint">{group.title}</p>
          <ul className="mt-2 space-y-0.5">
            {group.links.map((link) => {
              const active = isActive(role, link.href, pathname);
              return (
                <li key={link.href}>
                  <Link
                    href={link.href}
                    onClick={onNavigate}
                    aria-current={active ? "page" : undefined}
                    className={`flex min-h-11 items-center gap-2.5 rounded-field px-3 text-[0.9375rem] font-medium transition duration-200 ${
                      active
                        ? "bg-primary-tint text-primary-deep"
                        : "text-muted hover:bg-subtle hover:text-ink"
                    }`}
                  >
                    <Icon name={link.icon} className={`size-4 ${active ? "text-primary" : "text-faint"}`} />
                    {link.label}
                  </Link>
                </li>
              );
            })}
          </ul>
        </div>
      ))}
    </nav>
  );
}

export function DashboardShell({
  role,
  title,
  lede,
  action,
  children,
}: {
  role: Role;
  title: string;
  lede?: string;
  action?: React.ReactNode;
  children: React.ReactNode;
}) {
  const pathname = usePathname();
  const router = useRouter();
  const [open, setOpen] = useState(false);
  const ready = useAuth((state) => state.ready);
  const user = useAuth((state) => state.user);
  const clear = useAuth((state) => state.clear);

  useEffect(() => setOpen(false), [pathname]);

  useEffect(() => {
    if (!ready) return;
    if (!user) {
      router.replace("/login");
      return;
    }
    if (user.role !== role) router.replace(homeForRole(user.role));
  }, [ready, role, router, user]);

  if (!ready || !user || user.role !== role) {
    return (
      <div className="mx-auto max-w-md space-y-3 p-8">
        <Skeleton className="h-4 w-40" />
        <Skeleton className="h-4 w-56" />
        <p className="pt-2 text-sm text-faint">Checking your session…</p>
      </div>
    );
  }

  const signOut = () => {
    clear();
    router.replace("/");
  };

  const initials = user.fullName
    .split(" ")
    .slice(0, 2)
    .map((part) => part[0])
    .join("")
    .toUpperCase();

  return (
    <div className="min-h-screen md:grid md:grid-cols-[16.5rem_1fr]">
      <aside className="hidden border-r border-line bg-surface md:flex md:min-h-screen md:flex-col">
        <div className="px-4 py-5">
          <Logo href={homeForRole(role)} />
        </div>
        <div className="flex-1 px-2 py-2">
          <Nav role={role} />
        </div>
        <div className="border-t border-line p-3">
          <div className="flex items-center gap-3 px-1">
            <span className="grid size-9 place-items-center rounded-full bg-primary-tint text-[0.75rem] font-semibold text-primary-deep">
              {initials}
            </span>
            <div className="min-w-0">
              <p className="truncate text-[0.875rem] font-semibold text-ink">{user.fullName}</p>
              <p className="text-[0.75rem] capitalize text-faint">{role.toLowerCase()}</p>
            </div>
          </div>
          <button
            type="button"
            onClick={signOut}
            className="mt-3 min-h-11 w-full rounded-field px-2 text-left text-[0.875rem] font-semibold text-muted transition hover:bg-subtle hover:text-ink"
          >
            Sign out
          </button>
        </div>
      </aside>

      <div className="min-w-0 pb-20 md:pb-0">
        <header className="sticky top-0 z-30 border-b border-line bg-canvas/90 backdrop-blur-md">
          <div className="flex items-center gap-3 px-4 py-2 md:hidden">
            <button
              type="button"
              className="grid size-11 place-items-center rounded-field border border-line-strong"
              aria-label={open ? "Close menu" : "Open menu"}
              aria-expanded={open}
              onClick={() => setOpen((value) => !value)}
            >
              <Icon name={open ? "close" : "menu"} className="size-5" />
            </button>
            <Logo href={homeForRole(role)} />
          </div>
          <div className="flex flex-wrap items-end justify-between gap-3 px-4 py-4 md:px-8 md:py-6">
            <div className="min-w-0">
              <h1 className="truncate text-[1.375rem] font-semibold tracking-tight">{title}</h1>
              {lede && <p className="mt-1 text-[0.9375rem] text-muted">{lede}</p>}
            </div>
            <div className="flex items-center gap-2">{action}</div>
          </div>
        </header>

        {open && (
          <div className="border-b border-line bg-surface px-2 py-4 md:hidden">
            <Nav role={role} onNavigate={() => setOpen(false)} />
            <button
              type="button"
              onClick={signOut}
              className="mt-5 min-h-11 w-full rounded-field px-3 text-left text-[0.9375rem] font-semibold text-muted"
            >
              Sign out
            </button>
          </div>
        )}

        <main className="mx-auto max-w-5xl space-y-6 px-4 py-6 md:px-8 md:py-8">{children}</main>
      </div>

      <nav className="fixed inset-x-0 bottom-0 z-30 grid grid-cols-3 border-t border-line bg-surface/95 backdrop-blur-md md:hidden">
        {mobilePrimary[role].map((link) => {
          const active = isActive(role, link.href, pathname);
          return (
            <Link
              key={link.href}
              href={link.href}
              className={`flex min-h-14 flex-col items-center justify-center gap-1 text-[0.6875rem] font-semibold ${
                active ? "text-primary" : "text-faint"
              }`}
            >
              <Icon name={link.icon} className="size-5" />
              {link.label}
            </Link>
          );
        })}
      </nav>
    </div>
  );
}

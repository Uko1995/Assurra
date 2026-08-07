import Image from "next/image";
import Link from "next/link";
import { Container } from "@/components/ui/Container";
import { footerLinks, siteConfig } from "@/lib/site";

export function Footer() {
  return (
    <footer className="border-t border-border-subtle bg-secondary text-white">
      <Container className="py-14">
        <div className="grid gap-10 md:grid-cols-2 lg:grid-cols-4">
          <div className="lg:col-span-1">
            <div className="flex items-center gap-2.5">
              <Image
                src="/assurra-logo.svg"
                alt=""
                width={28}
                height={28}
                className="h-7 w-7"
              />
              <span className="font-serif text-xl font-semibold">Assurra</span>
            </div>
            <p className="mt-4 max-w-xs text-sm leading-6 text-white/75">
              {siteConfig.description}
            </p>
          </div>

          {(
            [
              ["Product", footerLinks.product],
              ["Audiences", footerLinks.audiences],
              ["Company", footerLinks.company],
            ] as const
          ).map(([title, links]) => (
            <div key={title}>
              <p className="text-xs font-semibold uppercase tracking-[0.08em] text-white/55">
                {title}
              </p>
              <ul className="mt-4 space-y-2.5">
                {links.map((link) => (
                  <li key={link.href}>
                    <Link
                      href={link.href}
                      className="text-sm text-white/85 transition-colors hover:text-white"
                    >
                      {link.label}
                    </Link>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>

        <div className="mt-12 flex flex-col gap-3 border-t border-white/10 pt-6 text-sm text-white/60 sm:flex-row sm:items-center sm:justify-between">
          <p>© {new Date().getFullYear()} Assurra. Escrow for African commerce.</p>
          <a href={`mailto:${siteConfig.contactEmail}`} className="hover:text-white">
            {siteConfig.contactEmail}
          </a>
        </div>
      </Container>
    </footer>
  );
}

export const siteConfig = {
  name: "Assurra",
  tagline: "Escrow for African commerce",
  description:
    "Assurra holds payments safely between buyers and sellers — in naira, on Nigerian bank accounts, with clear status every step of the way.",
  get siteUrl() {
    return process.env.NEXT_PUBLIC_SITE_URL ?? "http://localhost:3000";
  },
  get appUrl() {
    return process.env.NEXT_PUBLIC_APP_URL ?? "http://localhost:3000/app";
  },
  contactEmail: "hello@assurra.com",
} as const;

export function appPath(path: string) {
  const base = siteConfig.appUrl.replace(/\/$/, "");
  const suffix = path.startsWith("/") ? path : `/${path}`;
  return `${base}${suffix}`;
}

export const navLinks = [
  { href: "/services", label: "Services" },
  { href: "/how-it-works", label: "How it works" },
  { href: "/pricing", label: "Pricing" },
  { href: "/for-merchants", label: "Merchants" },
  { href: "/for-customers", label: "Customers" },
  { href: "/developers", label: "Developers" },
  { href: "/security", label: "Security" },
] as const;

export const footerLinks = {
  product: [
    { href: "/services", label: "Services" },
    { href: "/how-it-works", label: "How it works" },
    { href: "/pricing", label: "Pricing" },
    { href: "/data-saver", label: "Data Saver" },
  ],
  audiences: [
    { href: "/for-merchants", label: "For merchants" },
    { href: "/for-customers", label: "For customers" },
    { href: "/developers", label: "Developers" },
  ],
  company: [
    { href: "/security", label: "Security" },
    { href: "/help", label: "Help Center" },
    { href: "/legal/privacy", label: "Privacy" },
    { href: "/legal/terms", label: "Terms" },
  ],
} as const;

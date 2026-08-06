import type { ReactNode } from "react";
import { MarketingNav } from "@/widgets/MarketingNav";
import { MarketingFooter } from "@/widgets/MarketingFooter";
import { CookieConsentBanner } from "@/widgets/CookieConsentBanner";

export default function MarketingLayout({ children }: { children: ReactNode }) {
  return (
    <div className="flex min-h-screen flex-col">
      <a
        href="#main-content"
        className="sr-only focus:not-sr-only focus:absolute focus:left-8 focus:top-8 focus:z-[1500] focus:rounded focus:bg-primary focus:px-16 focus:py-12 focus:text-white"
      >
        Skip to main content
      </a>
      <MarketingNav />
      <main id="main-content" className="flex-1">
        {children}
      </main>
      <MarketingFooter />
      <CookieConsentBanner />
    </div>
  );
}

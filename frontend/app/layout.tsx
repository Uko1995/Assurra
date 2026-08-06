import type { Metadata, Viewport } from "next";
import { Inter, Fraunces } from "next/font/google";
import { DataSaverProvider } from "@/providers/DataSaverProvider";
import "@/shared/styles/globals.css";

const inter = Inter({
  subsets: ["latin"],
  variable: "--font-inter",
  display: "swap",
});

const fraunces = Fraunces({
  subsets: ["latin"],
  variable: "--font-display",
  display: "swap",
  axes: ["opsz"],
});

export const metadata: Metadata = {
  title: {
    default: "Assurra — Escrow for African Commerce",
    template: "%s — Assurra",
  },
  description:
    "Hold payments safely between buyers and sellers in Nigeria. Assurra releases funds only when delivery is confirmed. Fast, transparent, and built for African commerce.",
  metadataBase: new URL("https://assurra.com"),
  alternates: {
    canonical: "/",
  },
  openGraph: {
    type: "website",
    siteName: "Assurra",
    locale: "en_NG",
  },
  twitter: {
    card: "summary_large_image",
  },
};

export const viewport: Viewport = {
  width: "device-width",
  initialScale: 1,
  themeColor: [
    { media: "(prefers-color-scheme: light)", color: "#F6F8F6" },
    { media: "(prefers-color-scheme: dark)", color: "#0A0F0B" },
  ],
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en" suppressHydrationWarning>
      <body className={`${inter.variable} ${fraunces.variable} font-sans antialiased`}>
        <DataSaverProvider>{children}</DataSaverProvider>
      </body>
    </html>
  );
}

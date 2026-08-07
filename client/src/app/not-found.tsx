import Image from "next/image";
import { Button } from "@/components/ui/Button";
import { Footer } from "@/components/layout/Footer";
import { Header } from "@/components/layout/Header";
import { images, notFoundContent } from "@/content/pages";

export default function NotFound() {
  return (
    <div className="flex min-h-screen flex-col">
      <Header />
      <main className="relative flex flex-1 items-center justify-center overflow-hidden px-4 py-20">
        <div data-decorative className="pointer-events-none absolute inset-0 opacity-30">
          <Image
            src={images.notFoundHero}
            alt=""
            fill
            sizes="100vw"
            className="object-cover"
          />
          <div className="absolute inset-0 bg-[linear-gradient(180deg,var(--background)_20%,rgba(243,252,240,0.92)_100%)]" />
        </div>
        <div className="relative max-w-lg text-center">
          <p className="text-xs font-semibold uppercase tracking-[0.08em] text-primary">
            Error 404
          </p>
          <h1 className="mt-3 font-serif text-4xl font-bold text-on-surface sm:text-5xl">
            {notFoundContent.title}
          </h1>
          <p className="mt-4 text-muted">{notFoundContent.description}</p>
          <div className="mt-8 flex flex-wrap justify-center gap-3">
            <Button href="/">Return home</Button>
            <Button href="/help" variant="secondary">
              Contact support
            </Button>
          </div>
        </div>
      </main>
      <Footer />
    </div>
  );
}

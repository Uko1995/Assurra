import Image from "next/image";
import Link from "next/link";
import {
  ArrowRight,
  Check,
  LockKey,
  SealCheck,
  WifiHigh,
} from "@phosphor-icons/react/dist/ssr";
import { Button } from "@/components/ui/Button";
import { Container } from "@/components/ui/Container";
import { Section, SectionHeading } from "@/components/ui/Section";
import { appPath } from "@/lib/site";
import { homeContent, howItWorksSteps, images } from "@/content/pages";

const trustIcons = [LockKey, SealCheck, WifiHigh, SealCheck] as const;

export default function HomePage() {
  const { middleGround, audiences, proof, feeSnapshot } = homeContent;

  return (
    <>
      <section className="relative isolate min-h-[88vh] overflow-hidden">
        <div data-decorative className="absolute inset-0">
          <Image
            src={images.homeHero}
            alt=""
            fill
            priority
            sizes="100vw"
            className="object-cover object-[center_30%]"
          />
          <div className="absolute inset-0 bg-[linear-gradient(115deg,rgba(8,61,20,0.88)_0%,rgba(8,61,20,0.72)_38%,rgba(17,131,43,0.35)_100%)]" />
          <div className="absolute inset-0 bg-[radial-gradient(circle_at_80%_20%,rgba(243,252,240,0.12),transparent_45%)]" />
        </div>

        <Container className="relative flex min-h-[88vh] flex-col justify-end pb-16 pt-28 sm:pb-20 lg:pb-24">
          <p className="animate-fade-up font-serif text-5xl font-bold tracking-tight text-white sm:text-6xl lg:text-7xl">
            {homeContent.brand}
          </p>
          <h1 className="animate-fade-up mt-4 max-w-2xl font-serif text-3xl font-semibold leading-tight text-white sm:text-4xl lg:text-5xl">
            {homeContent.headline}
          </h1>
          <p className="animate-fade-up-delay mt-5 max-w-xl text-lg leading-8 text-white/85">
            {homeContent.support}
          </p>
          <div className="animate-fade-up-delay mt-8 flex flex-wrap gap-3">
            <Button href={appPath("/register/customer")} className="!bg-white !text-secondary">
              Create a free escrow <ArrowRight size={16} weight="bold" />
            </Button>
            <Button
              href={appPath("/register/merchant")}
              variant="secondary"
              className="!border-white/45 !bg-transparent !text-white hover:!bg-white/10"
            >
              Become a merchant
            </Button>
          </div>
        </Container>
      </section>

      <Section tone="muted" className="!py-10">
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {homeContent.trustStrip.map((label, i) => {
            const Icon = trustIcons[i] ?? SealCheck;
            return (
              <div key={label} className="flex items-center gap-3 px-1 py-1">
                <Icon size={22} weight="duotone" className="shrink-0 text-primary" />
                <span className="text-sm font-semibold text-on-surface">{label}</span>
              </div>
            );
          })}
        </div>
      </Section>

      <Section>
        <SectionHeading
          title={middleGround.title}
          description={middleGround.description}
          align="center"
        />
        <div className="grid items-stretch gap-6 lg:grid-cols-3">
          <div className="space-y-3">
            <h3 className="font-serif text-xl font-semibold text-on-surface">Buyer Risk</h3>
            <ul className="space-y-2 text-sm leading-7 text-muted">
              {middleGround.buyerRisks.map((item) => (
                <li key={item}>• {item}</li>
              ))}
            </ul>
          </div>
          <div className="flex items-center justify-center rounded-[20px] bg-secondary px-6 py-10 text-center text-white">
            <div>
              <p className="font-serif text-2xl font-semibold">Assurra</p>
              <p className="mt-3 text-sm leading-7 text-white/80">{middleGround.center}</p>
            </div>
          </div>
          <div className="space-y-3">
            <h3 className="font-serif text-xl font-semibold text-on-surface">Seller Risk</h3>
            <ul className="space-y-2 text-sm leading-7 text-muted">
              {middleGround.sellerRisks.map((item) => (
                <li key={item}>• {item}</li>
              ))}
            </ul>
          </div>
        </div>
      </Section>

      <Section tone="muted">
        <SectionHeading
          eyebrow={homeContent.lifecycleEyebrow}
          title={homeContent.lifecycleTitle}
        />
        <ol className="grid gap-3 sm:grid-cols-2 lg:grid-cols-7">
          {howItWorksSteps.map((step, index) => (
            <li key={step.status} className="min-w-0">
              <p className="text-xs font-bold uppercase tracking-[0.08em] text-primary">
                {String(index + 1).padStart(2, "0")}
              </p>
              <p className="mt-2 font-serif text-lg font-semibold text-on-surface">{step.title}</p>
              <p className="mt-1 text-[11px] font-semibold uppercase tracking-wide text-muted">
                {step.status.split(" / ")[0]}
              </p>
            </li>
          ))}
        </ol>
        <div className="mt-8">
          <Button href="/how-it-works" variant="secondary">
            View full lifecycle <ArrowRight size={16} weight="bold" />
          </Button>
        </div>
      </Section>

      <Section>
        <SectionHeading title={audiences.title} />
        <div className="grid gap-8 md:grid-cols-3">
          {(
            [
              ["Merchants", audiences.merchants, "/for-merchants"],
              ["Customers", audiences.customers, "/for-customers"],
              ["Developers", audiences.developers, "/developers"],
            ] as const
          ).map(([title, items, href]) => (
            <div key={title}>
              <h3 className="font-serif text-2xl font-semibold text-on-surface">{title}</h3>
              <ul className="mt-4 space-y-2.5">
                {items.map((item) => (
                  <li key={item} className="flex gap-2 text-sm leading-6 text-muted">
                    <Check size={18} weight="bold" className="mt-0.5 shrink-0 text-primary" />
                    {item}
                  </li>
                ))}
              </ul>
              <Link
                href={href}
                className="mt-5 inline-flex text-sm font-semibold text-primary hover:underline"
              >
                Learn more
              </Link>
            </div>
          ))}
        </div>
      </Section>

      <Section tone="muted">
        <SectionHeading title={proof.title} align="center" />
        <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
          {proof.stats.map((stat) => (
            <div key={stat.label} className="text-center">
              <p className="font-serif text-4xl font-bold text-primary">{stat.value}</p>
              <p className="mt-2 text-sm text-muted">{stat.label}</p>
            </div>
          ))}
        </div>
        <ul className="mx-auto mt-10 flex max-w-3xl flex-wrap justify-center gap-x-8 gap-y-2 text-sm font-medium text-on-surface">
          {proof.notes.map((note) => (
            <li key={note}>{note}</li>
          ))}
        </ul>
      </Section>

      <Section>
        <div className="grid items-end gap-10 lg:grid-cols-[1.1fr_0.9fr]">
          <SectionHeading
            title={feeSnapshot.title}
            description="Customer pays product amount + fee. Merchant receives net after fee."
          />
          <div className="space-y-3 border-t border-border-subtle pt-6 font-mono text-sm sm:text-base">
            <div className="flex justify-between gap-4">
              <span className="text-muted">Product value</span>
              <span className="font-semibold text-on-surface">{feeSnapshot.product}</span>
            </div>
            <div className="flex justify-between gap-4">
              <span className="text-muted">Assurra fee (1.5%)</span>
              <span className="font-semibold text-on-surface">{feeSnapshot.fee}</span>
            </div>
            <div className="flex justify-between gap-4 border-t border-border-subtle pt-3">
              <span className="text-muted">Merchant payout</span>
              <span className="font-semibold text-primary">{feeSnapshot.payout}</span>
            </div>
            <Button href="/pricing" variant="secondary" className="mt-4">
              View full pricing
            </Button>
          </div>
        </div>
      </Section>

      <Section tone="deep">
        <SectionHeading
          invert
          title={homeContent.finalCta.title}
          description="Create an escrow as a customer, become a verified merchant, or integrate the API into your platform."
        />
        <div className="flex flex-wrap gap-3">
          <Button href={appPath("/register/customer")} className="!bg-white !text-secondary">
            Start an escrow
          </Button>
          <Button href="/developers" variant="secondary" className="!border-white/40 !text-white">
            For developers
          </Button>
        </div>
      </Section>
    </>
  );
}

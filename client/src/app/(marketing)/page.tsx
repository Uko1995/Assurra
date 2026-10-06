import Link from "next/link";
import { Icon } from "@/components/icons";
import { IllustrationWell } from "@/components/illustrations";
import { Band, CtaBand, Intro, Story } from "@/components/marketing";
import { Eyebrow, Money, btnPrimary, btnSecondary } from "@/components/ui";
import { ESCROW_MAX, FEE_MAX, FEE_MIN, STAGE_DAYS_MAX, escrowFee, naira } from "@/lib/format";

const facts = [
  { title: "Fee on the record", copy: `1.5%, floor ${naira(FEE_MIN)}, cap ${naira(FEE_MAX)}.` },
  { title: "Work after funding", copy: "The merchant is told only when the money is held." },
  { title: "A real clock", copy: "72 hours to confirm. Unpaid records expire in 24 hours." },
  { title: "Two accounts", copy: "Payer and merchant. Neither pays for the other side's mistake." },
];

const uses = [
  {
    title: "Goods",
    copy: "An order is funded before anyone dispatches it. The payer confirms what arrived.",
    mark: "goods" as const,
  },
  {
    title: "Services",
    copy: "A professional fee stays on the record until the client confirms the work.",
    mark: "service" as const,
  },
  {
    title: "Timeline payments",
    copy: `A longer engagement is a sequence of escrows. Each stage has its own amount and up to ${STAGE_DAYS_MAX} days.`,
    mark: "sequence" as const,
  },
  {
    title: "Large and government work",
    copy: `One record holds up to ${naira(ESCROW_MAX)}. Above that, split the work. The contractor can carry its own fee schedule.`,
    mark: "record" as const,
  },
];

const path = [
  { status: "INITIATED", label: "Agreed" },
  { status: "FUNDED", label: "Paid in" },
  { status: "SHIPPED", label: "In progress" },
  { status: "DELIVERED", label: "Handed over" },
  { status: "RELEASED", label: "Released" },
];

const stageAmount = 4_000_000;

export default function HomePage() {
  return (
    <div>
      <section className="relative overflow-hidden">
        <div className="grain pointer-events-none absolute inset-0" />
        <div className="mx-auto grid max-w-6xl items-center gap-12 px-4 pb-16 pt-10 md:grid-cols-[1.05fr_0.95fr] md:pb-24 md:pt-16">
          <div className="space-y-6">
            <Eyebrow>Escrow for the work, not only the parcel</Eyebrow>
            <h1 className="hero-title rise max-w-xl text-ink">Hold the payment until this stage is confirmed.</h1>
            <p className="lede rise rise-delay-1 max-w-xl">
              Goods, a service, or one stage of a contract share the same record. The merchant starts only after the
              money is funded. Release follows the payer&apos;s confirmation.
            </p>
            <div className="rise rise-delay-2 flex flex-wrap gap-3">
              <Link href="/register?role=customer" className={btnPrimary}>
                Start an escrow
              </Link>
              <Link href="/register?role=merchant" className={btnSecondary}>
                Receive with Assurra
              </Link>
            </div>
          </div>

          <aside className="float relative overflow-hidden rounded-panel bg-panel px-6 py-10 text-white shadow-raise md:px-9 md:py-12">
            <div className="grain pointer-events-none absolute inset-0 opacity-70" />
            <div className="relative">
              <div className="flex items-center justify-between gap-3">
                <p className="meta text-white/55">Example record</p>
                <span className="rounded-full bg-white/10 px-2.5 py-1 text-[0.75rem] font-semibold text-mint">
                  In progress
                </span>
              </div>
              <p className="mt-7 text-[0.9375rem] text-white/70">Stage 2 · supply contract</p>
              <p className="num mt-1 text-[2.75rem] font-semibold tracking-[-0.04em]">
                <span className="text-white/40">₦</span>4,000,000
              </p>
              <p className="mt-2 text-[0.9375rem] text-white/55">
                Fee {naira(escrowFee(stageAmount))} · the published cap
              </p>
              <ol className="mt-8 space-y-0 text-[0.9375rem]">
                {[
                  { label: "Paid in", value: "Done", done: true },
                  { label: "In progress", value: "With the contractor", done: false },
                  { label: "Payer confirms", value: "Not yet", done: false },
                ].map((step) => (
                  <li
                    key={step.label}
                    className="flex items-center justify-between gap-4 border-b border-white/10 py-3.5 last:border-b-0"
                  >
                    <span className="flex items-center gap-2.5">
                      {step.done ? (
                        <Icon name="check" className="size-4 text-mint" />
                      ) : (
                        <span className="size-4 rounded-full border border-white/25" />
                      )}
                      {step.label}
                    </span>
                    <span className={step.done ? "text-mint" : "text-white/45"}>{step.value}</span>
                  </li>
                ))}
              </ol>
              <p className="mt-8 text-[0.875rem] leading-6 text-white/50">
                A larger procurement is the next record. Each stage stops at {naira(ESCROW_MAX)}.
              </p>
            </div>
          </aside>
        </div>
      </section>

      <section className="border-y border-line bg-surface">
        <div className="mx-auto grid max-w-6xl gap-px bg-line sm:grid-cols-2 lg:grid-cols-4">
          {facts.map((item) => (
            <article key={item.title} className="bg-surface px-5 py-6">
              <p className="text-[0.9375rem] font-semibold">{item.title}</p>
              <p className="mt-1.5 text-[0.875rem] leading-6 text-muted">{item.copy}</p>
            </article>
          ))}
        </div>
      </section>

      <Band>
        <div className="space-y-20">
          <Story
            eyebrow="The problem"
            title="People will not prepay a stranger. They will fund a record."
            copy="The money is held, not forwarded. The merchant sees funded, not promised. That is the only signal to start."
            mark="shield"
          />
          <Story
            eyebrow="The record"
            title="One status both sides can read."
            copy="Progress carries a reference. Completion is a status, not a text message. If something is wrong, the reason stays with the money."
            mark="record"
            reverse
          />
          <Story
            eyebrow="The clock"
            title="Deadlines are on the record, not in a chat."
            copy="An unpaid record expires after 24 hours. After handover the payer has 72 hours to confirm. Silence auto-releases."
            mark="clock"
          />
        </div>
      </Band>

      <Band className="bg-surface">
        <Intro title="What a record can hold">
          One flow covers a parcel, a professional fee, and a contract stage. The money and the status behave the same
          way in all of them.
        </Intro>
        <div className="mt-10 grid gap-4 sm:grid-cols-2">
          {uses.map((item) => (
            <article
              key={item.title}
              className="lift rounded-card border border-line bg-canvas p-6"
            >
              <IllustrationWell name={item.mark} />
              <h3 className="mt-5 text-[1.0625rem] font-semibold">{item.title}</h3>
              <p className="mt-1.5 text-[0.9375rem] leading-6 text-muted">{item.copy}</p>
            </article>
          ))}
        </div>
      </Band>

      <Band>
        <Intro
          title="Five steps, one stage."
          action={
            <Link href="/how-it-works" className="text-[0.9375rem] font-semibold text-primary">
              See how it works
            </Link>
          }
        >
          A timeline is the next escrow, not a second status inside this one.
        </Intro>
        <ol className="mt-10 grid gap-3 sm:grid-cols-3 lg:grid-cols-5">
          {path.map((step, index) => (
            <li key={step.status} className="relative rounded-card border border-line bg-surface p-5 shadow-card">
              <p className="meta text-faint">{String(index + 1).padStart(2, "0")}</p>
              <p className="mt-3 text-[1.0625rem] font-semibold">{step.label}</p>
              <p className="meta mt-2 text-primary">{step.status}</p>
            </li>
          ))}
        </ol>
      </Band>

      <Band className="bg-surface" tight>
        <div className="grid gap-10 md:grid-cols-2 md:items-center">
          <div>
            <Eyebrow>Pricing</Eyebrow>
            <h2 className="section-title mt-3">1.5 percent, with a floor and a cap you can read.</h2>
            <p className="lede mt-3">
              Never below {naira(FEE_MIN)} and never above {naira(FEE_MAX)}. A merchant on a large contract can be
              given a different percentage, floor, and cap.
            </p>
            <Link href="/pricing" className={`${btnSecondary} mt-6`}>
              Estimate a fee
            </Link>
          </div>
          <dl className="divide-y divide-line rounded-card border border-line bg-canvas px-6">
            {[20_000, 100_000, 4_000_000].map((amount) => (
              <div key={amount} className="flex items-baseline justify-between gap-4 py-5">
                <dt className="text-[0.9375rem] text-muted">
                  On <Money value={amount} whole />
                </dt>
                <dd className="text-[1.125rem] font-semibold">
                  <Money value={escrowFee(amount)} whole />
                </dd>
              </div>
            ))}
          </dl>
        </div>
      </Band>

      <CtaBand
        title="Open the first record."
        copy="Registration takes a name, an email, a phone number, and consent. It does not sign you in, and it does not cost anything until a record is funded."
        primary={{ href: "/register?role=customer", label: "Start an escrow" }}
        secondary={{ href: "/register?role=merchant", label: "Receive with Assurra" }}
      />
    </div>
  );
}

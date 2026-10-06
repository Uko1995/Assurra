import Link from "next/link";
import { FeeCalculator } from "@/components/fee-calculator";
import { Band, Intro } from "@/components/marketing";
import { Eyebrow, KeyValue, Money, btnPrimary } from "@/components/ui";
import { ESCROW_MAX, FEE_MAX, FEE_MIN, FEE_RATE, escrowFee, naira } from "@/lib/format";

const examples = [
  { amount: 20_000, note: "1.5 percent is below the floor, so the floor applies." },
  { amount: 100_000, note: "1.5 percent, inside the floor and the cap." },
  { amount: 4_000_000, note: "1.5 percent would pass the cap, so the cap applies." },
];

export default function PricingPage() {
  return (
    <div>
      <Band tight>
        <div className="grid gap-12 lg:grid-cols-[1fr_26rem] lg:items-start">
          <header className="space-y-5">
            <Eyebrow>Published schedule</Eyebrow>
            <h1 className="hero-title">One fee, with a floor and a cap.</h1>
            <p className="lede max-w-xl">
              Assurra charges {(FEE_RATE * 100).toFixed(1)} percent of the escrow amount, never below {naira(FEE_MIN)}{" "}
              and never above {naira(FEE_MAX)}. The figure sits on the record before the payer pays.
            </p>
            <ul className="space-y-3 text-[0.9375rem] text-muted">
              <li className="border-t border-line pt-3">No foreign-exchange markup on a local naira deal.</li>
              <li className="border-t border-line pt-3">
                One record stops at {naira(ESCROW_MAX)}. Above that, open the next stage.
              </li>
              <li className="border-t border-line pt-3">
                Nothing is charged until a record is funded. There is no monthly plan.
              </li>
            </ul>
            <Link href="/register" className={btnPrimary}>
              Create an account
            </Link>
          </header>
          <FeeCalculator />
        </div>
      </Band>

      <Band className="bg-surface">
        <Intro title="Three worked amounts">Same schedule, three different outcomes.</Intro>
        <div className="mt-8 grid gap-4 md:grid-cols-3">
          {examples.map((example) => (
            <article key={example.amount} className="rounded-card border border-line bg-canvas p-6">
              <p className="text-[0.9375rem] text-muted">
                On <Money value={example.amount} whole />
              </p>
              <p className="mt-3 text-[2rem] font-semibold tracking-tight">
                <Money value={escrowFee(example.amount)} whole />
              </p>
              <p className="mt-2 text-[0.9375rem] leading-6 text-muted">{example.note}</p>
            </article>
          ))}
        </div>
      </Band>

      <Band tight>
        <section className="grid gap-10 rounded-panel bg-panel px-6 py-12 text-white md:grid-cols-[1.15fr_0.85fr] md:px-12">
          <div className="space-y-3">
            <p className="meta text-mint">Custom schedule</p>
            <h2 className="section-title">Large accounts are priced on the merchant, not on this page.</h2>
            <p className="max-w-xl text-[0.9875rem] leading-6 text-white/70">
              A custom schedule can replace the published percentage, floor, and cap. The rate is stored as a
              fraction, so 1.5 percent is 0.015. Leaving the merchant blank changes the global default; setting one
              merchant id prices a contractor, a firm, or a procurement on its own terms.
            </p>
            <p className="text-[0.9375rem] text-white/50">
              There is no quote form. Ask on your account and a custom schedule can be set against your merchant id.
            </p>
          </div>
          <dl className="space-y-5 self-center text-[0.9375rem]">
            {[
              { label: "What can change", value: "Percentage, floor, and cap" },
              { label: "Where it is set", value: "Fee configuration, against a merchant id" },
              { label: "What stays true", value: "The payer still sees the fee before paying" },
            ].map((row) => (
              <div key={row.label} className="border-b border-white/10 pb-5 last:border-b-0 last:pb-0">
                <dt className="text-white/50">{row.label}</dt>
                <dd className="mt-1 font-medium">{row.value}</dd>
              </div>
            ))}
          </dl>
        </section>
      </Band>

      <Band className="bg-surface">
        <div className="grid gap-10 md:grid-cols-[0.8fr_1.2fr] md:items-start">
          <h2 className="section-title">What the fee is not</h2>
          <div className="rounded-card border border-line bg-canvas p-6">
            <KeyValue
              rows={[
                { label: "Monthly subscription", value: "None" },
                { label: "Charge to create a record", value: "None" },
                { label: "Charge on a cancelled record", value: "None" },
                { label: "Separate dispute charge", value: "None" },
                { label: "Charged when", value: "The record is funded" },
              ]}
            />
          </div>
        </div>
      </Band>
    </div>
  );
}

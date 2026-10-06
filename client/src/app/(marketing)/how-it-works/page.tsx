import Link from "next/link";
import { Band, CtaBand, Intro } from "@/components/marketing";
import { StatusPill, btnPrimary, btnSecondary } from "@/components/ui";
import { ESCROW_MAX, FEE_MAX, FEE_MIN, STAGE_DAYS_MAX, naira } from "@/lib/format";

const steps = [
  {
    status: "INITIATED",
    title: "Agree",
    copy: `The payer names the merchant, the amount, and what this stage is: goods, a service, or part of a contract. The window is 1 to ${STAGE_DAYS_MAX} days.`,
  },
  {
    status: "FUNDED",
    title: "Pay in",
    copy: "The payer pays on the payment page. The merchant is told only after the money is funded, and does not start before that. An unpaid record expires after 24 hours.",
  },
  {
    status: "SHIPPED",
    title: "Record progress",
    copy: "The merchant adds a reference. For goods that is a waybill. For a service or a contract stage it is a note both sides recognise.",
  },
  {
    status: "DELIVERED",
    title: "Hand over",
    copy: "The merchant marks the stage complete. The payer has 72 hours to confirm, and the record shows that deadline.",
  },
  {
    status: "RELEASED",
    title: "Release",
    copy: "Confirmation releases the merchant. If the window passes with no answer, the escrow auto-releases. Only the payer can open a dispute instead, and it is decided from the evidence on the record.",
  },
];

const stages = [
  { label: "Stage 1", title: "Mobilisation", note: "Funded and released", tone: "done" },
  { label: "Stage 2", title: "Delivery", note: "This record, in progress", tone: "now" },
  { label: "Stage 3", title: "Handover", note: "Opens when stage 2 is released", tone: "later" },
];

const faqs = [
  {
    q: "Can one escrow be a whole government contract?",
    a: `No. One record holds at most ${naira(ESCROW_MAX)} and ${STAGE_DAYS_MAX} days. A procurement is a series of records, each funded and confirmed on its own.`,
  },
  {
    q: "Who can open a dispute?",
    a: "The payer, on that record, after it is funded, in progress, or marked complete. The money stays on the record until the dispute is decided.",
  },
  {
    q: "When can the payer cancel?",
    a: "While the escrow is initiated, funded, or the merchant has been notified. Once progress is recorded, cancel is no longer the action.",
  },
  {
    q: "What happens if the payer goes quiet?",
    a: "The confirmation window is 72 hours from the moment the merchant marks the stage complete. After it passes the escrow auto-releases to the merchant.",
  },
  {
    q: "What does the fee cost?",
    a: `The published schedule is 1.5 percent, never below ${naira(FEE_MIN)} and never above ${naira(FEE_MAX)}. A merchant on a large contract can carry a different percentage, floor, and cap.`,
  },
];

export default function HowItWorksPage() {
  return (
    <div>
      <Band tight>
        <p className="meta text-primary">How it works</p>
        <h1 className="hero-title mt-3 max-w-2xl">Five steps, one stage.</h1>
        <p className="lede mt-4 max-w-2xl">
          The same path covers a parcel, a service, and one slice of a larger contract. A timeline is the next escrow,
          not a second status inside this one.
        </p>
        <div className="mt-8 flex flex-wrap gap-3">
          <Link href="/register?role=customer" className={btnPrimary}>
            Start an escrow
          </Link>
          <Link href="/pricing" className={btnSecondary}>
            See the fee
          </Link>
        </div>
      </Band>

      <Band className="bg-surface" tight>
        <ol className="space-y-4">
          {steps.map((step, index) => (
            <li
              key={step.title}
              className="grid gap-4 rounded-card border border-line bg-canvas p-5 md:grid-cols-[4.5rem_1fr_auto] md:items-start md:gap-8 md:p-7"
            >
              <span className="num text-[2rem] font-semibold leading-none tracking-tight text-primary">
                {String(index + 1).padStart(2, "0")}
              </span>
              <div>
                <h2 className="text-[1.25rem] font-semibold tracking-tight">{step.title}</h2>
                <p className="mt-2 max-w-2xl text-[0.9875rem] leading-6 text-muted">{step.copy}</p>
              </div>
              <StatusPill value={step.status} />
            </li>
          ))}
        </ol>
      </Band>

      <Band>
        <Intro title="A longer contract is a sequence">
          Each stage is its own escrow with its own amount, window, and confirmation. Nothing releases because the
          stage beside it released.
        </Intro>
        <ol className="mt-10 grid gap-4 rounded-panel bg-panel px-6 py-10 text-white md:grid-cols-3 md:px-10">
          {stages.map((stage) => (
            <li key={stage.label}>
              <p className="meta text-white/45">{stage.label}</p>
              <p className="mt-2 text-[1.125rem] font-semibold">{stage.title}</p>
              <p className={`mt-1 text-[0.9375rem] ${stage.tone === "now" ? "text-mint" : "text-white/55"}`}>
                {stage.note}
              </p>
            </li>
          ))}
        </ol>
      </Band>

      <Band className="bg-surface">
        <div className="grid gap-10 md:grid-cols-[0.75fr_1.25fr]">
          <h2 className="section-title">Questions the record can answer</h2>
          <dl className="divide-y divide-line">
            {faqs.map((item) => (
              <div key={item.q} className="py-5 first:pt-0 last:pb-0">
                <dt className="text-[1.0625rem] font-semibold">{item.q}</dt>
                <dd className="mt-1.5 text-[0.9375rem] leading-6 text-muted">{item.a}</dd>
              </div>
            ))}
          </dl>
        </div>
      </Band>

      <CtaBand
        title="Ready to hold the first payment?"
        copy="Create a payer or merchant account. Nothing is charged until a record is funded."
        primary={{ href: "/register?role=customer", label: "Start an escrow" }}
        secondary={{ href: "/pricing", label: "See the fee" }}
      />
    </div>
  );
}

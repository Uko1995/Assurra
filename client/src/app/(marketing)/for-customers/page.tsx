import Link from "next/link";
import { Band, CtaBand, Intro, Story } from "@/components/marketing";
import { Eyebrow, KeyValue, btnPrimary, btnSecondary } from "@/components/ui";
import { ESCROW_MAX, FEE_MAX, FEE_MIN, STAGE_DAYS_MAX, naira } from "@/lib/format";

const steps = [
  {
    title: "Open the record",
    copy: "Name the merchant, the amount, and what this stage is. Set a window of 1 to 30 days. You see the fee before you pay.",
  },
  {
    title: "Pay into the record",
    copy: "The money is held, not forwarded. The merchant is told it is funded and starts from there. An unpaid record expires after 24 hours.",
  },
  {
    title: "Watch one status",
    copy: "You and the merchant read the same record. Progress carries a reference, and completion is a status, not a text message.",
  },
  {
    title: "Confirm, or report a problem",
    copy: "Confirming releases the merchant. If the stage is wrong you open a dispute instead, with a reason and the amount in question. The money stays on the record until it is decided.",
  },
];

export default function ForCustomersPage() {
  return (
    <div>
      <Band tight>
        <Eyebrow>For payers</Eyebrow>
        <h1 className="hero-title mt-3 max-w-3xl">Pay into the stage, not into a stranger&apos;s account.</h1>
        <p className="lede mt-4 max-w-2xl">
          You may be buying goods, commissioning a service, or paying one stage of a contract, including a public
          procurement. The money stays on the record until you confirm, and the reason stays with it if you dispute.
        </p>
        <div className="mt-8 flex flex-wrap gap-3">
          <Link href="/register?role=customer" className={btnPrimary}>
            Start an escrow
          </Link>
          <Link href="/how-it-works" className={btnSecondary}>
            See the five steps
          </Link>
        </div>
      </Band>

      <Band className="bg-surface">
        <ol className="grid gap-4 md:grid-cols-2">
          {steps.map((step, index) => (
            <li key={step.title} className="rounded-card border border-line bg-canvas p-6">
              <p className="num text-[1.75rem] font-semibold text-primary">{String(index + 1).padStart(2, "0")}</p>
              <h2 className="mt-4 text-[1.125rem] font-semibold">{step.title}</h2>
              <p className="mt-2 text-[0.9375rem] leading-6 text-muted">{step.copy}</p>
            </li>
          ))}
        </ol>
      </Band>

      <Band>
        <Story
          eyebrow="Control"
          title="You confirm the stage. Silence is a decision too."
          copy="If you go quiet after handover, the record auto-releases in 72 hours. If the stage is wrong, you open a dispute instead. Only you can do that."
          mark="clock"
        />
      </Band>

      <Band className="bg-surface">
        <Intro title="What you control">
          Each record is at most {naira(ESCROW_MAX)} and {STAGE_DAYS_MAX} days. The next stage is a new escrow, so a
          long engagement never rests on one release.
        </Intro>
        <div className="mt-8 rounded-card border border-line bg-canvas p-6">
          <KeyValue
            rows={[
              { label: "You can cancel", value: "Before progress is recorded" },
              { label: "You confirm", value: "After the stage is marked complete" },
              { label: "Confirmation window", value: "72 hours, then auto-release" },
              { label: "Unpaid record", value: "Expires after 24 hours" },
              { label: "Only you can dispute", value: "Once the record is funded" },
              { label: "Published fee", value: `1.5 percent, ${naira(FEE_MIN)}–${naira(FEE_MAX)}` },
            ]}
          />
        </div>
      </Band>

      <CtaBand
        title="Hold the first payment."
        copy="Create a payer account. You see the fee before you pay, and the merchant starts only after funding."
        primary={{ href: "/register?role=customer", label: "Start an escrow" }}
        secondary={{ href: "/how-it-works", label: "See the five steps" }}
      />
    </div>
  );
}

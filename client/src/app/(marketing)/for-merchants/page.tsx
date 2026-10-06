import Link from "next/link";
import { Band, CtaBand, Intro, Story } from "@/components/marketing";
import { Eyebrow, KeyValue, btnPrimary, btnSecondary } from "@/components/ui";
import { ESCROW_MAX, FEE_MAX, FEE_MIN, STAGE_DAYS_MAX, naira } from "@/lib/format";

const steps = [
  {
    title: "Pass KYC once",
    copy: "Business details, a Nigerian bank account, a BVN, and a document. KYC is approved before any settlement can reach you.",
  },
  {
    title: "Wait for funding, not for a promise",
    copy: "You are notified when the escrow is funded. That is the signal to start, whether the stage is a shipment, a service, or contract work.",
  },
  {
    title: "Record progress, then mark complete",
    copy: "Add a reference both sides recognise, then mark the stage complete. The payer has 72 hours to confirm.",
  },
  {
    title: "Get paid to your account",
    copy: "Confirmation releases the payout. If the window passes with no answer, the escrow auto-releases to you.",
  },
];

export default function ForMerchantsPage() {
  return (
    <div>
      <Band tight>
        <Eyebrow>For merchants</Eyebrow>
        <h1 className="hero-title mt-3 max-w-3xl">Start the work only after the stage is funded.</h1>
        <p className="lede mt-4 max-w-2xl">
          You may be shipping goods, delivering a service, or completing one stage of a contract. The signal is the
          same: the escrow is funded. You record a reference, mark the stage complete, and the payout goes to the
          Nigerian account on your profile.
        </p>
        <div className="mt-8 flex flex-wrap gap-3">
          <Link href="/register?role=merchant" className={btnPrimary}>
            Become a merchant
          </Link>
          <Link href="/pricing" className={btnSecondary}>
            See the fee
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
          eyebrow="Settlement"
          title="You are paid to the account on your profile."
          copy="KYC happens once. After that, confirmation or the 72-hour window releases the payout. A larger contract is the next record, with its own funding."
          mark="shield"
        />
      </Band>

      <Band className="bg-surface">
        <Intro title="What the record guarantees">
          A contract larger than {naira(ESCROW_MAX)} is the next record, with its own funding and its own confirmation.
        </Intro>
        <div className="mt-8 rounded-card border border-line bg-canvas p-6">
          <KeyValue
            rows={[
              { label: "Notified when", value: "The escrow is funded" },
              { label: "One record holds", value: `Up to ${naira(ESCROW_MAX)}` },
              { label: "Window per stage", value: `1 to ${STAGE_DAYS_MAX} days` },
              { label: "Confirmation window", value: "72 hours, then auto-release" },
              { label: "Published fee", value: `1.5 percent, ${naira(FEE_MIN)}–${naira(FEE_MAX)}` },
              { label: "Large accounts", value: "Own percentage, floor, and cap" },
              { label: "Paid to", value: "Your Nigerian bank account" },
            ]}
          />
        </div>
      </Band>

      <CtaBand
        title="Receive with Assurra."
        copy="Create a merchant account. Settlement waits on KYC, and work waits on funding."
        primary={{ href: "/register?role=merchant", label: "Become a merchant" }}
        secondary={{ href: "/pricing", label: "See the fee" }}
      />
    </div>
  );
}

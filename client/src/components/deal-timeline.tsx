import { Icon } from "@/components/icons";
import { Meta, StatusPill } from "@/components/ui";
import { shortDateTime, timeLeft } from "@/lib/format";

const STEPS = [
  { label: "Agreed", hint: "The payer opens the record." },
  { label: "Paid in", hint: "The payment is held." },
  { label: "In progress", hint: "The merchant records a reference for the goods, the service, or the stage." },
  { label: "Handed over", hint: "The merchant marks this stage complete." },
  { label: "Confirmed", hint: "The payer accepts the stage." },
  { label: "Released", hint: "The merchant is paid." },
];

const RANK: Record<string, number> = {
  INITIATED: 0,
  FUNDED: 1,
  MERCHANT_NOTIFIED: 1,
  SHIPPED: 2,
  DELIVERED: 3,
  CONFIRMED: 4,
  RELEASED: 5,
  AUTO_RELEASED: 5,
  RESOLVED_MERCHANT: 5,
};

const OUTCOME: Record<string, { label: string; detail: string }> = {
  DISPUTED: {
    label: "Disputed",
    detail: "The payer opened a dispute. The money stays on this record until it is resolved.",
  },
  UNDER_REVIEW: {
    label: "Under review",
    detail: "The dispute on this record is under review.",
  },
  CANCELLED: {
    label: "Cancelled",
    detail: "This escrow was cancelled before the money was released.",
  },
  REFUNDED: {
    label: "Refunded",
    detail: "The payment on this escrow was refunded to the payer.",
  },
  RESOLVED_CUSTOMER: {
    label: "Resolved for the payer",
    detail: "The dispute was decided in the payer's favour.",
  },
};

export function DealTimeline({ status }: { status: string }) {
  const outcome = OUTCOME[status];

  if (outcome) {
    return (
      <div className="rounded-card border border-line bg-surface p-5 shadow-card">
        <div className="flex items-center justify-between gap-3">
          <p className="text-[1.0625rem] font-semibold">{outcome.label}</p>
          <StatusPill value={status} />
        </div>
        <p className="mt-2 text-[0.9375rem] text-muted">{outcome.detail}</p>
        <p className="mt-3">
          <Meta>{status.replaceAll("_", " ")}</Meta>
        </p>
      </div>
    );
  }

  const current = RANK[status] ?? 0;

  return (
    <ol className="rounded-card border border-line bg-surface p-6 shadow-card">
      {STEPS.map((step, index) => {
        const state = index < current ? "done" : index === current ? "now" : "later";
        return (
          <li key={step.label} className="grid grid-cols-[1.5rem_1fr] gap-3">
            <div className="flex flex-col items-center">
              {state === "done" ? (
                <span className="grid size-5 place-items-center rounded-full bg-primary text-white">
                  <Icon name="check" className="size-3" />
                </span>
              ) : state === "now" ? (
                <span className="grid size-5 place-items-center rounded-full border-2 border-primary bg-surface">
                  <span className="size-2 rounded-full bg-primary" />
                </span>
              ) : (
                <span className="mt-0.5 size-4 rounded-full border border-line-strong bg-surface" />
              )}
              {index < STEPS.length - 1 && (
                <span className={`w-px flex-1 ${index < current ? "bg-primary" : "bg-line"}`} />
              )}
            </div>
            <div className="pb-5 last:pb-0">
              <div className="flex flex-wrap items-center gap-2">
                <p className={`text-[0.9375rem] font-semibold ${state === "later" ? "text-faint" : "text-ink"}`}>
                  {step.label}
                </p>
                {state === "now" && (
                  <span className="rounded-full bg-primary-tint px-2 py-0.5 text-[0.6875rem] font-semibold text-primary-deep">
                    Now
                  </span>
                )}
              </div>
              <p className={`mt-0.5 text-[0.875rem] ${state === "later" ? "text-faint" : "text-muted"}`}>
                {step.hint}
              </p>
            </div>
          </li>
        );
      })}
    </ol>
  );
}

/** A real server deadline, never an invented one. */
export function Deadline({ label, iso }: { label: string; iso?: string | null }) {
  const when = shortDateTime(iso);
  if (!when) return null;
  const left = timeLeft(iso);
  const overdue = left === "overdue";
  return (
    <div
      className={`flex items-start gap-2 rounded-field px-3 py-2.5 text-[0.875rem] ${
        overdue ? "bg-warn-tint text-warn" : "bg-subtle text-muted"
      }`}
    >
      <Icon name="clock" className="mt-0.5 size-4 shrink-0" />
      <span>
        <span className="font-semibold">
          {label} {when}
        </span>
        {overdue ? " · the window has passed" : left ? ` · ${left}` : null}
      </span>
    </div>
  );
}

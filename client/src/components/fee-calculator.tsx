"use client";

import { useMemo, useState } from "react";
import { FEE_MAX, FEE_MIN, FEE_RATE, ESCROW_MAX, escrowFee } from "@/lib/format";
import { Field, Money, inputClass } from "@/components/ui";

export function FeeCalculator() {
  const [raw, setRaw] = useState("250000");
  const amount = Number(raw);
  const fee = useMemo(() => escrowFee(amount), [amount]);
  const valid = Number.isFinite(amount) && amount >= 100 && amount <= ESCROW_MAX;
  const atFloor = valid && fee === FEE_MIN && amount * FEE_RATE < FEE_MIN;
  const atCap = valid && fee === FEE_MAX && amount * FEE_RATE > FEE_MAX;

  return (
    <div className="rounded-card border border-line bg-surface p-6 shadow-card md:p-7">
      <h2 className="text-[1.125rem] font-semibold">Estimate a fee</h2>
      <p className="mt-1 text-[0.9375rem] text-muted">
        {(FEE_RATE * 100).toFixed(1)} percent of the amount, at least <Money value={FEE_MIN} whole /> and at most{" "}
        <Money value={FEE_MAX} whole />.
      </p>
      <div className="mt-5">
        <Field label="Escrow amount (NGN)" hint="One record holds ₦100 to ₦10,000,000.">
          <input
            className={`${inputClass} num`}
            inputMode="decimal"
            value={raw}
            onChange={(event) => setRaw(event.target.value)}
          />
        </Field>
      </div>
      <dl className="mt-5 grid grid-cols-2 gap-px overflow-hidden rounded-field border border-line bg-line">
        <div className="bg-subtle p-4">
          <dt className="text-[0.875rem] text-muted">Escrow fee</dt>
          <dd className="mt-1 text-[1.5rem] font-semibold tracking-tight">
            <Money value={valid ? fee : 0} whole />
          </dd>
        </div>
        <div className="bg-subtle p-4">
          <dt className="text-[0.875rem] text-muted">Merchant receives</dt>
          <dd className="mt-1 text-[1.5rem] font-semibold tracking-tight">
            <Money value={valid ? Math.max(amount - fee, 0) : 0} whole />
          </dd>
        </div>
      </dl>
      <p className="mt-3 text-[0.875rem] text-faint">
        {!valid
          ? "Enter an amount between ₦100 and ₦10,000,000."
          : atFloor
            ? "1.5 percent falls under the floor here, so the floor applies."
            : atCap
              ? "1.5 percent passes the cap here, so the cap applies."
              : "1.5 percent, inside the floor and the cap."}
      </p>
    </div>
  );
}

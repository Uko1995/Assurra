"use client";

import { useMemo, useState } from "react";

const FEE_RATE = 0.015;
const MIN_AMOUNT = 1000;

function formatNgn(value: number) {
  return new Intl.NumberFormat("en-NG", {
    style: "currency",
    currency: "NGN",
    maximumFractionDigits: 2,
  }).format(value);
}

export function FeeCalculator() {
  const [amount, setAmount] = useState(100000);

  const breakdown = useMemo(() => {
    const product = Number.isFinite(amount) ? Math.max(0, amount) : 0;
    const fee = product * FEE_RATE;
    return {
      product,
      fee,
      totalCharge: product + fee,
      merchantNet: product - fee,
      belowMin: product > 0 && product < MIN_AMOUNT,
    };
  }, [amount]);

  return (
    <div className="rounded-[16px] border border-border-subtle bg-surface-lowest p-6 shadow-[var(--shadow-card)]">
      <h3 className="font-serif text-2xl font-semibold text-on-surface">Fee example</h3>
      <p className="mt-2 text-sm text-muted">
        Default escrow fee is 1.5%. Customer pays product + fee. Merchant receives net after fee.
      </p>

      <label className="mt-6 block text-xs font-semibold uppercase tracking-[0.06em] text-on-surface">
        Product amount (₦)
        <input
          type="number"
          min={0}
          step={1000}
          value={amount}
          onChange={(e) => setAmount(Number(e.target.value))}
          className="mt-2 w-full rounded-[12px] border border-outline-variant bg-[#f2f0e4] px-4 py-3 text-base font-medium text-on-surface outline-none focus:border-2 focus:border-primary"
        />
      </label>

      {breakdown.belowMin ? (
        <p className="mt-3 text-sm text-error">Minimum transaction amount is ₦1,000.</p>
      ) : null}

      <dl className="mt-6 space-y-3 text-sm">
        <div className="flex justify-between gap-4">
          <dt className="text-muted">Escrow fee (1.5%)</dt>
          <dd className="font-semibold">{formatNgn(breakdown.fee)}</dd>
        </div>
        <div className="flex justify-between gap-4">
          <dt className="text-muted">Customer total charge</dt>
          <dd className="font-semibold">{formatNgn(breakdown.totalCharge)}</dd>
        </div>
        <div className="flex justify-between gap-4 border-t border-border-subtle pt-3">
          <dt className="text-muted">Merchant net payout</dt>
          <dd className="font-semibold text-primary">{formatNgn(breakdown.merchantNet)}</dd>
        </div>
      </dl>
    </div>
  );
}

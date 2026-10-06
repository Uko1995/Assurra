"use client";

import { FormEvent, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { DashboardShell } from "@/components/dashboard-shell";
import { Alert, Button, Card, Field, KeyValue, Money, SectionHead, inputClass } from "@/components/ui";
import { api, ApiError } from "@/lib/api";
import { ESCROW_MAX, STAGE_DAYS_MAX, escrowFee, naira } from "@/lib/format";
import type { Escrow } from "@/lib/types";

export default function NewEscrowPage() {
  const router = useRouter();
  const [error, setError] = useState<string | null>(null);
  const [pending, setPending] = useState(false);
  const [amount, setAmount] = useState("");

  const preview = useMemo(() => {
    const value = Number(amount);
    if (!Number.isFinite(value) || value < 100 || value > ESCROW_MAX) return null;
    const fee = escrowFee(value);
    return { value, fee, net: Math.max(value - fee, 0) };
  }, [amount]);

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setPending(true);
    setError(null);
    const form = new FormData(event.currentTarget);
    try {
      const escrow = await api<Escrow>("/api/v1/escrow", {
        headers: { "X-Idempotency-Key": crypto.randomUUID() },
        body: {
          merchantId: form.get("merchantId"),
          amount: Number(form.get("amount")),
          productDescription: form.get("productDescription"),
          productQuantity: Number(form.get("productQuantity") || 1),
          agreedDeliveryDays: Number(form.get("agreedDeliveryDays") || 7),
        },
      });
      router.replace(`/customer/escrow/${escrow.reference}`);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not create the escrow");
    } finally {
      setPending(false);
    }
  }

  return (
    <DashboardShell
      role="CUSTOMER"
      title="New escrow"
      lede="One record covers one stage. A longer engagement is a sequence of records."
    >
      <form onSubmit={onSubmit} className="grid gap-6 lg:grid-cols-[1fr_17rem] lg:items-start">
        <Card className="space-y-5">
          <SectionHead title="What is being paid for" />
          <Field label="Merchant ID" hint="Ask the merchant for the id on their account.">
            <input className={`${inputClass} num`} name="merchantId" required />
          </Field>
          <Field label="What is this stage?" hint="Goods, a service, or one stage of a contract. Up to 1000 characters.">
            <textarea
              className={inputClass}
              name="productDescription"
              required
              maxLength={1000}
              rows={4}
              placeholder="Delivery of 40 bags of cement to the Yaba site"
            />
          </Field>
          <div className="grid gap-5 sm:grid-cols-2">
            <Field label="Amount (NGN)" hint={`₦100 to ${naira(ESCROW_MAX)}.`}>
              <input
                className={`${inputClass} num`}
                name="amount"
                type="number"
                min={100}
                max={ESCROW_MAX}
                step="0.01"
                required
                value={amount}
                onChange={(event) => setAmount(event.target.value)}
              />
            </Field>
            <Field label="Quantity">
              <input className={`${inputClass} num`} name="productQuantity" type="number" min={1} defaultValue={1} />
            </Field>
          </div>
          <Field
            label="Window for this stage (days)"
            hint={`1 to ${STAGE_DAYS_MAX} days. A longer contract is the next record.`}
          >
            <input
              className={`${inputClass} num`}
              name="agreedDeliveryDays"
              type="number"
              min={1}
              max={STAGE_DAYS_MAX}
              defaultValue={7}
            />
          </Field>
          {error && <Alert>{error}</Alert>}
          <Button type="submit" disabled={pending}>
            {pending ? "Creating…" : "Create escrow"}
          </Button>
        </Card>

        <Card className="space-y-4 lg:sticky lg:top-28">
          <h2 className="text-[0.9375rem] font-semibold">Before you pay</h2>
          {preview ? (
            <KeyValue
              rows={[
                { label: "You pay", value: <Money value={preview.value} /> },
                { label: "Escrow fee", value: <Money value={preview.fee} /> },
                { label: "Merchant receives", value: <Money value={preview.net} /> },
              ]}
            />
          ) : (
            <p className="text-[0.875rem] text-faint">
              Enter an amount between ₦100 and {naira(ESCROW_MAX)} to see the fee.
            </p>
          )}
          <p className="border-t border-line pt-3 text-[0.875rem] text-muted">
            Creating the record does not charge anything. The fee applies when the record is funded, and the merchant
            is only notified after that.
          </p>
          <p className="text-[0.875rem] text-muted">An unfunded record expires after 24 hours.</p>
        </Card>
      </form>
    </DashboardShell>
  );
}

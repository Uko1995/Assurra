"use client";

import { FormEvent, useState } from "react";
import { DashboardShell } from "@/components/dashboard-shell";
import { Alert, Button, Card, Field, SectionHead, inputClass } from "@/components/ui";
import { api, ApiError } from "@/lib/api";

export default function AdminRefundsPage() {
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [pending, setPending] = useState(false);

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = event.currentTarget;
    const data = new FormData(form);
    const reference = String(data.get("reference"));
    const reason = encodeURIComponent(String(data.get("reason")));
    setPending(true);
    try {
      await api(`/api/v1/admin/payments/${reference}/refund?reason=${reason}`, { method: "POST" });
      setError(null);
      setMessage(`Refund requested for ${reference}.`);
      form.reset();
    } catch (err) {
      setMessage(null);
      setError(err instanceof ApiError ? err.message : "Refund failed");
    } finally {
      setPending(false);
    }
  }

  return (
    <DashboardShell role="ADMIN" title="Refund a payment" lede="One action, by payment reference.">
      <Card className="max-w-xl space-y-5">
        <SectionHead
          title="Payment reference"
          description="There is no admin list of payments. Take the reference from the escrow record or the payment notification."
        />
        <form onSubmit={onSubmit} className="space-y-4">
          <Field label="Payment reference">
            <input className={`${inputClass} num`} name="reference" required />
          </Field>
          <Field label="Reason" hint="Recorded against the payment.">
            <input className={inputClass} name="reason" required />
          </Field>
          {message && <Alert tone="success">{message}</Alert>}
          {error && <Alert>{error}</Alert>}
          <Button type="submit" variant="danger" disabled={pending}>
            {pending ? "Requesting…" : "Refund this payment"}
          </Button>
        </form>
      </Card>
    </DashboardShell>
  );
}

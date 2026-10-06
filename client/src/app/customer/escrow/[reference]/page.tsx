"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { FormEvent, use, useState } from "react";
import { DashboardShell } from "@/components/dashboard-shell";
import { Deadline, DealTimeline } from "@/components/deal-timeline";
import {
  Alert,
  Amount,
  Button,
  Card,
  Field,
  KeyValue,
  Meta,
  Money,
  SectionHead,
  Skeleton,
  StatusPill,
  inputClass,
} from "@/components/ui";
import { api, ApiError } from "@/lib/api";
import { useAuth } from "@/lib/auth-store";
import { labelize, naira, shortDateTime, statusSentence } from "@/lib/format";
import type { Escrow } from "@/lib/types";

const reasons = [
  "ITEM_NOT_RECEIVED",
  "ITEM_NOT_AS_DESCRIBED",
  "DAMAGED_ITEM",
  "WRONG_ITEM",
  "LATE_DELIVERY",
  "FRAUDULENT_SELLER",
  "PAYMENT_ISSUE",
  "OTHER",
];

type InitializePayment = { paymentLink?: string; reference: string };

export default function CustomerEscrowPage({ params }: { params: Promise<{ reference: string }> }) {
  const { reference } = use(params);
  return (
    <DashboardShell role="CUSTOMER" title="Escrow record" lede={reference}>
      <EscrowDetail reference={reference} />
    </DashboardShell>
  );
}

function EscrowDetail({ reference }: { reference: string }) {
  const [error, setError] = useState<string | null>(null);
  const [disputeOpen, setDisputeOpen] = useState(false);
  const user = useAuth((state) => state.user);
  const client = useQueryClient();

  const query = useQuery({
    queryKey: ["escrow", reference],
    queryFn: () => api<Escrow>(`/api/v1/escrow/${reference}`),
  });

  const mutate = useMutation({
    mutationFn: async (action: () => Promise<unknown>) => action(),
    onSuccess: () => {
      setError(null);
      setDisputeOpen(false);
      void client.invalidateQueries({ queryKey: ["escrow", reference] });
    },
    onError: (err: Error) => setError(err instanceof ApiError ? err.message : err.message),
  });

  if (query.isLoading) {
    return (
      <div className="space-y-4">
        <Skeleton className="h-9 w-48" />
        <Skeleton className="h-4 w-64" />
        <Skeleton className="h-64 w-full rounded-card" />
      </div>
    );
  }
  if (query.isError || !query.data) {
    return <Alert>{(query.error as Error)?.message ?? "Escrow not found"}</Alert>;
  }

  const escrow = query.data;
  const canDispute = ["FUNDED", "SHIPPED", "DELIVERED"].includes(escrow.status);
  const canCancel = ["INITIATED", "FUNDED", "MERCHANT_NOTIFIED"].includes(escrow.status);

  async function pay() {
    const payment = await api<InitializePayment>("/api/v1/payments", {
      body: {
        escrowReference: escrow.reference,
        customerEmail: user?.email,
        amount: escrow.amount,
        currency: escrow.currency || "NGN",
        callbackUrl: window.location.href,
        idempotencyKey: crypto.randomUUID(),
      },
    });
    if (payment.paymentLink) window.location.assign(payment.paymentLink);
  }

  function openDispute(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    mutate.mutate(() =>
      api("/api/v1/disputes", {
        body: {
          escrowReference: escrow.reference,
          customerId: escrow.customerId,
          merchantId: escrow.merchantId,
          raisedBy: user?.id,
          reason: form.get("reason"),
          description: form.get("description"),
          desiredOutcome: form.get("desiredOutcome"),
          amountDisputed: Number(form.get("amountDisputed")),
        },
      }),
    );
  }

  const details = [
    { label: "Reference", value: <span className="num">{escrow.reference}</span> },
    { label: "Opened", value: shortDateTime(escrow.createdAt) ?? "—" },
    { label: "Quantity", value: <span className="num">{escrow.productQuantity ?? 1}</span> },
    {
      label: "Window for this stage",
      value: <span className="num">{escrow.agreedDeliveryDays ?? "—"} days</span>,
    },
    { label: "Escrow fee", value: escrow.escrowFee != null ? <Money value={escrow.escrowFee} /> : "—" },
    {
      label: "Merchant receives",
      value: escrow.merchantAmount != null ? <Money value={escrow.merchantAmount} /> : "—",
    },
  ];

  if (escrow.trackingNumber) {
    details.splice(4, 0, {
      label: "Progress reference",
      value: (
        <span>
          <span className="num">{escrow.trackingNumber}</span>
          {escrow.logisticsProvider ? ` · ${escrow.logisticsProvider}` : ""}
        </span>
      ),
    });
  }

  return (
    <div className="space-y-6">
      <Card className="flex flex-wrap items-start justify-between gap-6">
        <div className="min-w-0">
          <p className="max-w-xl text-[0.9375rem] text-muted">{escrow.productDescription}</p>
          <div className="mt-3">
            <Amount value={escrow.amount} label="Held on this record" />
          </div>
          <div className="mt-3 flex flex-wrap items-center gap-2">
            <StatusPill value={escrow.status} />
            <Meta>{escrow.status.replaceAll("_", " ")}</Meta>
          </div>
          <p className="mt-2 text-[0.9375rem] font-medium">{statusSentence(escrow.status, "CUSTOMER")}</p>
        </div>
        <div className="w-full max-w-xs shrink-0 rounded-field bg-subtle p-4">
          <KeyValue
            rows={[
              { label: "Fee", value: escrow.escrowFee != null ? <Money value={escrow.escrowFee} /> : "—" },
              {
                label: "Merchant receives",
                value: escrow.merchantAmount != null ? <Money value={escrow.merchantAmount} /> : "—",
              },
            ]}
          />
        </div>
      </Card>

      <div className="grid gap-6 lg:grid-cols-[1fr_19rem] lg:items-start">
        <div className="space-y-6">
          <DealTimeline status={escrow.status} />
          <Card>
            <SectionHead title="The record" />
            <div className="mt-4">
              <KeyValue rows={details} />
            </div>
          </Card>
        </div>

        <Card className="space-y-3 lg:sticky lg:top-28">
          <h2 className="text-[0.9375rem] font-semibold">Next action</h2>
          {error && <Alert>{error}</Alert>}

          {escrow.status === "INITIATED" && (
            <>
              <Deadline label="Pay by" iso={escrow.paymentExpiresAt} />
              <Button full disabled={mutate.isPending} onClick={() => mutate.mutate(pay)}>
                Pay {naira(escrow.amount)}
              </Button>
            </>
          )}

          {escrow.status === "DELIVERED" && (
            <>
              <Deadline label="Confirm by" iso={escrow.confirmationDeadline} />
              <Button
                full
                disabled={mutate.isPending}
                onClick={() =>
                  mutate.mutate(() => api(`/api/v1/escrow/${escrow.reference}/confirm`, { method: "POST" }))
                }
              >
                Confirm this stage
              </Button>
              <p className="text-[0.875rem] text-muted">
                Confirming releases the merchant. If the window passes with no answer, the escrow auto-releases.
              </p>
            </>
          )}

          {escrow.status !== "INITIATED" && escrow.status !== "DELIVERED" && (
            <p className="text-[0.9375rem] text-muted">{statusSentence(escrow.status, "CUSTOMER")}</p>
          )}

          {canCancel && (
            <Button
              variant="secondary"
              full
              disabled={mutate.isPending}
              onClick={() => mutate.mutate(() => api(`/api/v1/escrow/${escrow.reference}/cancel`, { method: "POST" }))}
            >
              Cancel this escrow
            </Button>
          )}

          {canDispute && (
            <button
              type="button"
              className="w-full rounded-field px-1 py-1.5 text-left text-[0.875rem] font-semibold text-danger hover:bg-danger-tint"
              onClick={() => setDisputeOpen((open) => !open)}
            >
              {disputeOpen ? "Hide the dispute form" : "Report a problem"}
            </button>
          )}

          {disputeOpen && (
            <form onSubmit={openDispute} className="space-y-3 border-t border-line pt-3">
              <Field label="Reason">
                <select className={inputClass} name="reason" defaultValue="ITEM_NOT_RECEIVED">
                  {reasons.map((reason) => (
                    <option key={reason} value={reason}>
                      {labelize(reason)}
                    </option>
                  ))}
                </select>
              </Field>
              <Field label="What happened?">
                <textarea className={inputClass} name="description" required rows={4} />
              </Field>
              <Field label="What should happen next?">
                <input className={inputClass} name="desiredOutcome" placeholder="Refund, or finish the stage" />
              </Field>
              <Field label="Amount in question" hint="The money stays on the record until the dispute is decided.">
                <input
                  className={`${inputClass} num`}
                  name="amountDisputed"
                  type="number"
                  min={1}
                  step="0.01"
                  defaultValue={escrow.amount}
                  required
                />
              </Field>
              <Button type="submit" variant="danger" full disabled={mutate.isPending}>
                Open the dispute
              </Button>
            </form>
          )}
        </Card>
      </div>
    </div>
  );
}

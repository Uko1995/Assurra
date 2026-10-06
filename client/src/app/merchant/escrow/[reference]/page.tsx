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
import { shortDateTime, statusSentence } from "@/lib/format";
import type { Escrow } from "@/lib/types";

export default function MerchantEscrowPage({ params }: { params: Promise<{ reference: string }> }) {
  const { reference } = use(params);
  return (
    <DashboardShell role="MERCHANT" title="Escrow record" lede={reference}>
      <Detail reference={reference} />
    </DashboardShell>
  );
}

function Detail({ reference }: { reference: string }) {
  const [error, setError] = useState<string | null>(null);
  const client = useQueryClient();
  const query = useQuery({
    queryKey: ["escrow", reference],
    queryFn: () => api<Escrow>(`/api/v1/escrow/${reference}`),
  });
  const action = useMutation({
    mutationFn: (run: () => Promise<unknown>) => run(),
    onSuccess: () => {
      setError(null);
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
  if (!query.data) return <Alert>{(query.error as Error)?.message ?? "Escrow not found"}</Alert>;

  const escrow = query.data;
  const canShip = ["FUNDED", "MERCHANT_NOTIFIED"].includes(escrow.status);

  function ship(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const day = String(form.get("estimatedDeliveryDate") || "");
    action.mutate(() =>
      api(`/api/v1/escrow/${escrow.reference}/ship`, {
        body: {
          trackingNumber: form.get("trackingNumber"),
          logisticsProvider: form.get("logisticsProvider"),
          estimatedDeliveryDate: day ? `${day}T00:00:00` : undefined,
        },
      }),
    );
  }

  return (
    <div className="space-y-6">
      <Card className="flex flex-wrap items-start justify-between gap-6">
        <div className="min-w-0">
          <p className="max-w-xl text-[0.9375rem] text-muted">{escrow.productDescription}</p>
          <div className="mt-3">
            <Amount value={escrow.merchantAmount ?? escrow.amount} label="You receive" />
          </div>
          <div className="mt-3 flex flex-wrap items-center gap-2">
            <StatusPill value={escrow.status} />
            <Meta>{escrow.status.replaceAll("_", " ")}</Meta>
          </div>
          <p className="mt-2 text-[0.9375rem] font-medium">{statusSentence(escrow.status, "MERCHANT")}</p>
        </div>
        <div className="w-full max-w-xs shrink-0 rounded-field bg-subtle p-4">
          <KeyValue
            rows={[
              { label: "Payer pays", value: <Money value={escrow.amount} /> },
              { label: "Escrow fee", value: escrow.escrowFee != null ? <Money value={escrow.escrowFee} /> : "—" },
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
              <KeyValue
                rows={[
                  { label: "Reference", value: <span className="num">{escrow.reference}</span> },
                  { label: "Opened", value: shortDateTime(escrow.createdAt) ?? "—" },
                  { label: "Quantity", value: <span className="num">{escrow.productQuantity ?? 1}</span> },
                  {
                    label: "Window for this stage",
                    value: <span className="num">{escrow.agreedDeliveryDays ?? "—"} days</span>,
                  },
                  {
                    label: "Progress reference",
                    value: escrow.trackingNumber ? (
                      <span>
                        <span className="num">{escrow.trackingNumber}</span>
                        {escrow.logisticsProvider ? ` · ${escrow.logisticsProvider}` : ""}
                      </span>
                    ) : (
                      "Not recorded yet"
                    ),
                  },
                ]}
              />
            </div>
          </Card>
        </div>

        <Card className="space-y-3 lg:sticky lg:top-28">
          <h2 className="text-[0.9375rem] font-semibold">Next action</h2>
          {error && <Alert>{error}</Alert>}

          {canShip && (
            <form onSubmit={ship} className="space-y-3">
              <Field label="Reference" hint="A waybill, or a note both sides recognise.">
                <input className={inputClass} name="trackingNumber" required placeholder="Waybill or stage note" />
              </Field>
              <Field label="Who is doing the work">
                <input className={inputClass} name="logisticsProvider" required placeholder="Carrier, firm, or team" />
              </Field>
              <Field label="Expected completion">
                <input className={inputClass} name="estimatedDeliveryDate" type="date" />
              </Field>
              <Button type="submit" full disabled={action.isPending}>
                Save progress
              </Button>
            </form>
          )}

          {escrow.status === "SHIPPED" && (
            <>
              <Button
                full
                disabled={action.isPending}
                onClick={() =>
                  action.mutate(() => api(`/api/v1/escrow/${escrow.reference}/deliver`, { method: "POST" }))
                }
              >
                Mark this stage complete
              </Button>
              <p className="text-[0.875rem] text-muted">
                The payer then has 72 hours to confirm. After that the escrow auto-releases to you.
              </p>
            </>
          )}

          {escrow.status === "DELIVERED" && <Deadline label="Payer confirms by" iso={escrow.confirmationDeadline} />}
          {escrow.status === "INITIATED" && <Deadline label="Payment expires" iso={escrow.paymentExpiresAt} />}

          {!canShip && escrow.status !== "SHIPPED" && (
            <p className="text-[0.9375rem] text-muted">{statusSentence(escrow.status, "MERCHANT")}</p>
          )}
        </Card>
      </div>
    </div>
  );
}

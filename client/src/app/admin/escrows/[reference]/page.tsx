"use client";

import { use } from "react";
import { useQuery } from "@tanstack/react-query";
import { DashboardShell } from "@/components/dashboard-shell";
import { Deadline, DealTimeline } from "@/components/deal-timeline";
import {
  Alert,
  Amount,
  Card,
  KeyValue,
  Meta,
  Money,
  SectionHead,
  Skeleton,
  StatusPill,
} from "@/components/ui";
import { api } from "@/lib/api";
import { shortDateTime, statusSentence } from "@/lib/format";
import type { Escrow } from "@/lib/types";

export default function AdminEscrowDetail({ params }: { params: Promise<{ reference: string }> }) {
  const { reference } = use(params);
  return (
    <DashboardShell role="ADMIN" title="Escrow record" lede={reference}>
      <Detail reference={reference} />
    </DashboardShell>
  );
}

function Detail({ reference }: { reference: string }) {
  const query = useQuery({
    queryKey: ["admin-escrow", reference],
    queryFn: () => api<Escrow>(`/api/v1/admin/escrows/${reference}`),
  });

  if (query.isLoading) {
    return (
      <div className="space-y-4">
        <Skeleton className="h-9 w-48" />
        <Skeleton className="h-64 w-full rounded-card" />
      </div>
    );
  }
  if (!query.data) return <Alert>{(query.error as Error)?.message ?? "Escrow not found"}</Alert>;

  const escrow = query.data;

  return (
    <div className="space-y-6">
      <Card className="flex flex-wrap items-start justify-between gap-6">
        <div className="min-w-0">
          <p className="max-w-xl text-[0.9375rem] text-muted">{escrow.productDescription}</p>
          <div className="mt-3">
            <Amount value={escrow.amount} label="On the record" />
          </div>
          <div className="mt-3 flex flex-wrap items-center gap-2">
            <StatusPill value={escrow.status} />
            <Meta>{escrow.status.replaceAll("_", " ")}</Meta>
          </div>
          <p className="mt-2 text-[0.9375rem] font-medium">{statusSentence(escrow.status, "ADMIN")}</p>
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
        <DealTimeline status={escrow.status} />
        <div className="space-y-6">
          <Card>
            <SectionHead title="Parties" />
            <div className="mt-4">
              <KeyValue
                rows={[
                  { label: "Payer", value: <span className="num text-[0.8125rem]">{escrow.customerId}</span> },
                  { label: "Merchant", value: <span className="num text-[0.8125rem]">{escrow.merchantId}</span> },
                ]}
              />
            </div>
          </Card>
          <Card>
            <SectionHead title="The record" />
            <div className="mt-4">
              <KeyValue
                rows={[
                  { label: "Reference", value: <span className="num">{escrow.reference}</span> },
                  { label: "Opened", value: shortDateTime(escrow.createdAt) ?? "—" },
                  { label: "Quantity", value: <span className="num">{escrow.productQuantity ?? 1}</span> },
                  {
                    label: "Window",
                    value: <span className="num">{escrow.agreedDeliveryDays ?? "—"} days</span>,
                  },
                  {
                    label: "Progress reference",
                    value: escrow.trackingNumber ? (
                      <span className="num">{escrow.trackingNumber}</span>
                    ) : (
                      "Not recorded"
                    ),
                  },
                ]}
              />
            </div>
          </Card>
          <div className="space-y-2">
            <Deadline label="Payment expires" iso={escrow.paymentExpiresAt} />
            <Deadline label="Confirm by" iso={escrow.confirmationDeadline} />
            <Deadline label="Auto-releases" iso={escrow.autoReleaseAt} />
          </div>
        </div>
      </div>
    </div>
  );
}

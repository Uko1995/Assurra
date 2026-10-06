"use client";

import Link from "next/link";
import { useQuery } from "@tanstack/react-query";
import { DashboardShell } from "@/components/dashboard-shell";
import { Icon, type IconName } from "@/components/icons";
import { Card, Money, Skeleton } from "@/components/ui";
import { api } from "@/lib/api";
import type { AmlAlert, Dispute, KycMerchant, PageResult, Payout } from "@/lib/types";

export default function AdminHomePage() {
  const kycPending = useQuery({
    queryKey: ["kyc", "pending"],
    queryFn: () => api<PageResult<KycMerchant>>("/api/v1/admin/kyc/pending?page=0&size=5"),
  });
  const kycReview = useQuery({
    queryKey: ["kyc", "under-review"],
    queryFn: () => api<PageResult<KycMerchant>>("/api/v1/admin/kyc/under-review?page=0&size=5"),
  });
  const disputes = useQuery({
    queryKey: ["admin-disputes"],
    queryFn: () => api<PageResult<Dispute>>("/api/v1/admin/disputes?page=0&size=20"),
  });
  const payouts = useQuery({
    queryKey: ["admin-payouts"],
    queryFn: () => api<PageResult<Payout>>("/api/v1/admin/payouts?page=0&size=20"),
  });
  const aml = useQuery({
    queryKey: ["aml"],
    queryFn: () => api<PageResult<AmlAlert>>("/api/v1/admin/aml-alerts?page=0&size=20"),
  });

  const kycRows = [...(kycPending.data?.content ?? []), ...(kycReview.data?.content ?? [])].slice(0, 3);
  const kycCount = (kycPending.data?.totalElements ?? 0) + (kycReview.data?.totalElements ?? 0);
  const openDisputes = (disputes.data?.content ?? []).filter(
    (row) => row.status === "OPEN" || row.status === "UNDER_REVIEW",
  );
  const retryPayouts = (payouts.data?.content ?? []).filter(
    (row) => row.status === "FAILED" || row.status === "REVERSED",
  );
  const openAlerts = (aml.data?.content ?? []).filter((row) => row.status === "OPEN");

  return (
    <DashboardShell role="ADMIN" title="Overview" lede="Four queues. Everything else is a record you can look up.">
      <div className="grid gap-4 md:grid-cols-2">
        <Queue
          href="/admin/kyc"
          icon="shield"
          title="KYC"
          count={kycCount}
          loading={kycPending.isLoading || kycReview.isLoading}
          error={kycPending.error || kycReview.error}
          empty="No merchants are waiting for review."
          rows={kycRows.map((row) => ({
            id: row.userId,
            title: row.businessName ?? row.fullName,
            detail: row.email,
          }))}
        />
        <Queue
          href="/admin/disputes"
          icon="flag"
          title="Disputes"
          count={openDisputes.length}
          loading={disputes.isLoading}
          error={disputes.error}
          empty="No open disputes in this page of results."
          rows={openDisputes.slice(0, 3).map((row) => ({
            id: row.reference,
            title: row.reference,
            detail: row.escrowReference,
            amount: row.amountDisputed,
          }))}
        />
        <Queue
          href="/admin/payouts"
          icon="bank"
          title="Payouts to retry"
          count={retryPayouts.length}
          loading={payouts.isLoading}
          error={payouts.error}
          empty="No failed payouts in this page of results."
          rows={retryPayouts.slice(0, 3).map((row) => ({
            id: row.reference,
            title: row.reference,
            detail: row.status.toLowerCase(),
            amount: row.amount,
          }))}
        />
        <Queue
          href="/admin/aml"
          icon="alert"
          title="AML alerts"
          count={openAlerts.length}
          loading={aml.isLoading}
          error={aml.error}
          empty="No open alerts in this page of results."
          rows={openAlerts.slice(0, 3).map((row) => ({
            id: row.id,
            title: row.alertType.replaceAll("_", " ").toLowerCase(),
            detail: "Open",
            amount: row.amount,
          }))}
        />
      </div>

      <Card className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h2 className="text-[0.9375rem] font-semibold">Settings and one-off actions</h2>
          <p className="mt-1 text-[0.9375rem] text-muted">
            Fee schedules are set per merchant or globally. A refund needs the payment reference.
          </p>
        </div>
        <div className="flex gap-4 text-[0.9375rem] font-semibold text-primary">
          <Link href="/admin/fees">Fee configuration</Link>
          <Link href="/admin/refunds">Refund a payment</Link>
        </div>
      </Card>
    </DashboardShell>
  );
}

type QueueRow = { id: string; title: string; detail: string; amount?: number };

function Queue({
  href,
  icon,
  title,
  count,
  loading,
  error,
  empty,
  rows,
}: {
  href: string;
  icon: IconName;
  title: string;
  count: number;
  loading: boolean;
  error: unknown;
  empty: string;
  rows: QueueRow[];
}) {
  return (
    <Card className={`flex flex-col gap-4 ${count > 0 ? "ring-1 ring-warn/25" : ""}`}>
      <div className="flex items-start justify-between gap-3">
        <div className="flex items-center gap-2.5">
          <span className="grid size-8 place-items-center rounded-field bg-subtle text-muted">
            <Icon name={icon} className="size-4" />
          </span>
          <h2 className="text-[1.0625rem] font-semibold">{title}</h2>
        </div>
        {loading ? (
          <Skeleton className="h-7 w-10" />
        ) : (
          <span
            className={`num rounded-field px-2.5 py-1 text-[1.25rem] font-semibold leading-tight ${
              count > 0 ? "bg-warn-tint text-warn" : "bg-quiet-tint text-muted"
            }`}
          >
            {count}
          </span>
        )}
      </div>

      <div className="flex-1">
        {error ? (
          <p className="text-[0.9375rem] text-danger">{(error as Error).message}</p>
        ) : loading ? (
          <div className="space-y-2.5">
            <Skeleton className="h-3.5 w-40" />
            <Skeleton className="h-3.5 w-32" />
          </div>
        ) : rows.length === 0 ? (
          <p className="text-[0.9375rem] text-faint">{empty}</p>
        ) : (
          <ul className="divide-y divide-line">
            {rows.map((row) => (
              <li key={row.id} className="flex items-baseline justify-between gap-3 py-2 first:pt-0">
                <div className="min-w-0">
                  <p className="truncate text-[0.9375rem] font-medium">{row.title}</p>
                  <p className="truncate text-[0.875rem] text-faint">{row.detail}</p>
                </div>
                {row.amount != null && (
                  <p className="shrink-0 text-[0.9375rem] font-medium">
                    <Money value={row.amount} whole />
                  </p>
                )}
              </li>
            ))}
          </ul>
        )}
      </div>

      <Link href={href} className="text-[0.9375rem] font-semibold text-primary">
        Open queue
      </Link>
    </Card>
  );
}

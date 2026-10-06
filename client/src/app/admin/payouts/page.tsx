"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { DashboardShell } from "@/components/dashboard-shell";
import { Alert, Button, Empty, Money, Row, RowGroup, SkeletonRows, StatusPill } from "@/components/ui";
import { api } from "@/lib/api";
import { shortDateTime } from "@/lib/format";
import type { PageResult, Payout } from "@/lib/types";

const retryable = new Set(["FAILED", "REVERSED"]);

export default function AdminPayoutsPage() {
  const client = useQueryClient();
  const query = useQuery({
    queryKey: ["admin-payouts"],
    queryFn: () => api<PageResult<Payout>>("/api/v1/admin/payouts?page=0&size=20"),
  });
  const retry = useMutation({
    mutationFn: (reference: string) => api(`/api/v1/admin/payouts/${reference}/retry`, { method: "POST" }),
    onSuccess: () => void client.invalidateQueries({ queryKey: ["admin-payouts"] }),
  });

  const rows = query.data?.content ?? [];
  const failed = rows.filter((row) => retryable.has(row.status)).length;

  return (
    <DashboardShell
      role="ADMIN"
      title="Payouts"
      lede={failed > 0 ? `${failed} on this page can be retried.` : "Nothing on this page needs a retry."}
    >
      {query.isError && <Alert>{(query.error as Error).message}</Alert>}
      {retry.isError && <Alert>{(retry.error as Error).message}</Alert>}
      {query.isLoading && <SkeletonRows count={5} />}
      {!query.isLoading && rows.length === 0 && (
        <Empty title="No payouts" mark="inbox">
          No release has produced a payout yet.
        </Empty>
      )}
      {rows.length > 0 && (
        <RowGroup>
          {rows.map((payout) => (
            <Row
              key={payout.reference}
              title={<span className="num">{payout.reference}</span>}
              sub={`Escrow ${payout.escrowReference}`}
              meta={shortDateTime(payout.createdAt) ?? undefined}
              right={
                <>
                  <p className="text-[0.9375rem] font-semibold">
                    <Money value={payout.netAmount ?? payout.amount} whole />
                  </p>
                  <StatusPill value={payout.status} />
                  {retryable.has(payout.status) && (
                    <Button
                      size="sm"
                      variant="secondary"
                      disabled={retry.isPending}
                      onClick={() => retry.mutate(payout.reference)}
                    >
                      Retry
                    </Button>
                  )}
                </>
              }
            />
          ))}
        </RowGroup>
      )}
    </DashboardShell>
  );
}

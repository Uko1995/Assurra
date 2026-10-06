"use client";

import { useQuery } from "@tanstack/react-query";
import { DashboardShell } from "@/components/dashboard-shell";
import { Alert, Empty, Money, Row, RowGroup, SkeletonRows, StatusPill } from "@/components/ui";
import { api } from "@/lib/api";
import { shortDateTime } from "@/lib/format";
import type { PageResult, Payout } from "@/lib/types";

export default function MerchantPayoutsPage() {
  const query = useQuery({
    queryKey: ["payouts"],
    queryFn: () => api<PageResult<Payout>>("/api/v1/payouts?page=0&size=20"),
  });

  const rows = query.data?.content ?? [];

  return (
    <DashboardShell
      role="MERCHANT"
      title="Payouts"
      lede="A payout starts when the payer confirms, or when the confirmation window passes."
    >
      {query.isError && <Alert>{(query.error as Error).message}</Alert>}
      {query.isLoading && <SkeletonRows count={5} />}
      {!query.isLoading && rows.length === 0 && (
        <Empty title="No payouts yet" mark="inbox">
          A payout appears here after a record is released to you.
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
                  {payout.fee != null && (
                    <span className="text-[0.8125rem] text-faint">
                      Fee <Money value={payout.fee} whole />
                    </span>
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

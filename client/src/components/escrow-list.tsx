"use client";

import { useQuery } from "@tanstack/react-query";
import { Alert, Empty, Money, Row, RowGroup, SkeletonRows, StatusPill } from "@/components/ui";
import { api } from "@/lib/api";
import { statusSentence } from "@/lib/format";
import type { Escrow, PageResult } from "@/lib/types";

export function EscrowList({ basePath, endpoint }: { basePath: string; endpoint: string }) {
  const query = useQuery({
    queryKey: ["escrows", endpoint],
    queryFn: () => api<PageResult<Escrow>>(`${endpoint}?page=0&size=20`),
  });

  if (query.isLoading) return <SkeletonRows count={6} />;
  if (query.isError)
    return (
      <Alert
        action={
          <button type="button" className="font-semibold underline" onClick={() => void query.refetch()}>
            Try again
          </button>
        }
      >
        {(query.error as Error).message}
      </Alert>
    );

  const rows = query.data?.content ?? [];
  if (rows.length === 0)
    return (
      <Empty title="No escrow records" mark="record">
        Nothing has been created on the platform yet.
      </Empty>
    );

  return (
    <div className="space-y-3">
      <p className="text-[0.875rem] text-faint">
        <span className="num">{rows.length}</span> of <span className="num">{query.data?.totalElements ?? 0}</span>{" "}
        records
      </p>
      <RowGroup>
        {rows.map((escrow) => (
          <Row
            key={escrow.reference}
            href={`${basePath}/${escrow.reference}`}
            title={escrow.productDescription}
            sub={statusSentence(escrow.status, "ADMIN")}
            meta={escrow.reference}
            right={
              <>
                <p className="text-[0.9375rem] font-semibold">
                  <Money value={escrow.amount} whole />
                </p>
                <StatusPill value={escrow.status} />
              </>
            }
          />
        ))}
      </RowGroup>
    </div>
  );
}

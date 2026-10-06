"use client";

import { useQuery } from "@tanstack/react-query";
import { Alert, Empty, Money, Row, RowGroup, SkeletonRows, StatusPill } from "@/components/ui";
import { api } from "@/lib/api";
import { escrowBucket, naira, nextActionLabel, statusSentence } from "@/lib/format";
import type { Escrow, PageResult } from "@/lib/types";

const groups = [
  {
    id: "needs" as const,
    title: "Needs you",
    empty: "Nothing is waiting on you.",
  },
  {
    id: "progress" as const,
    title: "In progress",
    empty: "Nothing is moving with the other side right now.",
  },
  {
    id: "closed" as const,
    title: "Closed",
    empty: "No closed records yet.",
  },
];

export function EscrowQueues({
  role,
  endpoint,
  basePath,
  emptyAction,
}: {
  role: "CUSTOMER" | "MERCHANT";
  endpoint: string;
  basePath: string;
  emptyAction?: React.ReactNode;
}) {
  const query = useQuery({
    queryKey: ["escrows", endpoint],
    queryFn: () => api<PageResult<Escrow>>(`${endpoint}?page=0&size=20`),
  });

  if (query.isLoading) return <SkeletonRows count={4} />;
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
  if (rows.length === 0) {
    return (
      <Empty title="No escrow records yet" mark="record" action={emptyAction}>
        {role === "CUSTOMER"
          ? "Open a record to hold the money until a stage is confirmed."
          : "A record appears here once a payer funds one against your account."}
      </Empty>
    );
  }

  return (
    <div className="space-y-8">
      {groups.map((group) => {
        const items = rows.filter((escrow) => escrowBucket(role, escrow.status) === group.id);
        return (
          <section key={group.id} className="space-y-3">
            <div className="flex items-baseline gap-2">
              <h2 className="text-[1.0625rem] font-semibold tracking-tight">{group.title}</h2>
              <span className="num text-[0.875rem] text-faint">{items.length}</span>
            </div>
            {items.length === 0 ? (
              <p className="rounded-card border border-line bg-subtle/50 px-4 py-4 text-[0.9375rem] text-faint">
                {group.empty}
              </p>
            ) : (
              <RowGroup>
                {items.map((escrow) => {
                  const action = nextActionLabel(role, escrow.status);
                  return (
                    <Row
                      key={escrow.reference}
                      href={`${basePath}/${escrow.reference}`}
                      title={escrow.productDescription}
                      sub={statusSentence(escrow.status, role)}
                      meta={escrow.reference}
                      right={
                        <>
                          <p className="text-[0.9375rem] font-semibold">
                            <Money value={escrow.amount} whole />
                          </p>
                          {action ? (
                            <span className="text-[0.875rem] font-semibold text-primary">{action}</span>
                          ) : (
                            <StatusPill value={escrow.status} />
                          )}
                          {escrow.escrowFee != null && group.id === "closed" && (
                            <span className="text-[0.8125rem] text-faint">Fee {naira(escrow.escrowFee)}</span>
                          )}
                        </>
                      }
                    />
                  );
                })}
              </RowGroup>
            )}
          </section>
        );
      })}
    </div>
  );
}

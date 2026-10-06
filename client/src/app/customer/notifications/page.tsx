"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { DashboardShell } from "@/components/dashboard-shell";
import { Alert, Button, Empty, Row, RowGroup, SkeletonRows } from "@/components/ui";
import { api } from "@/lib/api";
import { shortDateTime } from "@/lib/format";
import type { NotificationItem, PageResult } from "@/lib/types";

export default function NotificationsPage() {
  const client = useQueryClient();
  const query = useQuery({
    queryKey: ["notifications"],
    queryFn: () => api<PageResult<NotificationItem>>("/api/v1/notifications?page=0&size=20"),
  });
  const markAll = useMutation({
    mutationFn: () => api("/api/v1/notifications/read-all", { method: "PUT" }),
    onSuccess: () => void client.invalidateQueries({ queryKey: ["notifications"] }),
  });
  const markOne = useMutation({
    mutationFn: (id: string) => api(`/api/v1/notifications/${id}/read`, { method: "PUT" }),
    onSuccess: () => void client.invalidateQueries({ queryKey: ["notifications"] }),
  });

  const rows = query.data?.content ?? [];
  const unread = rows.filter((item) => !item.readAt).length;

  return (
    <DashboardShell
      role="CUSTOMER"
      title="Notifications"
      lede={unread > 0 ? `${unread} unread.` : "Nothing unread."}
      action={
        unread > 0 ? (
          <Button variant="secondary" size="sm" onClick={() => markAll.mutate()} disabled={markAll.isPending}>
            Mark all read
          </Button>
        ) : undefined
      }
    >
      {query.isError && <Alert>{(query.error as Error).message}</Alert>}
      {query.isLoading && <SkeletonRows count={4} />}
      {!query.isLoading && rows.length === 0 && (
        <Empty title="No notifications" mark="inbox">
          Funding, progress, and release each send one.
        </Empty>
      )}
      {rows.length > 0 && (
        <RowGroup>
          {rows.map((item) => (
            <Row
              key={item.id}
              title={
                <span className="flex items-center gap-2">
                  {!item.readAt && <span className="size-2 shrink-0 rounded-full bg-primary" />}
                  {item.subject}
                </span>
              }
              sub={item.body}
              meta={shortDateTime(item.createdAt) ?? undefined}
              right={
                !item.readAt ? (
                  <button
                    type="button"
                    className="text-[0.875rem] font-semibold text-primary"
                    onClick={() => markOne.mutate(item.id)}
                  >
                    Mark read
                  </button>
                ) : undefined
              }
            />
          ))}
        </RowGroup>
      )}
    </DashboardShell>
  );
}

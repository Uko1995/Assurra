"use client";

import { FormEvent, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { DashboardShell } from "@/components/dashboard-shell";
import {
  Alert,
  Button,
  Empty,
  Field,
  Money,
  Row,
  RowGroup,
  SkeletonRows,
  StatusPill,
  inputClass,
} from "@/components/ui";
import { api, ApiError } from "@/lib/api";
import { labelize, shortDateTime, type Tone } from "@/lib/format";
import type { AmlAlert, PageResult } from "@/lib/types";

/** CONFIRMED on an alert means confirmed suspicious, so it reads as danger here. */
const tones: Record<string, Tone> = {
  OPEN: "danger",
  CLEARED: "success",
  ESCALATED: "warn",
  CONFIRMED: "danger",
};

export default function AdminAmlPage() {
  const client = useQueryClient();
  const [error, setError] = useState<string | null>(null);
  const [open, setOpen] = useState<string | null>(null);

  const query = useQuery({
    queryKey: ["aml"],
    queryFn: () => api<PageResult<AmlAlert>>("/api/v1/admin/aml-alerts?page=0&size=20"),
  });

  const act = useMutation({
    mutationFn: (path: string) => api(path, { method: "PUT" }),
    onSuccess: () => {
      setError(null);
      setOpen(null);
      void client.invalidateQueries({ queryKey: ["aml"] });
    },
    onError: (err: Error) => setError(err instanceof ApiError ? err.message : err.message),
  });

  function resolve(event: FormEvent<HTMLFormElement>, id: string) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const notes = encodeURIComponent(String(form.get("notes") || "Resolved from dashboard"));
    const resolution = encodeURIComponent(String(form.get("resolution")));
    act.mutate(`/api/v1/admin/aml-alerts/${id}/resolve?resolution=${resolution}&notes=${notes}`);
  }

  const rows = query.data?.content ?? [];

  return (
    <DashboardShell role="ADMIN" title="AML alerts" lede="Clear, escalate, or confirm. Every answer takes a note.">
      {error && <Alert>{error}</Alert>}
      {query.isLoading && <SkeletonRows count={4} />}
      {!query.isLoading && rows.length === 0 && (
        <Empty title="No alerts" mark="shield">
          No transaction has tripped a rule.
        </Empty>
      )}
      {rows.length > 0 && (
        <RowGroup>
          {rows.map((alert) => (
            <Row
              key={alert.id}
              title={labelize(alert.alertType)}
              sub={alert.notes ?? "No note recorded"}
              meta={shortDateTime(alert.createdAt) ?? undefined}
              right={
                <>
                  <p className="text-[0.9375rem] font-semibold">
                    <Money value={alert.amount} whole />
                  </p>
                  <StatusPill value={alert.status} tone={tones[alert.status]} />
                  {alert.status === "OPEN" && (
                    <button
                      type="button"
                      className="text-[0.875rem] font-semibold text-primary"
                      onClick={() => setOpen((id) => (id === alert.id ? null : alert.id))}
                    >
                      {open === alert.id ? "Close" : "Resolve"}
                    </button>
                  )}
                </>
              }
            >
              {open === alert.id && (
                <form
                  onSubmit={(event) => resolve(event, alert.id)}
                  className="grid gap-3 md:grid-cols-[1fr_1.4fr_auto] md:items-end"
                >
                  <Field label="Resolution">
                    <select className={inputClass} name="resolution" defaultValue="CLEARED">
                      <option value="CLEARED">Cleared</option>
                      <option value="ESCALATED">Escalated</option>
                      <option value="CONFIRMED">Confirmed suspicious</option>
                    </select>
                  </Field>
                  <Field label="Notes">
                    <input className={inputClass} name="notes" />
                  </Field>
                  <Button type="submit" disabled={act.isPending}>
                    Record
                  </Button>
                </form>
              )}
            </Row>
          ))}
        </RowGroup>
      )}
    </DashboardShell>
  );
}

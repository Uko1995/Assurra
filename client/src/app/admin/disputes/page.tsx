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
import { labelize } from "@/lib/format";
import type { Dispute, PageResult } from "@/lib/types";

export default function AdminDisputesPage() {
  const client = useQueryClient();
  const [error, setError] = useState<string | null>(null);
  const [open, setOpen] = useState<string | null>(null);

  const query = useQuery({
    queryKey: ["admin-disputes"],
    queryFn: () => api<PageResult<Dispute>>("/api/v1/admin/disputes?page=0&size=20"),
  });

  const resolve = useMutation({
    mutationFn: (input: { reference: string; resolution: string; resolutionNotes: string }) =>
      api(`/api/v1/admin/disputes/${input.reference}/resolve`, {
        method: "PUT",
        body: { resolution: input.resolution, resolutionNotes: input.resolutionNotes },
      }),
    onSuccess: () => {
      setError(null);
      setOpen(null);
      void client.invalidateQueries({ queryKey: ["admin-disputes"] });
    },
    onError: (err: Error) => setError(err instanceof ApiError ? err.message : err.message),
  });

  function onSubmit(event: FormEvent<HTMLFormElement>, reference: string) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    resolve.mutate({
      reference,
      resolution: String(form.get("resolution")),
      resolutionNotes: String(form.get("resolutionNotes") || ""),
    });
  }

  const rows = query.data?.content ?? [];
  const decidable = new Set(["OPEN", "UNDER_REVIEW"]);

  return (
    <DashboardShell
      role="ADMIN"
      title="Disputes"
      lede="The money stays on the record until the decision is recorded here."
    >
      {error && <Alert>{error}</Alert>}
      {query.isLoading && <SkeletonRows count={3} />}
      {!query.isLoading && rows.length === 0 && (
        <Empty title="No disputes" mark="search">
          Nothing has been raised against a record.
        </Empty>
      )}
      {rows.length > 0 && (
        <RowGroup>
          {rows.map((dispute) => (
            <Row
              key={dispute.reference}
              title={labelize(dispute.reason)}
              sub={dispute.description}
              meta={`${dispute.reference} · escrow ${dispute.escrowReference}`}
              right={
                <>
                  <p className="text-[0.9375rem] font-semibold">
                    <Money value={dispute.amountDisputed} whole />
                  </p>
                  <StatusPill value={dispute.status} />
                  {decidable.has(dispute.status) && (
                    <button
                      type="button"
                      className="text-[0.875rem] font-semibold text-primary"
                      onClick={() => setOpen((ref) => (ref === dispute.reference ? null : dispute.reference))}
                    >
                      {open === dispute.reference ? "Close" : "Decide"}
                    </button>
                  )}
                </>
              }
            >
              {open === dispute.reference && (
                <form
                  onSubmit={(event) => onSubmit(event, dispute.reference)}
                  className="grid gap-3 md:grid-cols-[1fr_1.4fr_auto] md:items-end"
                >
                  <Field label="Decision">
                    <select className={inputClass} name="resolution" defaultValue="RESOLVED_CUSTOMER">
                      <option value="RESOLVED_CUSTOMER">Refund the payer</option>
                      <option value="RESOLVED_MERCHANT">Pay the merchant</option>
                      <option value="CLOSED">Close without a payment</option>
                    </select>
                  </Field>
                  <Field label="Notes" hint="What the evidence showed.">
                    <input className={inputClass} name="resolutionNotes" />
                  </Field>
                  <Button type="submit" disabled={resolve.isPending}>
                    Record decision
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

"use client";

import { FormEvent, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { DashboardShell } from "@/components/dashboard-shell";
import {
  Alert,
  Button,
  Card,
  Empty,
  Field,
  Money,
  Row,
  RowGroup,
  SectionHead,
  SkeletonRows,
  StatusPill,
  inputClass,
} from "@/components/ui";
import { api, ApiError } from "@/lib/api";
import { FEE_MAX, FEE_MIN } from "@/lib/format";
import type { FeeConfig, PageResult } from "@/lib/types";

export default function AdminFeesPage() {
  const client = useQueryClient();
  const [error, setError] = useState<string | null>(null);

  const query = useQuery({
    queryKey: ["fees"],
    queryFn: () => api<PageResult<FeeConfig>>("/api/v1/admin/fee-configurations?page=0&size=20"),
  });
  const create = useMutation({
    mutationFn: (body: unknown) => api("/api/v1/admin/fee-configurations", { body }),
    onSuccess: () => {
      setError(null);
      void client.invalidateQueries({ queryKey: ["fees"] });
    },
    onError: (err: Error) => setError(err instanceof ApiError ? err.message : err.message),
  });
  const deactivate = useMutation({
    mutationFn: (id: string) => api(`/api/v1/admin/fee-configurations/${id}`, { method: "DELETE" }),
    onSuccess: () => void client.invalidateQueries({ queryKey: ["fees"] }),
    onError: (err: Error) => setError(err instanceof ApiError ? err.message : err.message),
  });

  function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const merchantId = String(form.get("merchantId") || "").trim();
    create.mutate({
      ...(merchantId ? { merchantId } : {}),
      feeType: form.get("feeType"),
      feeValue: Number(form.get("feeValue")),
      minFee: Number(form.get("minFee")),
      maxFee: Number(form.get("maxFee")),
    });
  }

  const rows = query.data?.content ?? [];

  return (
    <DashboardShell
      role="ADMIN"
      title="Fee configuration"
      lede="This is where a large account leaves the published cap."
    >
      {error && <Alert>{error}</Alert>}

      <Card className="space-y-5">
        <SectionHead
          title="Add a schedule"
          description="The rate is a fraction: 0.015 is 1.5 percent. Leave the merchant blank to replace the global default, or paste one merchant id to price that account on its own."
        />
        <form onSubmit={onSubmit} className="grid gap-5 md:grid-cols-2">
          <Field label="Merchant id" hint="Blank means the global default.">
            <input className={`${inputClass} num`} name="merchantId" placeholder="Global default" />
          </Field>
          <Field label="Fee type">
            <select className={inputClass} name="feeType" defaultValue="PERCENTAGE">
              <option value="PERCENTAGE">Percentage</option>
              <option value="FLAT">Flat</option>
              <option value="BLENDED">Blended</option>
            </select>
          </Field>
          <Field label="Rate" hint="A fraction. 0.015 is 1.5 percent.">
            <input
              className={`${inputClass} num`}
              name="feeValue"
              type="number"
              step="0.0001"
              required
              placeholder="0.015"
            />
          </Field>
          <div className="grid gap-5 sm:grid-cols-2">
            <Field label="Floor (NGN)">
              <input className={`${inputClass} num`} name="minFee" type="number" step="0.01" defaultValue={FEE_MIN} />
            </Field>
            <Field label="Cap (NGN)">
              <input className={`${inputClass} num`} name="maxFee" type="number" step="0.01" defaultValue={FEE_MAX} />
            </Field>
          </div>
          <div className="md:col-span-2">
            <Button type="submit" disabled={create.isPending}>
              {create.isPending ? "Saving…" : "Add schedule"}
            </Button>
          </div>
        </form>
      </Card>

      <div className="space-y-3">
        <SectionHead title="Active schedules" description="The merchant schedule wins over the global default." />
        {query.isLoading && <SkeletonRows count={3} />}
        {!query.isLoading && rows.length === 0 && (
          <Empty title="No schedule saved" mark="record">
            Every escrow falls back to 1.5 percent with a ₦500 floor and a ₦50,000 cap.
          </Empty>
        )}
        {rows.length > 0 && (
          <RowGroup>
            {rows.map((fee) => (
              <Row
                key={fee.id}
                title={
                  fee.feeType === "PERCENTAGE" ? (
                    <span>
                      <span className="num">{(fee.feeValue * 100).toFixed(2)}</span> percent
                    </span>
                  ) : (
                    <span>
                      {fee.feeType.toLowerCase()} <span className="num">{fee.feeValue}</span>
                    </span>
                  )
                }
                sub={
                  <span>
                    Floor <Money value={fee.minFee ?? 0} whole /> · cap <Money value={fee.maxFee ?? 0} whole />
                  </span>
                }
                meta={fee.merchantId ? `merchant ${fee.merchantId}` : "global default"}
                right={
                  <>
                    <StatusPill
                      value={fee.isActive === false ? "CLOSED" : "APPROVED"}
                      label={fee.isActive === false ? "Inactive" : "Active"}
                    />
                    {fee.isActive !== false && (
                      <button
                        type="button"
                        className="text-[0.875rem] font-semibold text-danger"
                        onClick={() => deactivate.mutate(fee.id)}
                      >
                        Deactivate
                      </button>
                    )}
                  </>
                }
              />
            ))}
          </RowGroup>
        )}
      </div>
    </DashboardShell>
  );
}

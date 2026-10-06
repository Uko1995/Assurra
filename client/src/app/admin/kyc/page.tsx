"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { FormEvent, useState } from "react";
import { DashboardShell } from "@/components/dashboard-shell";
import { Alert, Button, Empty, Field, Row, RowGroup, SkeletonRows, inputClass } from "@/components/ui";
import { api, ApiError } from "@/lib/api";
import type { KycMerchant, PageResult } from "@/lib/types";

export default function AdminKycPage() {
  const client = useQueryClient();
  const [error, setError] = useState<string | null>(null);
  const [rejecting, setRejecting] = useState<string | null>(null);

  const pending = useQuery({
    queryKey: ["kyc", "pending"],
    queryFn: () => api<PageResult<KycMerchant>>("/api/v1/admin/kyc/pending?page=0&size=20"),
  });
  const review = useQuery({
    queryKey: ["kyc", "under-review"],
    queryFn: () => api<PageResult<KycMerchant>>("/api/v1/admin/kyc/under-review?page=0&size=20"),
  });

  const act = useMutation({
    mutationFn: (path: string) => api(path, { method: "PUT" }),
    onSuccess: () => {
      setError(null);
      setRejecting(null);
      void client.invalidateQueries({ queryKey: ["kyc"] });
    },
    onError: (err: Error) => setError(err instanceof ApiError ? err.message : err.message),
  });

  const rows = [...(pending.data?.content ?? []), ...(review.data?.content ?? [])];
  const loading = pending.isLoading || review.isLoading;

  function reject(event: FormEvent<HTMLFormElement>, userId: string) {
    event.preventDefault();
    const reason = String(new FormData(event.currentTarget).get("reason") || "").trim();
    if (!reason) return;
    act.mutate(`/api/v1/admin/kyc/${userId}/reject?rejectionReason=${encodeURIComponent(reason)}`);
  }

  return (
    <DashboardShell
      role="ADMIN"
      title="KYC review"
      lede="A merchant cannot be settled until this is approved."
    >
      {error && <Alert>{error}</Alert>}
      {loading && <SkeletonRows count={4} />}
      {!loading && rows.length === 0 && (
        <Empty title="Nothing is waiting on you" mark="shield">
          No merchants are pending or under review.
        </Empty>
      )}
      {!loading && rows.length > 0 && (
        <RowGroup>
          {rows.map((merchant) => (
            <Row
              key={merchant.userId}
              title={merchant.businessName ?? merchant.fullName}
              sub={`${merchant.email}${merchant.bankName ? ` · ${merchant.bankName}` : ""}`}
              meta={merchant.businessType ?? "No business type given"}
              right={
                <div className="flex gap-2">
                  <Button
                    size="sm"
                    disabled={act.isPending}
                    onClick={() => act.mutate(`/api/v1/admin/kyc/${merchant.userId}/approve`)}
                  >
                    Approve
                  </Button>
                  <Button
                    size="sm"
                    variant="secondary"
                    onClick={() => setRejecting((id) => (id === merchant.userId ? null : merchant.userId))}
                  >
                    Reject
                  </Button>
                </div>
              }
            >
              {rejecting === merchant.userId && (
                <form onSubmit={(event) => reject(event, merchant.userId)} className="flex flex-wrap items-end gap-3">
                  <div className="min-w-60 flex-1">
                    <Field label="Why is this rejected?" hint="The merchant sees this reason on their KYC screen.">
                      <input className={inputClass} name="reason" required />
                    </Field>
                  </div>
                  <Button type="submit" variant="danger" disabled={act.isPending}>
                    Reject KYC
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

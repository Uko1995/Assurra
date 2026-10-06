"use client";

import { FormEvent, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { DashboardShell } from "@/components/dashboard-shell";
import { Alert, Button, Card, Field, SectionHead, Skeleton, StatusPill, inputClass } from "@/components/ui";
import { api, ApiError } from "@/lib/api";
import { labelize } from "@/lib/format";
import type { MerchantProfile } from "@/lib/types";

const documents = ["CAC_CERT", "UTILITY_BILL", "ID_CARD", "PASSPORT", "DRIVERS_LICENSE", "BANK_STATEMENT", "OTHER"];

export default function MerchantKycPage() {
  const client = useQueryClient();
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const profile = useQuery({
    queryKey: ["merchant-profile"],
    queryFn: () => api<MerchantProfile>("/api/v1/merchants/profile"),
  });

  const submit = useMutation({
    mutationFn: (body: unknown) => api("/api/v1/merchants/kyc", { body }),
    onSuccess: () => {
      setError(null);
      setMessage("KYC details saved. They are reviewed next.");
      void client.invalidateQueries({ queryKey: ["merchant-profile"] });
    },
    onError: (err: Error) => setError(err instanceof ApiError ? err.message : err.message),
  });

  function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    submit.mutate({
      businessName: form.get("businessName"),
      businessType: form.get("businessType"),
      businessRegNumber: form.get("businessRegNumber"),
      bankName: form.get("bankName"),
      bankCode: form.get("bankCode"),
      bankAccountNumber: form.get("bankAccountNumber"),
      bvn: form.get("bvn"),
    });
  }

  async function upload(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const element = event.currentTarget;
    const form = new FormData(element);
    const file = form.get("file");
    if (!(file instanceof File) || file.size === 0) return;
    const body = new FormData();
    body.set("file", file);
    body.set("documentType", String(form.get("documentType")));
    try {
      await api("/api/v1/merchants/kyc/documents", { form: body });
      setError(null);
      setMessage("Document uploaded.");
      element.reset();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Upload failed");
    }
  }

  const status = profile.data?.kycStatus;

  return (
    <DashboardShell
      role="MERCHANT"
      title="KYC"
      lede="A payout can only reach you once this is approved."
    >
      <Card className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <p className="meta text-faint">Status</p>
          {profile.isLoading ? (
            <Skeleton className="mt-2 h-6 w-28" />
          ) : (
            <div className="mt-2 flex items-center gap-2">
              <StatusPill value={status ?? "NOT_SUBMITTED"} label={status ? labelize(status) : "Not submitted"} />
            </div>
          )}
          {profile.data?.kycRejectionReason && (
            <p className="mt-2 max-w-md text-[0.9375rem] text-danger">{profile.data.kycRejectionReason}</p>
          )}
        </div>
        <p className="max-w-xs text-[0.875rem] text-muted">
          Bank account numbers and BVNs are stored encrypted and used for settlement and KYC only.
        </p>
      </Card>

      {message && <Alert tone="success">{message}</Alert>}
      {error && <Alert>{error}</Alert>}

      <Card className="space-y-5">
        <SectionHead title="Business and settlement" />
        <form onSubmit={onSubmit} className="grid gap-5 md:grid-cols-2">
          <Field label="Business name">
            <input className={inputClass} name="businessName" required defaultValue={profile.data?.businessName} />
          </Field>
          <Field label="Business type">
            <input className={inputClass} name="businessType" defaultValue={profile.data?.businessType} />
          </Field>
          <Field label="Registration number" hint="CAC number, if you have one.">
            <input className={`${inputClass} num`} name="businessRegNumber" />
          </Field>
          <Field label="Bank name">
            <input className={inputClass} name="bankName" required defaultValue={profile.data?.bankName} />
          </Field>
          <Field label="Bank code">
            <input className={`${inputClass} num`} name="bankCode" required />
          </Field>
          <Field label="Account number" hint="10 digits.">
            <input className={`${inputClass} num`} name="bankAccountNumber" required pattern="\d{10}" />
          </Field>
          <Field label="BVN" hint="11 digits.">
            <input className={`${inputClass} num`} name="bvn" required pattern="\d{11}" />
          </Field>
          <div className="md:col-span-2">
            <Button type="submit" disabled={submit.isPending}>
              {submit.isPending ? "Saving…" : "Submit KYC"}
            </Button>
          </div>
        </form>
      </Card>

      <Card className="space-y-5">
        <SectionHead
          title="Documents"
          description="Upload one file at a time. A CAC certificate and an ID move a review along fastest."
        />
        <form onSubmit={upload} className="grid gap-5 md:grid-cols-2 md:items-end">
          <Field label="Type">
            <select className={inputClass} name="documentType" defaultValue="ID_CARD">
              {documents.map((type) => (
                <option key={type} value={type}>
                  {labelize(type)}
                </option>
              ))}
            </select>
          </Field>
          <Field label="File">
            <input className={inputClass} name="file" type="file" required />
          </Field>
          <div className="md:col-span-2">
            <Button type="submit" variant="secondary">
              Upload document
            </Button>
          </div>
        </form>
      </Card>
    </DashboardShell>
  );
}

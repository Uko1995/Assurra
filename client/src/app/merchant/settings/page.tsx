"use client";

import { FormEvent, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { DashboardShell } from "@/components/dashboard-shell";
import { Alert, Button, Card, Field, KeyValue, SectionHead, Skeleton, inputClass } from "@/components/ui";
import { api, ApiError } from "@/lib/api";
import type { ApiKeyInfo, MerchantProfile } from "@/lib/types";

export default function MerchantSettingsPage() {
  const [secret, setSecret] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [note, setNote] = useState<string | null>(null);
  const [pending, setPending] = useState(false);

  const profile = useQuery({
    queryKey: ["merchant-profile"],
    queryFn: () => api<MerchantProfile>("/api/v1/merchants/profile"),
  });
  const key = useQuery({
    queryKey: ["api-key"],
    queryFn: () => api<ApiKeyInfo>("/api/v1/merchants/api-keys"),
  });

  async function saveWebhook(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    try {
      await api("/api/v1/merchants/webhook", {
        method: "PUT",
        body: { url: form.get("url"), events: ["escrow.funded", "escrow.released"] },
      });
      setError(null);
      setNote("Webhook saved.");
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not save webhook");
    }
  }

  async function regenerate() {
    setPending(true);
    try {
      const value = await api<string>("/api/v1/merchants/api-key/regenerate", { method: "POST" });
      setSecret(value);
      setError(null);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not regenerate the key");
    } finally {
      setPending(false);
    }
  }

  return (
    <DashboardShell role="MERCHANT" title="Settings" lede="Your account, your key, and where we call you back.">
      {error && <Alert>{error}</Alert>}
      {note && <Alert tone="success">{note}</Alert>}

      <Card>
        <SectionHead title="Account" />
        <div className="mt-4">
          {profile.isLoading ? (
            <div className="space-y-2.5">
              <Skeleton className="h-3.5 w-48" />
              <Skeleton className="h-3.5 w-32" />
            </div>
          ) : (
            <KeyValue
              rows={[
                { label: "Business", value: profile.data?.businessName ?? "Not set" },
                { label: "Email", value: profile.data?.email ?? "—" },
                { label: "Bank", value: profile.data?.bankName ?? "Not set" },
                {
                  label: "Verified",
                  value: profile.data?.isVerified ? "Yes" : "Not yet",
                },
              ]}
            />
          )}
        </div>
      </Card>

      <Card className="space-y-4">
        <SectionHead
          title="API key"
          description="The full key is shown once, at the moment it is created. Store it before you leave this page."
        />
        <KeyValue rows={[{ label: "Prefix", value: <span className="num">{key.data?.apiKeyPrefix ?? "Not issued"}</span> }]} />
        {secret && (
          <Alert tone="warn">
            Copy this key now. It will not be shown again:{" "}
            <span className="num break-all font-semibold">{secret}</span>
          </Alert>
        )}
        <Button variant="secondary" onClick={regenerate} disabled={pending}>
          {pending ? "Generating…" : "Regenerate API key"}
        </Button>
      </Card>

      <Card className="space-y-5">
        <SectionHead
          title="Webhook"
          description="We post escrow.funded and escrow.released to this URL."
        />
        <form onSubmit={saveWebhook} className="max-w-xl space-y-4">
          <Field label="Webhook URL">
            <input
              className={inputClass}
              name="url"
              type="url"
              required
              placeholder="https://"
              defaultValue={profile.data?.webhookUrl}
            />
          </Field>
          <Button type="submit">Save webhook</Button>
        </form>
      </Card>
    </DashboardShell>
  );
}

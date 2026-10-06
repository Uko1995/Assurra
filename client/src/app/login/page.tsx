"use client";

import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { FormEvent, Suspense, useState } from "react";
import { AuthFrame } from "@/components/site-chrome";
import { Alert, Button, Field, KeyValue, Segmented, Skeleton, inputClass } from "@/components/ui";
import { api, ApiError } from "@/lib/api";
import { useAuth } from "@/lib/auth-store";
import { homeForRole } from "@/lib/format";
import type { AuthSession, Role } from "@/lib/types";

const roles: { value: Role; label: string }[] = [
  { value: "CUSTOMER", label: "Payer" },
  { value: "MERCHANT", label: "Merchant" },
];

function LoginForm() {
  const router = useRouter();
  const params = useSearchParams();
  const setSession = useAuth((state) => state.setSession);
  const [role, setRole] = useState<Role>("CUSTOMER");
  const [error, setError] = useState<string | null>(null);
  const [pending, setPending] = useState(false);
  const registered = params.get("registered") === "1";
  const expired = params.get("reason") === "session_expired";

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setPending(true);
    const form = new FormData(event.currentTarget);
    try {
      const session = await api<AuthSession>(`/api/v1/auth/login/${role.toLowerCase()}`, {
        body: { email: form.get("email"), password: form.get("password") },
        auth: false,
      });
      setSession(session);
      router.replace(homeForRole(session.user.role));
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Sign in failed");
    } finally {
      setPending(false);
    }
  }

  return (
    <AuthFrame
      title="Sign in"
      lede="Assurra holds the payment until the payer confirms the stage. Sign in as a payer or a merchant."
      aside={
        <div className="mt-10 hidden lg:block">
          <KeyValue
            rows={[
              { label: "Payer", value: "Pays, confirms, disputes" },
              { label: "Merchant", value: "Records progress, gets paid" },
            ]}
          />
        </div>
      }
    >
      <div className="space-y-4">
        {registered && <Alert tone="success">Account created. Sign in to continue.</Alert>}
        {expired && <Alert tone="quiet">Your session ended. Sign in again.</Alert>}
        <form onSubmit={onSubmit} className="space-y-4">
          <Field label="Account type">
            <Segmented options={roles} value={role} onChange={setRole} />
          </Field>
          <Field label="Email">
            <input className={inputClass} name="email" type="email" autoComplete="email" required />
          </Field>
          <Field label="Password">
            <input
              className={inputClass}
              name="password"
              type="password"
              autoComplete="current-password"
              required
              minLength={8}
            />
          </Field>
          {error && <Alert>{error}</Alert>}
          <Button type="submit" full disabled={pending}>
            {pending ? "Signing in…" : "Sign in"}
          </Button>
        </form>
        <p className="text-[0.9375rem] text-muted">
          New here?{" "}
          <Link href="/register" className="font-semibold text-primary">
            Create an account
          </Link>
        </p>
      </div>
    </AuthFrame>
  );
}

export default function LoginPage() {
  return (
    <Suspense
      fallback={
        <div className="mx-auto max-w-md space-y-3 p-8">
          <Skeleton className="h-4 w-32" />
          <Skeleton className="h-4 w-48" />
        </div>
      }
    >
      <LoginForm />
    </Suspense>
  );
}

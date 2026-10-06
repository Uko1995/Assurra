"use client";

import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { FormEvent, Suspense, useState } from "react";
import { AuthFrame } from "@/components/site-chrome";
import { Alert, Button, Checkbox, Field, KeyValue, Segmented, Skeleton, inputClass } from "@/components/ui";
import { api, ApiError } from "@/lib/api";
import { ESCROW_MAX, FEE_MAX, FEE_MIN, naira } from "@/lib/format";

const accounts = [
  { value: "customer" as const, label: "I am paying" },
  { value: "merchant" as const, label: "I am being paid" },
];

function RegisterForm() {
  const router = useRouter();
  const params = useSearchParams();
  const initial = params.get("role") === "merchant" ? "merchant" : "customer";
  const [role, setRole] = useState<"customer" | "merchant">(initial);
  const [error, setError] = useState<string | null>(null);
  const [pending, setPending] = useState(false);

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setPending(true);
    const form = new FormData(event.currentTarget);
    const shared = {
      fullName: form.get("fullName"),
      email: form.get("email"),
      phone: form.get("phone"),
      password: form.get("password"),
      termsAccepted: form.get("termsAccepted") === "on",
      dataProcessingConsent: form.get("dataProcessingConsent") === "on",
      marketingConsent: form.get("marketingConsent") === "on",
    };
    const body =
      role === "customer"
        ? shared
        : {
            ...shared,
            businessName: form.get("businessName"),
            businessType: form.get("businessType"),
            businessRegNumber: form.get("businessRegNumber"),
            bankAccountNumber: form.get("bankAccountNumber"),
            bankCode: form.get("bankCode"),
            bankName: form.get("bankName"),
            bvn: form.get("bvn"),
          };
    try {
      await api(`/api/v1/auth/register/${role}`, { body, auth: false });
      router.replace("/login?registered=1");
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Registration failed");
    } finally {
      setPending(false);
    }
  }

  return (
    <AuthFrame
      title="Create an account"
      lede="Registration takes a name, an email, a phone number, and consent. It does not sign you in, and nothing is charged until a record is funded."
      aside={
        <div className="mt-10 hidden lg:block">
          <KeyValue
            rows={[
              { label: "Published fee", value: `1.5 percent, ${naira(FEE_MIN)}–${naira(FEE_MAX)}` },
              { label: "One record holds", value: `₦100 to ${naira(ESCROW_MAX)}` },
              { label: "Large accounts", value: "Own percentage, floor, and cap" },
            ]}
          />
        </div>
      }
    >
      <form onSubmit={onSubmit} className="space-y-5">
        <Field label="What is this account for?">
          <Segmented options={accounts} value={role} onChange={setRole} />
        </Field>

        <fieldset className="space-y-4 border-t border-line pt-5">
          <legend className="meta text-faint">Your details</legend>
          <Field label="Full name">
            <input className={inputClass} name="fullName" required minLength={2} autoComplete="name" />
          </Field>
          <Field label="Email">
            <input className={inputClass} name="email" type="email" required autoComplete="email" />
          </Field>
          <Field label="Phone" hint="A Nigerian mobile number is fine.">
            <input className={inputClass} name="phone" required minLength={10} maxLength={20} autoComplete="tel" />
          </Field>
          <Field label="Password" hint="At least 8 characters.">
            <input
              className={inputClass}
              name="password"
              type="password"
              required
              minLength={8}
              autoComplete="new-password"
            />
          </Field>
        </fieldset>

        {role === "merchant" && (
          <>
            <fieldset className="space-y-4 border-t border-line pt-5">
              <legend className="meta text-faint">Business</legend>
              <Field label="Business name">
                <input className={inputClass} name="businessName" required minLength={3} />
              </Field>
              <Field label="Business type">
                <input className={inputClass} name="businessType" placeholder="Trading, services, contracting" />
              </Field>
              <Field label="Registration number" hint="CAC number, if you have one.">
                <input className={inputClass} name="businessRegNumber" />
              </Field>
            </fieldset>
            <fieldset className="space-y-4 border-t border-line pt-5">
              <legend className="meta text-faint">Settlement</legend>
              <Field label="Bank name">
                <input className={inputClass} name="bankName" required />
              </Field>
              <Field label="Bank code">
                <input className={`${inputClass} num`} name="bankCode" required />
              </Field>
              <Field label="Account number" hint="10 digits.">
                <input className={`${inputClass} num`} name="bankAccountNumber" required pattern="\d{10}" />
              </Field>
              <Field label="BVN" hint="11 digits. Stored encrypted and used for KYC only.">
                <input className={`${inputClass} num`} name="bvn" required pattern="\d{11}" />
              </Field>
            </fieldset>
          </>
        )}

        <div className="space-y-3 border-t border-line pt-5">
          <Checkbox name="termsAccepted" required>
            I accept the{" "}
            <Link href="/legal/terms" className="font-semibold text-primary">
              terms of service
            </Link>
          </Checkbox>
          <Checkbox name="dataProcessingConsent" required>
            I consent to the data processing needed to run an escrow account
          </Checkbox>
          <Checkbox name="marketingConsent">Send me product updates</Checkbox>
        </div>

        {error && <Alert>{error}</Alert>}
        <Button type="submit" full disabled={pending}>
          {pending ? "Creating account…" : "Create account"}
        </Button>
        <p className="text-[0.9375rem] text-muted">
          Already registered?{" "}
          <Link href="/login" className="font-semibold text-primary">
            Sign in
          </Link>
        </p>
      </form>
    </AuthFrame>
  );
}

export default function RegisterPage() {
  return (
    <Suspense
      fallback={
        <div className="mx-auto max-w-md space-y-3 p-8">
          <Skeleton className="h-4 w-40" />
          <Skeleton className="h-4 w-56" />
        </div>
      }
    >
      <RegisterForm />
    </Suspense>
  );
}

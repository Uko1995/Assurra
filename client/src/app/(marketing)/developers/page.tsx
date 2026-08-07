import type { Metadata } from "next";
import { PageHero } from "@/components/PageHero";
import { Button } from "@/components/ui/Button";
import { Section, SectionHeading } from "@/components/ui/Section";
import { developerEndpoints } from "@/content/pages";
import { appPath } from "@/lib/site";

export const metadata: Metadata = {
  title: "Developers",
  description:
    "Assurra API-first escrow: /api/v1 gateway, JWT and X-API-Key auth, idempotent escrow create, HMAC webhooks.",
};

export default function DevelopersPage() {
  return (
    <>
      <PageHero
        eyebrow="Developers"
        title="Add escrow in days, not months"
        description="White-label, naira-native escrow behind /api/v1. JWT for users, X-API-Key for KYC-verified merchants, Interswitch for payments, HMAC webhooks for state changes."
        actions={
          <>
            <Button href={appPath("/register/merchant")}>Get API access</Button>
            <Button href="/how-it-works" variant="secondary">
              Review lifecycle
            </Button>
          </>
        }
      />

      <Section>
        <SectionHeading
          title="Core endpoints"
          description="Path examples match the Assurra gateway. Response envelope: { success, message, data, timestamp }."
        />
        <div className="overflow-hidden rounded-[16px] border border-border-subtle bg-secondary text-white shadow-[var(--shadow-card)]">
          <ul className="divide-y divide-white/10">
            {developerEndpoints.map((ep) => (
              <li key={ep.path} className="grid gap-2 px-5 py-4 sm:grid-cols-[5rem_1fr]">
                <span className="text-xs font-bold tracking-wide text-[#91fa91]">{ep.method}</span>
                <div>
                  <code className="text-sm text-white">{ep.path}</code>
                  <p className="mt-1 text-sm text-white/70">{ep.note}</p>
                </div>
              </li>
            ))}
          </ul>
        </div>
      </Section>

      <Section tone="muted">
        <SectionHeading
          title="Webhooks & sandbox"
          description="Merchant callbacks are HMAC-SHA256 verified with retries at 1m, 5m, 30m, 2h, and 24h. Use sandbox to integrate without moving real money."
        />
        <pre className="overflow-x-auto rounded-[16px] border border-border-subtle bg-surface-lowest p-5 text-sm leading-7 text-on-surface">
{`curl -X POST "$GATEWAY/api/v1/escrow" \\
  -H "Authorization: Bearer $TOKEN" \\
  -H "X-Idempotency-Key: $(uuidgen)" \\
  -H "Content-Type: application/json" \\
  -d '{ "merchantId": "...", "amount": 100000, "currency": "NGN" }'`}
        </pre>
      </Section>
    </>
  );
}

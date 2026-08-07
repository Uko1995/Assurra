"use client";

import { useEffect, useState } from "react";
import { useSettingsStore } from "@/stores/settings";

export function ConsentBanner() {
  const consent = useSettingsStore((s) => s.analyticsConsent);
  const setConsent = useSettingsStore((s) => s.setAnalyticsConsent);
  const [ready, setReady] = useState(false);

  useEffect(() => setReady(true), []);

  if (!ready || consent !== null) return null;

  return (
    <div className="fixed inset-x-0 bottom-0 z-50 border-t border-border-subtle bg-surface-lowest/95 p-4 shadow-[0_-8px_30px_rgba(18,26,19,0.08)] backdrop-blur-md">
      <div className="mx-auto flex max-w-[var(--container-max)] flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <p className="text-sm leading-6 text-muted">
          We use optional analytics cookies to improve Assurra. Essential cookies keep the site
          working. See our{" "}
          <a href="/legal/privacy" className="font-semibold text-primary underline-offset-2 hover:underline">
            Privacy Policy
          </a>
          .
        </p>
        <div className="flex shrink-0 gap-2">
          <button
            type="button"
            onClick={() => setConsent(false)}
            className="rounded-[12px] border border-outline px-4 py-2 text-sm font-semibold text-on-surface"
          >
            Essential only
          </button>
          <button
            type="button"
            onClick={() => setConsent(true)}
            className="rounded-[12px] bg-primary px-4 py-2 text-sm font-semibold text-on-primary"
          >
            Accept analytics
          </button>
        </div>
      </div>
    </div>
  );
}

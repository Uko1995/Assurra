"use client";

import { useSettingsStore } from "@/stores/settings";

export function DataSaverToggle({ className = "" }: { className?: string }) {
  const dataSaver = useSettingsStore((s) => s.dataSaver);
  const setDataSaver = useSettingsStore((s) => s.setDataSaver);

  return (
    <button
      type="button"
      onClick={() => setDataSaver(!dataSaver)}
      className={`inline-flex items-center gap-2 rounded-full border border-outline-variant px-3 py-1.5 text-xs font-semibold text-muted transition-colors hover:border-primary hover:text-primary ${className}`}
      aria-pressed={dataSaver}
    >
      <span
        className={`h-2 w-2 rounded-full ${dataSaver ? "bg-primary" : "bg-outline"}`}
        aria-hidden
      />
      Data Saver {dataSaver ? "on" : "off"}
    </button>
  );
}

"use client";

import { useEffect } from "react";
import { useSettingsStore } from "@/stores/settings";

export function DataSaverSync() {
  const dataSaver = useSettingsStore((s) => s.dataSaver);

  useEffect(() => {
    document.body.dataset.saver = dataSaver ? "true" : "false";
  }, [dataSaver]);

  return null;
}

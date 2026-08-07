"use client";

import { create } from "zustand";
import { persist } from "zustand/middleware";

type SettingsState = {
  dataSaver: boolean;
  analyticsConsent: boolean | null;
  setDataSaver: (value: boolean) => void;
  setAnalyticsConsent: (value: boolean) => void;
};

export const useSettingsStore = create<SettingsState>()(
  persist(
    (set) => ({
      dataSaver: false,
      analyticsConsent: null,
      setDataSaver: (dataSaver) => set({ dataSaver }),
      setAnalyticsConsent: (analyticsConsent) => set({ analyticsConsent }),
    }),
    { name: "assurra-settings" }
  )
);

"use client";

import { create } from "zustand";
import type { AuthSession, User } from "@/lib/types";

const REFRESH_KEY = "assurra.refreshToken";

type AuthState = {
  accessToken: string | null;
  user: User | null;
  ready: boolean;
  setSession: (session: AuthSession) => void;
  setUser: (user: User) => void;
  clear: () => void;
  markReady: () => void;
};

export const useAuth = create<AuthState>((set) => ({
  accessToken: null,
  user: null,
  ready: false,
  setSession: (session) => {
    window.localStorage.setItem(REFRESH_KEY, session.refreshToken);
    set({ accessToken: session.accessToken, user: session.user, ready: true });
  },
  setUser: (user) => set({ user }),
  clear: () => {
    window.localStorage.removeItem(REFRESH_KEY);
    set({ accessToken: null, user: null, ready: true });
  },
  markReady: () => set({ ready: true }),
}));

export function readRefreshToken() {
  if (typeof window === "undefined") return null;
  return window.localStorage.getItem(REFRESH_KEY);
}

"use client";

import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import { readRefreshToken, useAuth } from "@/lib/auth-store";
import type { AuthSession, User } from "@/lib/types";

function AuthHydrator({ children }: { children: React.ReactNode }) {
  const markReady = useAuth((state) => state.markReady);
  const setSession = useAuth((state) => state.setSession);
  const setUser = useAuth((state) => state.setUser);
  const clear = useAuth((state) => state.clear);

  useEffect(() => {
    const refreshToken = readRefreshToken();
    if (!refreshToken) {
      markReady();
      return;
    }
    let cancelled = false;
    api<AuthSession>("/api/v1/auth/refresh", {
      method: "POST",
      body: { refreshToken },
      auth: false,
    })
      .then(async (session) => {
        if (cancelled) return;
        setSession(session);
        const me = await api<User>("/api/v1/auth/me");
        if (!cancelled) setUser(me);
      })
      .catch(() => {
        if (!cancelled) clear();
      });
    return () => {
      cancelled = true;
    };
  }, [clear, markReady, setSession, setUser]);

  return children;
}

export function Providers({ children }: { children: React.ReactNode }) {
  const [client] = useState(
    () =>
      new QueryClient({
        defaultOptions: {
          queries: { retry: (count, error) => {
            const status = (error as { status?: number }).status;
            if (status && status >= 400 && status < 500) return false;
            return count < 2;
          } },
        },
      }),
  );

  return (
    <QueryClientProvider client={client}>
      <AuthHydrator>{children}</AuthHydrator>
    </QueryClientProvider>
  );
}

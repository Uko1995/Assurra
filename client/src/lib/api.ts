"use client";

import { readRefreshToken, useAuth } from "@/lib/auth-store";
import type { AuthSession } from "@/lib/types";

const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

type Envelope<T> = {
  success?: boolean;
  message?: string;
  data?: T;
  error?: string;
  status?: number;
};

export class ApiError extends Error {
  status: number;

  constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

let refreshFlight: Promise<string | null> | null = null;

async function parseBody(response: Response): Promise<Envelope<unknown>> {
  const text = await response.text();
  if (!text) return {};
  try {
    return JSON.parse(text) as Envelope<unknown>;
  } catch {
    return { message: text };
  }
}

function messageFrom(body: Envelope<unknown>, fallback: string) {
  return body.message || body.error || fallback;
}

async function refreshAccessToken(): Promise<string | null> {
  const refreshToken = readRefreshToken();
  if (!refreshToken) return null;
  const response = await fetch(`${API_URL}/api/v1/auth/refresh`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ refreshToken }),
  });
  const body = (await parseBody(response)) as Envelope<AuthSession>;
  if (!response.ok || !body.data?.accessToken) {
    useAuth.getState().clear();
    return null;
  }
  useAuth.getState().setSession(body.data);
  return body.data.accessToken;
}

function singleFlightRefresh() {
  if (!refreshFlight) {
    refreshFlight = refreshAccessToken().finally(() => {
      refreshFlight = null;
    });
  }
  return refreshFlight;
}

type RequestOptions = {
  method?: string;
  body?: unknown;
  form?: FormData;
  auth?: boolean;
  retry?: boolean;
  headers?: Record<string, string>;
};

export async function api<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const headers = new Headers();
  if (!(options.form instanceof FormData)) {
    headers.set("Content-Type", "application/json");
  }
  if (options.auth !== false) {
    const token = useAuth.getState().accessToken;
    if (token) headers.set("Authorization", `Bearer ${token}`);
  }
  for (const [key, value] of Object.entries(options.headers ?? {})) {
    headers.set(key, value);
  }

  const response = await fetch(`${API_URL}${path}`, {
    method: options.method ?? (options.body || options.form ? "POST" : "GET"),
    headers,
    body: options.form ?? (options.body !== undefined ? JSON.stringify(options.body) : undefined),
  });

  if (response.status === 401 && options.auth !== false && options.retry !== false) {
    const next = await singleFlightRefresh();
    if (next) return api<T>(path, { ...options, retry: false });
    if (typeof window !== "undefined" && !window.location.pathname.startsWith("/login")) {
      window.location.assign("/login?reason=session_expired");
    }
  }

  const body = (await parseBody(response)) as Envelope<T>;
  if (!response.ok || body.success === false) {
    throw new ApiError(response.status, messageFrom(body, "Request failed"));
  }
  return body.data as T;
}

import type { MetadataRoute } from "next";
import { env } from "@/shared/lib/env";

const staticRoutes = [
  { path: "", priority: 1, changeFrequency: "weekly" },
  { path: "/services", priority: 0.9, changeFrequency: "monthly" },
  { path: "/how-it-works", priority: 0.9, changeFrequency: "monthly" },
  { path: "/pricing", priority: 0.9, changeFrequency: "monthly" },
  { path: "/for-merchants", priority: 0.9, changeFrequency: "monthly" },
  { path: "/for-customers", priority: 0.9, changeFrequency: "monthly" },
  { path: "/developers", priority: 0.8, changeFrequency: "monthly" },
  { path: "/security", priority: 0.8, changeFrequency: "monthly" },
  { path: "/data-saver", priority: 0.7, changeFrequency: "yearly" },
  { path: "/faq", priority: 0.8, changeFrequency: "monthly" },
  { path: "/contact", priority: 0.5, changeFrequency: "yearly" },
  { path: "/legal/privacy", priority: 0.3, changeFrequency: "yearly" },
  { path: "/legal/terms", priority: 0.3, changeFrequency: "yearly" },
] as const;

export default function sitemap(): MetadataRoute.Sitemap {
  const origin = env.NEXT_PUBLIC_APP_URL.replace(/\/$/, "");

  return staticRoutes.map((route) => ({
    url: `${origin}${route.path}`,
    lastModified: new Date(),
    changeFrequency: route.changeFrequency,
    priority: route.priority,
  }));
}

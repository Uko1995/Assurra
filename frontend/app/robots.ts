import type { MetadataRoute } from "next";
import { env } from "@/shared/lib/env";

export default function robots(): MetadataRoute.Robots {
  const origin = env.NEXT_PUBLIC_APP_URL.replace(/\/$/, "");

  return {
    rules: [
      {
        userAgent: "*",
        allow: "/",
        disallow: ["/api/", "/dashboard/"],
      },
    ],
    sitemap: `${origin}/sitemap.xml`,
  };
}

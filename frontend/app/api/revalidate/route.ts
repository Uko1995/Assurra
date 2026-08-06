import { revalidatePath } from "next/cache";
import { NextRequest, NextResponse } from "next/server";

const REVALIDATION_TOKEN = process.env.REVALIDATION_TOKEN;

export async function POST(request: NextRequest) {
  const authHeader = request.headers.get("authorization");
  const token = authHeader?.startsWith("Bearer ") ? authHeader.slice(7) : null;

  if (!REVALIDATION_TOKEN || token !== REVALIDATION_TOKEN) {
    return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  }

  const body = (await request.json().catch(() => ({}))) as { path?: string };

  try {
    revalidatePath(body.path ?? "/");
    return NextResponse.json({ revalidated: true, timestamp: Date.now() });
  } catch (error) {
    return NextResponse.json(
      { error: "Revalidation failed", detail: error instanceof Error ? error.message : undefined },
      { status: 500 },
    );
  }
}

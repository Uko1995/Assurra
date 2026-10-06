"use client";

import { DashboardShell } from "@/components/dashboard-shell";
import { EscrowList } from "@/components/escrow-list";

export default function AdminEscrowsPage() {
  return (
    <DashboardShell role="ADMIN" title="All escrows" lede="Read-only. Actions belong to the payer and the merchant.">
      <EscrowList basePath="/admin/escrows" endpoint="/api/v1/admin/escrows" />
    </DashboardShell>
  );
}

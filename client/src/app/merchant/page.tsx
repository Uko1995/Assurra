"use client";

import { DashboardShell } from "@/components/dashboard-shell";
import { EscrowQueues } from "@/components/escrow-queues";

export default function MerchantHomePage() {
  return (
    <DashboardShell
      role="MERCHANT"
      title="Escrows"
      lede="A record reaches you once the payer has funded it."
    >
      <EscrowQueues role="MERCHANT" basePath="/merchant/escrow" endpoint="/api/v1/escrow" />
    </DashboardShell>
  );
}
